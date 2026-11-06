package com.drugproject;

import java.util.List;

/** The full answer for ONE medicine and ONE age. The frontend shows this. */
public record MedicineResult(
        int id,
        String name,
        String usedFor,
        List<IngredientInfo> ingredients,
        SafetyInfo safety,
        List<String> howToUse,
        List<SideEffectInfo> sideEffects) {

    /** A chemical inside the medicine, e.g. Paracetamol 650 mg */
    public record IngredientInfo(String name, String strength) {}

    /** Safety for the age group: status is safe, careful or avoid */
    public record SafetyInfo(String ageGroup, String status, String note,
                             String amount, String dailyLimit) {}

    /** One side effect: severity is mild, moderate or severe */
    public record SideEffectInfo(String name, String severity) {}
}
