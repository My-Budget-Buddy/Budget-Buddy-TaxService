package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.constants.FilingStatus;
import com.skillstorm.taxservice.dtos.*;
import com.skillstorm.taxservice.exceptions.DuplicateDataException;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.exceptions.UnauthorizedException;
import com.skillstorm.taxservice.repositories.TaxReturnDeductionRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UserDataDto addTaxReturn(UserDataDto newTaxReturn) {
        try {
            return new UserDataDto(taxReturnRepository.saveAndFlush(newTaxReturn.mapToEntity()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDataException(environment.getProperty("taxreturn.duplicate.year"), newTaxReturn.getYear());
        }
    }

    // Get TaxReturn by id:
    @PostAuthorize("returnObject.userId == #userId")
    public TaxReturnDto findById(int id, int userId) {
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
    public UserDataDto updateTaxReturn(int id, TaxReturnDto updatedTaxReturn) {

        // Verify that the TaxReturn exists:
        TaxReturnDto oldTaxReturn = findById(id, updatedTaxReturn.getUserId());

        // Set the ID of the updated TaxReturn in case it was not set in the request body
        updatedTaxReturn.setId(id);

        // Set the W2s, Deductions, OtherIncome, and TaxCredit to match the db object
        // because they're not included in the request body and would be deleted if not set here:
        updatedTaxReturn.setW2s(oldTaxReturn.getW2s());
        updatedTaxReturn.setDeductions(oldTaxReturn.getDeductions());
        updatedTaxReturn.setOtherIncome(oldTaxReturn.getOtherIncome());
        updatedTaxReturn.setTaxCredit(oldTaxReturn.getTaxCredit());

        // Save the updated TaxReturn to the database:
        return new UserDataDto(taxReturnRepository.saveAndFlush(updatedTaxReturn.mapToEntity()));
    }

    // Delete TaxReturn by id:
    public void deleteTaxReturn(int id, int userId) {
        // Verify that the TaxReturn exists:
        TaxReturnDto taxReturnDto = findById(id, userId);
        if (userId != taxReturnDto.getUserId()) {
            throw new UnauthorizedException(environment.getProperty("user.unauthorized"));
        }
        taxReturnRepository.deleteById(id);
    }

    // Claim a deduction for a TaxReturn:
    public TaxReturnDeductionDto claimDeduction(int id, TaxReturnDeductionDto deduction) {
        deduction.setTaxReturn(id);
        try {
            return new TaxReturnDeductionDto(taxReturnDeductionRepository.saveAndFlush(deduction.mapToEntity()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateDataException(environment.getProperty("taxreturn.duplicate.deduction"), deduction.getDeductionName());
        }
    }

    // Get the current tax refund for a TaxReturn. Used for front end to keep a running total of the refund amount
    // without having to pass the entire TaxReturn object back and forth between the front end and back end:
    public RefundDto getRefund(int id, int userId) {
        TaxReturnDto taxReturnDto = findById(id, userId);
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

    // Get all filing statuses::
    public List<String> getFilingStatuses() {
        return FilingStatus.getFilingStatuses();
    }
}
