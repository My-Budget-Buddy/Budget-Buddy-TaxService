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

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.services.TaxReturnCreditService;

@RestController
@RequestMapping("/tax-return-credit")
public class TaxReturnCreditController {

  private final TaxReturnCreditService taxReturnCreditService;

  public TaxReturnCreditController(TaxReturnCreditService taxReturnCreditService) {
    this.taxReturnCreditService = taxReturnCreditService;
  }

  @GetMapping("/{taxReturnId}")
  public ResponseEntity<TaxReturnCreditDto> findByTaxReturnId(@PathVariable int taxReturnId) {
    return ResponseEntity.ok(taxReturnCreditService.findByTaxReturnId(taxReturnId));
  }

  @PostMapping()
  public ResponseEntity<TaxReturnCreditDto> createTaxReturnCredit(@RequestBody TaxReturnCreditDto taxReturnCreditDto) {
    return ResponseEntity.ok(taxReturnCreditService.createTaxReturnCredit(taxReturnCreditDto));
  }

  @PutMapping()
  public ResponseEntity<TaxReturnCreditDto> updateTaxReturnCredit(@RequestBody TaxReturnCreditDto taxReturnCreditDto) {
    return ResponseEntity.ok(taxReturnCreditService.updateTaxReturnCredit(taxReturnCreditDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTaxReturnCredit(@PathVariable("id") int id) {
    taxReturnCreditService.deleteTaxReturnCredit(id);
    return ResponseEntity.noContent().build();
  }
}
