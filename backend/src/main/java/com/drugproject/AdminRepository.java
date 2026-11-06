package com.drugproject;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.drugproject.MedicineRepository.MedicineRow;

/** SQL for adding, changing and deleting medicines (Admin module). */
@Repository
public class AdminRepository {

    private final JdbcTemplate jdbc;

    public AdminRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MedicineRow> findAll() {
        return jdbc.query(
                "SELECT id, brand_name, used_for FROM medicines ORDER BY brand_name",
                (rs, n) -> new MedicineRow(rs.getInt("id"), rs.getString("brand_name"), rs.getString("used_for")));
    }

    public List<MedicineForm.AgeRuleForm> findAgeRules(int medicineId) {
        return jdbc.query(
                "SELECT age_group, status, safety_note, dose_amount, daily_limit FROM age_rules "
                        + "WHERE medicine_id = ? ORDER BY FIELD(age_group, 'child', 'adult', 'elderly')",
                (rs, n) -> new MedicineForm.AgeRuleForm(rs.getString("age_group"), rs.getString("status"),
                        rs.getString("safety_note"), rs.getString("dose_amount"), rs.getString("daily_limit")),
                medicineId);
    }

    public int insertMedicine(String name, String usedFor) {
        return insertReturningId("INSERT INTO medicines (brand_name, used_for) VALUES (?, ?)", name, usedFor);
    }

    public void updateMedicine(int id, String name, String usedFor) {
        jdbc.update("UPDATE medicines SET brand_name = ?, used_for = ? WHERE id = ?", name, usedFor, id);
    }

    /** Remove everything linked to a medicine (before saving the new version). */
    public void deleteChildren(int id) {
        jdbc.update("DELETE FROM medicine_ingredients WHERE medicine_id = ?", id);
        jdbc.update("DELETE FROM medicine_side_effects WHERE medicine_id = ?", id);
        jdbc.update("DELETE FROM age_rules WHERE medicine_id = ?", id);
        jdbc.update("DELETE FROM usage_steps WHERE medicine_id = ?", id);
    }

    public int deleteMedicine(int id) {
        return jdbc.update("DELETE FROM medicines WHERE id = ?", id);
    }

    /** Use the ingredient if it already exists, otherwise create it. */
    public int getOrCreateIngredient(String name) {
        List<Integer> ids = jdbc.query("SELECT id FROM ingredients WHERE name = ?",
                (rs, n) -> rs.getInt("id"), name);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        return insertReturningId("INSERT INTO ingredients (name) VALUES (?)", name);
    }

    /** Use the side effect if it already exists, otherwise create it. */
    public int getOrCreateSideEffect(String name, String severity) {
        List<Integer> ids = jdbc.query("SELECT id FROM side_effects WHERE name = ?",
                (rs, n) -> rs.getInt("id"), name);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        return insertReturningId("INSERT INTO side_effects (name, severity) VALUES (?, ?)", name, severity);
    }

    public void linkIngredient(int medicineId, int ingredientId, String strength) {
        jdbc.update("INSERT IGNORE INTO medicine_ingredients (medicine_id, ingredient_id, strength) VALUES (?, ?, ?)",
                medicineId, ingredientId, strength);
    }

    public void linkSideEffect(int medicineId, int sideEffectId) {
        jdbc.update("INSERT IGNORE INTO medicine_side_effects (medicine_id, side_effect_id) VALUES (?, ?)",
                medicineId, sideEffectId);
    }

    public void insertStep(int medicineId, int stepNo, String text) {
        jdbc.update("INSERT INTO usage_steps (medicine_id, step_no, step_text) VALUES (?, ?, ?)",
                medicineId, stepNo, text);
    }

    public void insertAgeRule(int medicineId, MedicineForm.AgeRuleForm r) {
        jdbc.update("INSERT INTO age_rules (medicine_id, age_group, status, safety_note, dose_amount, daily_limit) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                medicineId, r.ageGroup(), r.status(), r.note(), r.amount(), r.dailyLimit());
    }

    // Runs an INSERT and gives back the new id
    private int insertReturningId(String sql, Object... params) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keys);
        return keys.getKey().intValue();
    }
}
