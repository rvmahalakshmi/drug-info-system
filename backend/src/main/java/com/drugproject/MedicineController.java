package com.drugproject;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * These are the web links the frontend calls.
 * @CrossOrigin lets the frontend page (a different address) use them.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class MedicineController {

    private final MedicineService service;

    public MedicineController(MedicineService service) {
        this.service = service;
    }

    // Link 1:  /api/medicine?name=Dolo&age=30
    @GetMapping("/medicine")
    public SearchResponse search(@RequestParam("name") String name,
                                 @RequestParam("age") int age) {
        checkAge(age);
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please give a medicine or chemical name");
        }
        return service.search(name.trim(), age);
    }

    // Link 2:  /api/medicine/3?age=30   (after the user picks one from the list)
    @GetMapping("/medicine/{id}")
    public MedicineResult details(@PathVariable("id") int id,
                                  @RequestParam("age") int age) {
        checkAge(age);
        return service.details(id, age);
    }

    // Link 3:  /api/suggestions   (names for the search box)
    @GetMapping("/suggestions")
    public List<String> suggestions() {
        return service.suggestions();
    }

    private void checkAge(int age) {
        if (age < 1 || age > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 1 and 120");
        }
    }
}
