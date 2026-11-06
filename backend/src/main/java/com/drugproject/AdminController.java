package com.drugproject;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drugproject.MedicineRepository.MedicineRow;

/** Web links for the Admin page: list, add, edit, delete. */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    /** The answer after add, edit or delete */
    public record Saved(int id, String message) {}

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/medicines")
    public List<MedicineRow> list() {
        return service.list();
    }

    @GetMapping("/medicines/{id}")
    public MedicineForm get(@PathVariable("id") int id) {
        return service.getForEdit(id);
    }

    @PostMapping("/medicines")
    public Saved create(@RequestBody MedicineForm form) {
        int id = service.create(form);
        return new Saved(id, "Medicine added");
    }

    @PutMapping("/medicines/{id}")
    public Saved update(@PathVariable("id") int id, @RequestBody MedicineForm form) {
        service.update(id, form);
        return new Saved(id, "Medicine updated");
    }

    @DeleteMapping("/medicines/{id}")
    public Saved delete(@PathVariable("id") int id) {
        service.delete(id);
        return new Saved(id, "Medicine deleted");
    }
}
