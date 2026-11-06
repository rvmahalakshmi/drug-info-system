package com.drugproject;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** This file talks to the database. All the SQL is here. */
@Repository
public class MedicineRepository {

    /** One row from the medicines table */
    public record MedicineRow(int id, String name, String usedFor) {}

    private final JdbcTemplate jdbc;

    public MedicineRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Find medicines by brand name OR chemical name (spaces are ignored). */
    public List<MedicineRow> findMatches(String text) {
        String like = "%" + text.replace(" ", "") + "%";
        String sql = "SELECT m.id, m.brand_name, m.used_for FROM medicines m "
                + "WHERE REPLACE(m.brand_name, ' ', '') LIKE ? "
                + "OR m.id IN (SELECT mi.medicine_id FROM medicine_ingredients mi "
                + "            JOIN ingredients i ON i.id = mi.ingredient_id "
                + "            WHERE REPLACE(i.name, ' ', '') LIKE ?) "
                + "ORDER BY m.brand_name";
        return jdbc.query(sql,
                (rs, n) -> new MedicineRow(rs.getInt("id"), rs.getString("brand_name"), rs.getString("used_for")),
                like, like);
    }

    public Optional<MedicineRow> findById(int id) {
        List<MedicineRow> rows = jdbc.query(
                "SELECT id, brand_name, used_for FROM medicines WHERE id = ?",
                (rs, n) -> new MedicineRow(rs.getInt("id"), rs.getString("brand_name"), rs.getString("used_for")),
                id);
        return rows.stream().findFirst();
    }

    /** The chemicals inside a medicine */
    public List<MedicineResult.IngredientInfo> findIngredients(int medicineId) {
        return jdbc.query(
                "SELECT i.name, mi.strength FROM medicine_ingredients mi "
                        + "JOIN ingredients i ON i.id = mi.ingredient_id "
                        + "WHERE mi.medicine_id = ? ORDER BY i.name",
                (rs, n) -> new MedicineResult.IngredientInfo(rs.getString("name"), rs.getString("strength")),
                medicineId);
    }

    /** Safety rule for one age group (child, adult or elderly) */
    public Optional<MedicineResult.SafetyInfo> findSafety(int medicineId, String ageGroup) {
        List<MedicineResult.SafetyInfo> rows = jdbc.query(
                "SELECT status, safety_note, dose_amount, daily_limit FROM age_rules "
                        + "WHERE medicine_id = ? AND age_group = ?",
                (rs, n) -> new MedicineResult.SafetyInfo(ageGroup, rs.getString("status"),
                        rs.getString("safety_note"), rs.getString("dose_amount"), rs.getString("daily_limit")),
                medicineId, ageGroup);
        return rows.stream().findFirst();
    }

    /** "How to use" steps in order */
    public List<String> findSteps(int medicineId) {
        return jdbc.query(
                "SELECT step_text FROM usage_steps WHERE medicine_id = ? ORDER BY step_no",
                (rs, n) -> rs.getString("step_text"),
                medicineId);
    }

    /** Side effects, mild first and severe last */
    public List<MedicineResult.SideEffectInfo> findSideEffects(int medicineId) {
        return jdbc.query(
                "SELECT s.name, s.severity FROM side_effects s "
                        + "JOIN medicine_side_effects ms ON ms.side_effect_id = s.id "
                        + "WHERE ms.medicine_id = ? ORDER BY s.severity, s.name",
                (rs, n) -> new MedicineResult.SideEffectInfo(rs.getString("name"), rs.getString("severity")),
                medicineId);
    }

    /** All brand names and chemical names (for the search box suggestions) */
    public List<String> findAllNames() {
        return jdbc.query(
                "SELECT brand_name AS name FROM medicines UNION SELECT name FROM ingredients ORDER BY name",
                (rs, n) -> rs.getString("name"));
    }
}
