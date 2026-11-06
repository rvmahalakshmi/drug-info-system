package com.drugproject;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Web link for the Reports page:  /api/reports */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ReportController {

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/reports")
    public ReportResponse reports() {
        return new ReportResponse(
                repo.totals(),
                repo.commonSideEffects(),
                repo.mostSideEffects(),
                repo.medicinesPerIngredient(),
                repo.safetyByAge());
    }
}
