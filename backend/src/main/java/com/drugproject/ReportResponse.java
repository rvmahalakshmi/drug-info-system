package com.drugproject;

import java.util.List;

/** Everything shown on the Reports page. */
public record ReportResponse(
        Totals totals,
        List<SeverityCount> commonSideEffects,
        List<CountItem> mostSideEffects,
        List<CountItem> medicinesPerIngredient,
        List<AgeStatusCount> safetyByAge) {

    public record Totals(int medicines, int ingredients, int sideEffects) {}

    public record SeverityCount(String name, String severity, int count) {}

    public record CountItem(String name, int count) {}

    public record AgeStatusCount(String ageGroup, String status, int count) {}
}
