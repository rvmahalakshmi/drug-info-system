package com.drugproject;

import java.util.List;

/** The data the Admin page sends to add or edit a medicine. */
public record MedicineForm(
        String name,
        String usedFor,
        List<IngredientForm> ingredients,
        List<SideEffectForm> sideEffects,
        List<String> howToUse,
        List<AgeRuleForm> ageRules) {

    public record IngredientForm(String name, String strength) {}

    public record SideEffectForm(String name, String severity) {}

    public record AgeRuleForm(String ageGroup, String status, String note,
                              String amount, String dailyLimit) {}
}
