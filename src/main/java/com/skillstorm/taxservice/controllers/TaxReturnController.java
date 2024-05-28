package com.skillstorm.taxservice.controllers;

import com.skillstorm.taxservice.dtos.RefundDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.UserDataDto;
import com.skillstorm.taxservice.services.TaxReturnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/taxreturns")
public class TaxReturnController {

    private final TaxReturnService taxReturnService;

    @Autowired
    public TaxReturnController(TaxReturnService taxReturnService) {
        this.taxReturnService = taxReturnService;
    }

    // Add new TaxReturn. Should at least contain the year and can be updated later. Can also contain all user info but a
    // POST must be done before filing W2s because the TaxReturn ID is needed to associate the W2s with the TaxReturn:
    @PostMapping
    public ResponseEntity<UserDataDto> addTaxReturn(@Valid @RequestBody UserDataDto newTaxReturn, @RequestHeader("User-ID") int userId) {
        newTaxReturn.setUserId(userId);
        UserDataDto createdTaxReturn = taxReturnService.addTaxReturn(newTaxReturn);
        return ResponseEntity.created(URI.create("/" + createdTaxReturn.getId())).body(createdTaxReturn);
    }

    // Get TaxReturn by id:
    @GetMapping("/{id}")
    public ResponseEntity<TaxReturnDto> findById(@PathVariable("id") int id, @RequestHeader("User-ID") int userId) {
        return ResponseEntity.ok(taxReturnService.findById(id, userId));
    }

    // Get current federal tax refund:
    @GetMapping("/{id}/refund")
    public ResponseEntity<RefundDto> getRefund(@PathVariable("id") int id, @RequestHeader("User-ID") int userId) {
        return ResponseEntity.ok(taxReturnService.getRefund(id, userId));
    }

    // Get all TaxReturns by userId (and optionally by year):
    @GetMapping
    public ResponseEntity<List<TaxReturnDto>> findAllByUserId(@RequestParam(value = "year", required = false) Integer year, @RequestHeader("User-ID") int userId) {
        if(year == null) {
            return ResponseEntity.ok(taxReturnService.findAllByUserId(userId));
        }
        return ResponseEntity.ok(taxReturnService.findAllByUserIdAndYear(userId, year));
    }

    // Update TaxReturn:
    @PutMapping("/{id}")
    @PreAuthorize("#userId == #updatedTaxReturn.userId")
    public ResponseEntity<UserDataDto> updateTaxReturn(@PathVariable("id") int id, @Valid @RequestBody TaxReturnDto updatedTaxReturn, @RequestHeader("User-ID") int userId) {
        return ResponseEntity.ok(taxReturnService.updateTaxReturn(id, updatedTaxReturn));
    }

    // Delete TaxReturn:
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaxReturn(@PathVariable("id") int id, @RequestHeader("User-ID") int userId) {
        taxReturnService.deleteTaxReturn(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Claim deductions:
    @PostMapping("/{id}/deductions")
    public ResponseEntity<TaxReturnDeductionDto> claimDeduction(@PathVariable("id") int id, @RequestBody TaxReturnDeductionDto deduction) {
        return ResponseEntity.ok(taxReturnService.claimDeduction(id, deduction));
    }

    // View a TaxReturnDeduction by ID:
    @GetMapping("taxreturn/deductions/{taxReturnDeductionId}")
    public ResponseEntity<TaxReturnDeductionDto> getTaxReturnDeductionById(@PathVariable("taxReturnDeductionId") int taxReturnDeductionId) {
        return ResponseEntity.ok(taxReturnService.getTaxReturnDeductionById(taxReturnDeductionId));
    }

    // Get all deductions for a TaxReturn:
    @GetMapping("/{id}/deductions")
    public ResponseEntity<List<TaxReturnDeductionDto>> getDeductions(@PathVariable("id") int id) {
        return ResponseEntity.ok(taxReturnService.getDeductions(id));
    }

    // Update a TaxReturnDeduction. This is used to update the amount spent on a deduction. We think it makes more sense
    // to only let the user update the amount spent. If they want to change the type of deduction, they should delete the
    // current deduction and add a new one:
    @PutMapping("taxreturn/deductions/{taxReturnDeductionId}")
    public ResponseEntity<TaxReturnDeductionDto> updateTaxReturnDeduction(@PathVariable("taxReturnDeductionId") int taxReturnDeductionId, @RequestBody TaxReturnDeductionDto amountSpent) {
        return ResponseEntity.ok(taxReturnService.updateTaxReturnDeduction(taxReturnDeductionId, amountSpent));
    }

    // Delete a TaxReturnDeduction:
    @DeleteMapping("taxreturn/deductions/{taxReturnDeductionId}")
    public ResponseEntity<Void> deleteTaxReturnDeduction(@PathVariable("taxReturnDeductionId") int taxReturnDeductionId) {
        taxReturnService.deleteTaxReturnDeduction(taxReturnDeductionId);
        return ResponseEntity.noContent().build();
    }

    // Delete absolutely everything associated with a UserId in the event they delete their account:
    @DeleteMapping("/deleteAll")
    public ResponseEntity<Void> deleteAllByUserId(@RequestHeader("userId") int userId) {
        taxReturnService.deleteAllByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    // View all possible Filing Statuses:
    @GetMapping("/filingStatuses")
    public ResponseEntity<List<String>> getFilingStatuses() {
        return ResponseEntity.ok(taxReturnService.getFilingStatuses());
    }
}
