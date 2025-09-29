package com.airfranceklm.fasttrack.assignment.controller;




import com.airfranceklm.fasttrack.assignment .model.Holiday;
import com.airfranceklm.fasttrack.assignment.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {


    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }


    @GetMapping
    public List<Holiday> list(@RequestParam(required = false) String employeeId) {
        return holidayService.list(employeeId);
    }


    @GetMapping("/{id}")
    public Holiday get(@PathVariable UUID id) {
        return holidayService.get(id);
    }


    @PostMapping
    public ResponseEntity<Holiday> create(@Valid @RequestBody Holiday holiday) {
        Holiday created = holidayService.create(holiday);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{id}")
    public Holiday update(@PathVariable UUID id, @Valid @RequestBody Holiday holiday) {
        return holidayService.update(id, holiday);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        holidayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
