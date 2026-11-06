package com.drugproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.drugproject.MedicineRepository.MedicineRow;

/** Admin logic: check the form, then add, change or delete a medicine. */
@Service
public class AdminService {

    private static final Set<String> GROUPS = Set.of("child", "adult", "elderly");
    private static final Set<String> STATUSES = Set.of("safe", "careful", "avoid");
    private static final Set<String> SEVERITIES = Set.of("mild", "moderate", "severe");

    private final AdminRepository admin;
    private final MedicineRepository repo;

    public AdminService(AdminRepository admin, MedicineRepository repo) {
        this.admin = admin;
        this.repo = repo;
    }

    public List<MedicineRow> list() {
        return admin.findAll();
    }

    /** All the data of one medicine, to fill the edit form. */
    public MedicineForm getForEdit(int id) {
        MedicineRow row = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));
        return new MedicineForm(
                row.name(),
                row.usedFor(),
                repo.findIngredients(id).stream()
                        .map(i -> new MedicineForm.IngredientForm(i.name(), i.strength())).toList(),
                repo.findSideEffects(id).stream()
                        .map(s -> new MedicineForm.SideEffectForm(s.name(), s.severity())).toList(),
                repo.findSteps(id),
                admin.findAgeRules(id));
    }

    @Transactional
    public int create(MedicineForm form) {
        MedicineForm f = clean(form);
        try {
            int id = admin.insertMedicine(f.name(), f.usedFor());
            saveChildren(id, f);
            return id;
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A medicine with this name already exists");
        }
    }

    @Transactional
    public void update(int id, MedicineForm form) {
        repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found"));
        MedicineForm f = clean(form);
        try {
            admin.updateMedicine(id, f.name(), f.usedFor());
            admin.deleteChildren(id);
            saveChildren(id, f);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A medicine with this name already exists");
        }
    }

    @Transactional
    public void delete(int id) {
        if (admin.deleteMedicine(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found");
        }
    }

    // ---------- helper methods ----------

    private void saveChildren(int id, MedicineForm f) {
        for (MedicineForm.IngredientForm i : f.ingredients()) {
            int ingredientId = admin.getOrCreateIngredient(i.name());
            admin.linkIngredient(id, ingredientId, i.strength());
        }
        for (MedicineForm.SideEffectForm s : f.sideEffects()) {
            int sideEffectId = admin.getOrCreateSideEffect(s.name(), s.severity());
            admin.linkSideEffect(id, sideEffectId);
        }
        int stepNo = 1;
        for (String step : f.howToUse()) {
            admin.insertStep(id, stepNo++, step);
        }
        for (MedicineForm.AgeRuleForm r : f.ageRules()) {
            admin.insertAgeRule(id, r);
        }
    }

    /** Check every field and return a clean copy. Wrong data gives a clear error message. */
    private MedicineForm clean(MedicineForm f) {
        if (f == null) {
            throw bad("The form is empty");
        }
        String name = req(f.name(), "Medicine name", 100);
        String usedFor = req(f.usedFor(), "Used for", 200);

        List<MedicineForm.IngredientForm> ingredients = new ArrayList<>();
        if (f.ingredients() != null) {
            for (MedicineForm.IngredientForm i : f.ingredients()) {
                if (i == null) continue;
                ingredients.add(new MedicineForm.IngredientForm(
                        req(i.name(), "Ingredient name", 100), opt(i.strength(), "Strength", 30)));
            }
        }
        if (ingredients.isEmpty()) {
            throw bad("Add at least one ingredient");
        }

        List<MedicineForm.SideEffectForm> effects = new ArrayList<>();
        if (f.sideEffects() != null) {
            for (MedicineForm.SideEffectForm s : f.sideEffects()) {
                if (s == null) continue;
                String severity = req(s.severity(), "Severity", 20).toLowerCase();
                if (!SEVERITIES.contains(severity)) {
                    throw bad("Severity must be mild, moderate or severe");
                }
                effects.add(new MedicineForm.SideEffectForm(req(s.name(), "Side effect name", 100), severity));
            }
        }

        List<String> steps = new ArrayList<>();
        if (f.howToUse() != null) {
            for (String step : f.howToUse()) {
                if (step == null || step.isBlank()) continue;
                steps.add(req(step, "How to use step", 300));
            }
        }

        List<MedicineForm.AgeRuleForm> rules = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (f.ageRules() != null) {
            for (MedicineForm.AgeRuleForm r : f.ageRules()) {
                if (r == null) continue;
                String group = req(r.ageGroup(), "Age group", 20).toLowerCase();
                String status = req(r.status(), "Safety status", 20).toLowerCase();
                if (!GROUPS.contains(group)) {
                    throw bad("Age group must be child, adult or elderly");
                }
                if (!STATUSES.contains(status)) {
                    throw bad("Safety status must be safe, careful or avoid");
                }
                if (!seen.add(group)) {
                    throw bad("Age group repeated: " + group);
                }
                rules.add(new MedicineForm.AgeRuleForm(group, status,
                        req(r.note(), "Safety note (" + group + ")", 300),
                        req(r.amount(), "Amount (" + group + ")", 200),
                        req(r.dailyLimit(), "Daily limit (" + group + ")", 200)));
            }
        }
        return new MedicineForm(name, usedFor, ingredients, effects, steps, rules);
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    // A required text: cannot be empty, cannot be too long
    private static String req(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            throw bad(label + " is required");
        }
        String t = value.trim();
        if (t.length() > max) {
            throw bad(label + " is too long (maximum " + max + " characters)");
        }
        return t;
    }

    // An optional text: can be empty, cannot be too long
    private static String opt(String value, String label, int max) {
        String t = value == null ? "" : value.trim();
        if (t.length() > max) {
            throw bad(label + " is too long (maximum " + max + " characters)");
        }
        return t;
    }
}
