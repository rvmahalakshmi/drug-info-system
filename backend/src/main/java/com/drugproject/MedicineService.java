package com.drugproject;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.drugproject.MedicineRepository.MedicineRow;

/** This file holds the main logic: search, then build the answer for the age. */
@Service
public class MedicineService {

    private final MedicineRepository repo;

    public MedicineService(MedicineRepository repo) {
        this.repo = repo;
    }

    /** child = under 18, adult = 18 to 59, elderly = 60 and above */
    public static String ageGroup(int age) {
        if (age < 18) return "child";
        if (age < 60) return "adult";
        return "elderly";
    }

    /** Search by brand or chemical name. */
    public SearchResponse search(String text, int age) {
        List<MedicineRow> rows = repo.findMatches(text);

        if (rows.isEmpty()) {
            return new SearchResponse("NOT_FOUND", null, List.of());
        }
        if (rows.size() == 1) {
            return new SearchResponse("FOUND", buildResult(rows.get(0), age), List.of());
        }
        List<SearchResponse.MatchInfo> matches = rows.stream()
                .map(r -> new SearchResponse.MatchInfo(r.id(), r.name(), ingredientsText(r.id())))
                .toList();
        return new SearchResponse("MULTIPLE", null, matches);
    }

    /** Full details for one medicine id (used after the user picks from the list). */
    public MedicineResult details(int id, int age) {
        MedicineRow row = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));
        return buildResult(row, age);
    }

    public List<String> suggestions() {
        return repo.findAllNames();
    }

    // ---------- helper methods ----------

    private MedicineResult buildResult(MedicineRow row, int age) {
        String group = ageGroup(age);

        // If no age rule is saved, give a safe default message.
        MedicineResult.SafetyInfo safety = repo.findSafety(row.id(), group).orElse(
                new MedicineResult.SafetyInfo(group, "careful",
                        "No age rule is saved for this medicine. Ask a doctor.",
                        "Ask a doctor or pharmacist", "Not decided here"));

        return new MedicineResult(
                row.id(),
                row.name(),
                row.usedFor(),
                repo.findIngredients(row.id()),
                safety,
                repo.findSteps(row.id()),
                repo.findSideEffects(row.id()));
    }

    private String ingredientsText(int medicineId) {
        return repo.findIngredients(medicineId).stream()
                .map(i -> i.name() + " " + i.strength())
                .collect(Collectors.joining(" + "));
    }
}
