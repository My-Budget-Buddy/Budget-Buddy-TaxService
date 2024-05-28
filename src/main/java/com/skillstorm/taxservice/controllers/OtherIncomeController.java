package com.skillstorm.taxservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.services.OtherIncomeService;

@RestController
@RequestMapping("/other-income")
public class OtherIncomeController {
  
  private final OtherIncomeService otherIncomeService;

  public OtherIncomeController(OtherIncomeService otherIncomeService) {
    this.otherIncomeService = otherIncomeService;
  }

  @GetMapping("/{taxReturnId}")
  public ResponseEntity<OtherIncomeDto> findByTaxReturnId(@PathVariable int taxReturnId) {
    return ResponseEntity.ok(otherIncomeService.findByTaxReturnId(taxReturnId));
  }

  @PostMapping()
  public ResponseEntity<OtherIncomeDto> addOtherIncome(@RequestBody OtherIncomeDto otherIncomeDto) {
    return ResponseEntity.ok(otherIncomeService.createOtherIncome(otherIncomeDto));
  }

  @PutMapping()
  public ResponseEntity<OtherIncomeDto> updateOtherIncome(@RequestBody OtherIncomeDto otherIncomeDto) {
    return ResponseEntity.ok(otherIncomeService.updateOtherIncome(otherIncomeDto));
  }

  @DeleteMapping()
  public ResponseEntity<Void> deleteOtherIncome(@RequestBody OtherIncomeDto otherIncomeDto) {
    otherIncomeService.deleteOtherIncome(otherIncomeDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{otherIncomeId}")
  public ResponseEntity<Void> deleteOtherIncomeById(@PathVariable int otherIncomeId) {
    otherIncomeService.deleteOtherIncomeById(otherIncomeId);
    return ResponseEntity.noContent().build();
  }
}
