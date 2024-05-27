package com.skillstorm.taxservice.controllers;

import com.skillstorm.taxservice.dtos.DeductionDto;
import com.skillstorm.taxservice.services.DeductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deductions")
public class DeductionController {

    private final DeductionService deductionService;

    @Autowired
    public DeductionController(DeductionService deductionService) {
        this.deductionService = deductionService;
    }

    // Find Deduction by Deduction ID:
    @GetMapping("/{id}")
    public ResponseEntity<DeductionDto> findDeductionById(@PathVariable("id") int id) {
        return ResponseEntity.ok(deductionService.findById(id));
    }

    // Retrieve list of all Deductions:
    @GetMapping
    public ResponseEntity<List<DeductionDto>> findAllDeductions() {
        return ResponseEntity.ok(deductionService.findAll());
    }
}
