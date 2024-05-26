package com.skillstorm.taxservice.controllers;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.taxcredits.EarnedIncomeTaxCredit;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditAotc;
import com.skillstorm.taxservice.models.taxcredits.EducationTaxCreditLlc;
import com.skillstorm.taxservice.models.taxcredits.SaversTaxCredit;
import com.skillstorm.taxservice.services.TaxCreditService;

@RestController
@RequestMapping("/credits")
public class TaxCreditController {
  
  @Autowired
  private TaxCreditService taxCreditService;

  @GetMapping("/child-tax-credit/{id}")
  public ResponseEntity<?> getChildTaxCreditById(@PathVariable int id) {
    return ResponseEntity.ok(taxCreditService.getChildTaxCreditById(id));
  }

  @GetMapping("/earned-income-tax-credit/{id}")
  public ResponseEntity<?> getEarnedIncomeTaxCreditById(@PathVariable int id) {
    try {
      EarnedIncomeTaxCredit credit = taxCreditService.getEarnedIncomeTaxCreditById(id);
      return ResponseEntity.ok(credit);
    } catch (NotFoundException e) {
      return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/education-tax-credit-aotc/{id}")
  public ResponseEntity<?> getEducationTaxCreditAotcById(@PathVariable int id) {
    try {
      EducationTaxCreditAotc credit = taxCreditService.getEducationTaxCreditAotcById(id);
      return ResponseEntity.ok(credit);
    } catch (NotFoundException e) {
      return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/education-tax-credit-llc/{id}")
  public ResponseEntity<?> getEducationTaxCreditLlcById(@PathVariable int id) {
    try {
      EducationTaxCreditLlc credit = taxCreditService.getEducationTaxCreditLlcById(id);
      return ResponseEntity.ok(credit);
    } catch (NotFoundException e) {
      return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
    }
  }

  @GetMapping("/savers-tax-credit/{id}")
  public ResponseEntity<?> getSaversTaxCreditById(@PathVariable int id) {
    try {
      SaversTaxCredit credit = taxCreditService.getSaversTaxCreditById(id);
      return ResponseEntity.ok(credit);
    } catch (NotFoundException e) {
      return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
    }
  }
}
