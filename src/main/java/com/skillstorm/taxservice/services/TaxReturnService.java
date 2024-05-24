package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.dtos.RefundDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.repositories.TaxReturnDeductionRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@PropertySource("classpath:SystemMessages.properties")
public class TaxReturnService {

    private final TaxReturnRepository taxReturnRepository;
    private final TaxReturnDeductionRepository taxReturnDeductionRepository;
    private final TaxCalculatorService taxCalculatorService;
    private final Environment environment;

    @Autowired
    public TaxReturnService(TaxReturnRepository taxReturnRepository, TaxReturnDeductionRepository taxReturnDeductionRepository,
                            TaxCalculatorService taxCalculatorService, Environment environment) {
        this.taxReturnRepository = taxReturnRepository;
        this.taxReturnDeductionRepository = taxReturnDeductionRepository;
        this.taxCalculatorService = taxCalculatorService;
        this.environment = environment;
    }

    // Add new TaxReturn:
    public TaxReturnDto addTaxReturn(TaxReturnDto newTaxReturn) {
        taxCalculatorService.calculateAll(newTaxReturn);
        return new TaxReturnDto(taxReturnRepository.saveAndFlush(newTaxReturn.mapToEntity()));
    }

    // Get TaxReturn by id:
    public TaxReturnDto findById(int id) {
        TaxReturnDto taxReturnDto = new TaxReturnDto(taxReturnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(environment.getProperty("taxreturn.not.found") + " " + id)));
        taxCalculatorService.calculateAll(taxReturnDto);
        return taxReturnDto;
    }

    // Find all TaxReturns by userId:
    public List<TaxReturnDto> findAllByUserId(int userId) {
        return taxReturnRepository.findAllByUserId(userId)
                .stream().map(TaxReturnDto::new).toList();
    }

    // Find all TaxReturns by userId and year:
    public List<TaxReturnDto> findAllByUserIdAndYear(int userId, int year) {
        return taxReturnRepository.findAllByUserIdAndYear(userId, year)
                .stream().map(TaxReturnDto::new).toList();
    }

    // Update TaxReturn. Just the User Info. Other fields are determined by its components:
    public TaxReturnDto updateTaxReturn(int id, TaxReturnDto updatedTaxReturn) {
        // Verify that the TaxReturn exists:
        findById(id);

        // Set the ID of the updated TaxReturn in case it was not set in the request body:
        updatedTaxReturn.setId(id);

        // Save the updated TaxReturn to the database:
        return new TaxReturnDto(taxReturnRepository.saveAndFlush(updatedTaxReturn.mapToEntity()));
    }

    // Delete TaxReturn:
    public void deleteTaxReturn(int id) {
        // Verify that the TaxReturn exists:
        findById(id);
        taxReturnRepository.deleteById(id);
    }

    // Calculate the Tax Refund for a TaxReturn:
    private void calculateRefundAmount(TaxReturnDto taxReturn) {

        // Set financial values for the TaxReturn:
        setFinancialValues(taxReturn);

        // With all of our fields set, let's run the TaxCalculator. Placeholder for now:
        taxCalculatorService.calculateAll(taxReturn);
        taxReturn.setFederalRefund(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        // Also calculate the state refund. Placeholder for now:
        taxReturn.setStateRefund(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // Populate the monetary values for a TaxReturn:
    private void setFinancialValues(TaxReturnDto taxReturn) {
        // Calculate the total income for the TaxReturn:
        taxReturn.setTotalIncome(getTotalIncome(taxReturn));

        // Calculate all taxes withheld for the TaxReturn:
        calculateTaxesWithheld(taxReturn);

        // Calculate total credits and deductions for the TaxReturn:
        //calculateCreditsAndDeductions(taxReturn);

        // Calculate the adjusted gross income for the TaxReturn:
        //calculateAgi(taxReturn);

        // Calculate the taxable income for the TaxReturn:
        //calculateTaxableIncome(taxReturn);
    }

    // Calculate the taxable income for a TaxReturn:
    private void calculateTaxableIncome(TaxReturnDto taxReturn) {
        // Placeholder for now:
        taxReturn.setTaxableIncome(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // Calculate the adjusted gross income for a TaxReturn:
    private void calculateAgi(TaxReturnDto taxReturn) {
        // Placeholder for now:
        taxReturn.setAdjustedGrossIncome(taxReturn
                .getTotalIncome()
                .subtract(taxReturn.getDeductions().stream().map(TaxReturnDeductionDto::getNetDeduction)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .setScale(2, RoundingMode.HALF_UP));
    }

    // Calculate the total credits and deductions for a TaxReturn:
    private void calculateCreditsAndDeductions(TaxReturnDto taxReturn) {
        // Will need to sum up all the TaxReturnCredits associated with the TaxReturn. Placeholder for now:
        taxReturn.setTotalCredits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        // Will need to sum up all the TaxReturnDeductions associated with the TaxReturn. Placeholder for now:
        //taxReturn.setTotalDeductions(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    // Calculate the total of all taxes already withheld for a TaxReturn:
    private void calculateTaxesWithheld(TaxReturnDto taxReturn) {
        taxReturn.setFedTaxWithheld(getFederalTaxesWithheld(taxReturn));
        taxReturn.setStateTaxWithheld(getStateTaxesWithheld(taxReturn));
        taxReturn.setSocialSecurityTaxWithheld(getSocialSecurityTaxesWithheld(taxReturn));
        taxReturn.setMedicareTaxWithheld(getMedicareTaxesWithheld(taxReturn));
    }

    // Calculate total medicare taxes already withheld for a TaxReturn:
    private BigDecimal getMedicareTaxesWithheld(TaxReturnDto taxReturn) {
        return taxReturn.getW2s().stream().map(W2Dto::getMedicareTaxWithheld)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total social security taxes already withheld for a TaxReturn:
    private BigDecimal getSocialSecurityTaxesWithheld(TaxReturnDto taxReturn) {
        return taxReturn.getW2s().stream().map(W2Dto::getSocialSecurityTaxWithheld)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total state income taxes already withheld for a TaxReturn:
    private BigDecimal getStateTaxesWithheld(TaxReturnDto taxReturn) {
        return taxReturn.getW2s().stream().map(W2Dto::getStateIncomeTaxWithheld)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total federal income taxes already withheld for a TaxReturn:
    private BigDecimal getFederalTaxesWithheld(TaxReturnDto taxReturn) {
        return taxReturn.getW2s().stream().map(W2Dto::getFederalIncomeTaxWithheld)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Iterate through the W2s and sum the wages to get the total income:
    private BigDecimal getTotalIncome(TaxReturnDto taxReturn) {
        return taxReturn.getW2s().stream().map(W2Dto::getWages)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Claim a deductions for a TaxReturn:
    public TaxReturnDeductionDto claimDeduction(int id, TaxReturnDeductionDto deduction) {
        deduction.setTaxReturn(id);
        return new TaxReturnDeductionDto(taxReturnDeductionRepository.saveAndFlush(deduction.mapToEntity()));

    }

    // Get the current tax refund for a TaxReturn:
    public RefundDto getRefund(int id) {
        TaxReturnDto taxReturnDto = findById(id);
        return new RefundDto(taxReturnDto.getFederalRefund(), taxReturnDto.getStateRefund());
    }

    // Get a TaxReturnDeduction by ID:
    public TaxReturnDeductionDto getTaxReturnDeductionById(int taxReturnDeductionId) {
        return new TaxReturnDeductionDto(taxReturnDeductionRepository.findById(taxReturnDeductionId)
                .orElseThrow(() -> new NotFoundException(environment.getProperty("taxreturn.deduction.not.found") + " " + taxReturnDeductionId)));
    }

    // Get all deductions for a TaxReturn:
    public List<TaxReturnDeductionDto> getDeductions(int taxReturnId) {
        return taxReturnDeductionRepository.findAllByTaxReturnId(taxReturnId)
                .stream().map(TaxReturnDeductionDto::new).toList();
    }

    // Update a TaxReturnDeduction:
    public TaxReturnDeductionDto updateTaxReturnDeduction(int taxReturnDeductionId, TaxReturnDeductionDto updatedDeduction) {
        // Verify that the TaxReturnDeduction exists:
        getTaxReturnDeductionById(taxReturnDeductionId);

        // Set the ID of the updated TaxReturnDeduction in case it was not set in the request body:
        updatedDeduction.setId(taxReturnDeductionId);

        // Save the updated TaxReturnDeduction to the database:
        return new TaxReturnDeductionDto(taxReturnDeductionRepository.saveAndFlush(updatedDeduction.mapToEntity()));
    }

    // Delete a TaxReturnDeduction:
    public void deleteTaxReturnDeduction(int taxReturnDeductionId) {
        // Verify that the TaxReturnDeduction exists:
        getTaxReturnDeductionById(taxReturnDeductionId);
        taxReturnDeductionRepository.deleteById(taxReturnDeductionId);
    }

    // Clean up all entities associated with a User when they delete their account:
    @Transactional
    public void deleteAllByUserId(int userId) {
        taxReturnRepository.deleteAllByUserId(userId);
    }

    @Transactional
    @RabbitListener(queues = "${queues.fanout}")
    public void receiveDeleteAllByUserId(@Payload int userId) {
      taxReturnRepository.deleteAllByUserId(userId);
    }
}
