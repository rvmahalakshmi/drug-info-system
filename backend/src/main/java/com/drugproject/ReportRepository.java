package com.drugproject;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** SQL for the analysis reports. */
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ReportResponse.Totals totals() {
        return jdbc.queryForObject(
                "SELECT (SELECT COUNT(*) FROM medicines) AS m, "
                        + "(SELECT COUNT(*) FROM ingredients) AS i, "
                        + "(SELECT COUNT(*) FROM side_effects) AS s",
                (rs, n) -> new ReportResponse.Totals(rs.getInt("m"), rs.getInt("i"), rs.getInt("s")));
    }

    /** Which side effects appear in the most medicines */
    public List<ReportResponse.SeverityCount> commonSideEffects() {
        return jdbc.query(
                "SELECT s.name, s.severity, COUNT(*) AS c FROM side_effects s "
                        + "JOIN medicine_side_effects ms ON ms.side_effect_id = s.id "
                        + "GROUP BY s.id, s.name, s.severity ORDER BY c DESC, s.name",
                (rs, n) -> new ReportResponse.SeverityCount(rs.getString("name"), rs.getString("severity"), rs.getInt("c")));
    }

    /** Top 5 medicines with the most side effects */
    public List<ReportResponse.CountItem> mostSideEffects() {
        return jdbc.query(
                "SELECT m.brand_name AS name, COUNT(*) AS c FROM medicines m "
                        + "JOIN medicine_side_effects ms ON ms.medicine_id = m.id "
                        + "GROUP BY m.id, m.brand_name ORDER BY c DESC, m.brand_name LIMIT 5",
                (rs, n) -> new ReportResponse.CountItem(rs.getString("name"), rs.getInt("c")));
    }

    /** How many medicines contain each chemical */
    public List<ReportResponse.CountItem> medicinesPerIngredient() {
        return jdbc.query(
                "SELECT i.name, COUNT(*) AS c FROM ingredients i "
                        + "JOIN medicine_ingredients mi ON mi.ingredient_id = i.id "
                        + "GROUP BY i.id, i.name ORDER BY c DESC, i.name",
                (rs, n) -> new ReportResponse.CountItem(rs.getString("name"), rs.getInt("c")));
    }

    /** For each age group: how many medicines are safe, careful or avoid */
    public List<ReportResponse.AgeStatusCount> safetyByAge() {
        return jdbc.query(
                "SELECT age_group, status, COUNT(*) AS c FROM age_rules GROUP BY age_group, status",
                (rs, n) -> new ReportResponse.AgeStatusCount(rs.getString("age_group"), rs.getString("status"), rs.getInt("c")));
    }
}
