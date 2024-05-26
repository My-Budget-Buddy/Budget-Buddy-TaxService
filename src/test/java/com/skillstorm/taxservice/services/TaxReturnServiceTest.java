package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.constants.FilingStatus;
import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.dtos.RefundDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.UserDataDto;
import com.skillstorm.taxservice.exceptions.DuplicateDataException;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.exceptions.UnauthorizedException;
import com.skillstorm.taxservice.models.Deduction;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.models.TaxReturnDeduction;
import com.skillstorm.taxservice.repositories.TaxReturnDeductionRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class TaxReturnServiceTest {

    @InjectMocks private static TaxReturnService taxReturnService;

    @Mock private static TaxReturnRepository taxReturnRepository;
    @Mock private static TaxReturnDeductionRepository taxReturnDeductionRepository;
    @Mock private static TaxCalculatorService taxCalculatorService;
    @Spy private static Environment environment;

    private static UserDataDto newTaxReturnRequest;
    private static TaxReturn returnedNewTaxReturn;
    private static TaxReturnDto updatedTaxReturn;

    private static Deduction claimedDeduction;
    private static TaxReturnDeductionDto deductionRequest;
    private static TaxReturnDeduction returnedDeduction;

    @BeforeEach
    void setUp() {
        taxReturnService = new TaxReturnService(taxReturnRepository, taxReturnDeductionRepository, taxCalculatorService, environment);

        setupTaxReturns();
        setupDeductions();
    }

    private void setupDeductions() {
        claimedDeduction = new Deduction();
        claimedDeduction.setId(1);
        claimedDeduction.setName("TestDeduction");
        claimedDeduction.setItemized(false);
        claimedDeduction.setAgiLimit(BigDecimal.valueOf(3850.000));

        deductionRequest = new TaxReturnDeductionDto();
        deductionRequest.setId(1);
        deductionRequest.setTaxReturn(1);
        deductionRequest.setDeduction(1);
        deductionRequest.setAmountSpent(BigDecimal.valueOf(1000.00));

        returnedDeduction = new TaxReturnDeduction();
        returnedDeduction.setId(1);
        returnedDeduction.setTaxReturn(new TaxReturn(1));
        returnedDeduction.setDeduction(claimedDeduction);
        returnedDeduction.setAmountSpent(BigDecimal.valueOf(1000.00));
    }

    private void setupTaxReturns() {
        // Define the data being sent from the request:
        newTaxReturnRequest = new UserDataDto();
        newTaxReturnRequest.setYear(2024);
        newTaxReturnRequest.setUserId(1);
        newTaxReturnRequest.setFilingStatus(FilingStatus.SINGLE.toString());
        newTaxReturnRequest.setFirstName("TestFirstName");
        newTaxReturnRequest.setLastName("TestLastName");
        newTaxReturnRequest.setEmail("TestEmail");
        newTaxReturnRequest.setAddress("TestAddress");
        newTaxReturnRequest.setCity("TestCity");
        newTaxReturnRequest.setState(State.FL);
        newTaxReturnRequest.setZip("TestZipCode");
        newTaxReturnRequest.setDateOfBirth("2024-01-01");
        newTaxReturnRequest.setSsn("123-45-6789");

        // Define the data being returned from the repository:
        returnedNewTaxReturn = new TaxReturn();
        returnedNewTaxReturn.setId(1);
        returnedNewTaxReturn.setYear(2024);
        returnedNewTaxReturn.setUserId(1);
        returnedNewTaxReturn.setFilingStatus(1);
        returnedNewTaxReturn.setFirstName("TestFirstName");
        returnedNewTaxReturn.setLastName("TestLastName");
        returnedNewTaxReturn.setEmail("TestEmail");
        returnedNewTaxReturn.setAddress("TestAddress");
        returnedNewTaxReturn.setCity("TestCity");
        returnedNewTaxReturn.setState(State.FL);
        returnedNewTaxReturn.setZip("TestZipCode");
        returnedNewTaxReturn.setDateOfBirth(LocalDate.parse("2024-01-01"));
        returnedNewTaxReturn.setSsn("123-45-6789");

        updatedTaxReturn = new TaxReturnDto();
        updatedTaxReturn.setId(1);
        updatedTaxReturn.setYear(2024);
        updatedTaxReturn.setUserId(1);
        updatedTaxReturn.setFilingStatus(FilingStatus.SINGLE);
        updatedTaxReturn.setFirstName("TestFirstName");
        updatedTaxReturn.setLastName("TestLastName");
        updatedTaxReturn.setEmail("TestEmail");
        updatedTaxReturn.setAddress("TestAddress");
        updatedTaxReturn.setCity("TestCity");
        updatedTaxReturn.setState(State.FL);
        updatedTaxReturn.setZip("TestZipCode");
        updatedTaxReturn.setDateOfBirth("2024-01-01");
        updatedTaxReturn.setSsn("123-45-6789");
    }

    // Add new TaxReturn Success:
    @Test
    void addTaxReturn() {

        // Define stubbing:
        when(taxReturnRepository.saveAndFlush(newTaxReturnRequest.mapToEntity())).thenReturn(returnedNewTaxReturn);

        // Call the method to be tested:
        UserDataDto result = taxReturnService.addTaxReturn(newTaxReturnRequest);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturn ID should be 1.");
        assertEquals(2024, result.getYear(), "The TaxReturn year should be 2024.");
        assertEquals(1, result.getUserId(), "The TaxReturn user ID should be 1.");
        assertEquals(FilingStatus.SINGLE.toString(), result.getFilingStatus(), "The TaxReturn filing status should be Single.");
        assertEquals("TestFirstName", result.getFirstName(), "The TaxReturn first name should be TestFirstName.");
        assertEquals("TestLastName", result.getLastName(), "The TaxReturn last name should be TestLastName.");
        assertEquals("TestEmail", result.getEmail(), "The TaxReturn email should be TestEmail.");
        assertEquals("TestAddress", result.getAddress(), "The TaxReturn address should be TestAddress.");
        assertEquals("TestCity", result.getCity(), "The TaxReturn city should be TestCity.");
        assertEquals(State.FL, result.getState(), "The TaxReturn state should be Florida.");
        assertEquals("TestZipCode", result.getZip(), "The TaxReturn zip code should be TestZipCode.");
        assertEquals("2024-01-01", result.getDateOfBirth(), "The TaxReturn date of birth should be 2024-01-01.");
        assertEquals("123-45-6789", result.getSsn(), "The TaxReturn SSN should be 123-45-6789.");
    }

    // Add new TaxReturn Duplicate Year:
    @Test
    void addTaxReturnDuplicateYearException() {

        // Define stubbing:
        when(taxReturnRepository.saveAndFlush(newTaxReturnRequest.mapToEntity())).thenThrow(new DuplicateDataException("Duplicate year", 2024));

        // Verify the exception
        assertThrows(DuplicateDataException.class, () -> taxReturnService.addTaxReturn(newTaxReturnRequest), "DuplicateDataException should be thrown.");
    }

    // Get TaxReturn by id success:
    @Test
    void findByIdSuccess() {

        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(returnedNewTaxReturn));

        // Call the method to be tested:
        TaxReturnDto result = taxReturnService.findById(1, 1);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturn ID should be 1.");
        assertEquals(2024, result.getYear(), "The TaxReturn year should be 2024.");
        assertEquals(1, result.getUserId(), "The TaxReturn user ID should be 1.");
        assertEquals(FilingStatus.SINGLE, result.getFilingStatus(), "The TaxReturn filing status should be Single.");
        assertEquals("TestFirstName", result.getFirstName(), "The TaxReturn first name should be TestFirstName.");
        assertEquals("TestLastName", result.getLastName(), "The TaxReturn last name should be TestLastName.");
        assertEquals("TestEmail", result.getEmail(), "The TaxReturn email should be TestEmail.");
        assertEquals("TestAddress", result.getAddress(), "The TaxReturn address should be TestAddress.");
        assertEquals("TestCity", result.getCity(), "The TaxReturn city should be TestCity.");
        assertEquals(State.FL, result.getState(), "The TaxReturn state should be Florida.");
        assertEquals("TestZipCode", result.getZip(), "The TaxReturn zip code should be TestZipCode.");
        assertEquals("2024-01-01", result.getDateOfBirth(), "The TaxReturn date of birth should be 2024-01-01.");
        assertEquals("123-45-6789", result.getSsn(), "The TaxReturn SSN should be 123-45-6789.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalIncome(), "The TaxReturn total income should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getAdjustedGrossIncome(), "The TaxReturn adjusted gross income should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTaxableIncome(), "The TaxReturn taxable income should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getFedTaxWithheld(), "The TaxReturn federal tax withheld should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getStateTaxWithheld(), "The TaxReturn state tax withheld should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getSocialSecurityTaxWithheld(), "The TaxReturn social security tax withheld should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getMedicareTaxWithheld(), "The TaxReturn medicare tax withheld should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getTotalCredits(), "The TaxReturn total credits should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getFederalRefund(), "The TaxReturn federal refund should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getStateRefund(), "The TaxReturn state refund should be 0.00.");
    }

    // Get TaxReturn by id failure:
    @Test
    void findByIdFailure() {

        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.empty());

        // Verify the exception
        assertThrows(NotFoundException.class, () -> taxReturnService.findById(1, 1), "NotFoundException should be thrown.");
    }

    // Find all TaxReturns by userId:
    @Test
    void findAllByUserId() {

        // Define stubbing:
        when(taxReturnRepository.findAllByUserId(1)).thenReturn(List.of(returnedNewTaxReturn));

        // Call the method to be tested:
        List<TaxReturnDto> result = taxReturnService.findAllByUserId(1);

        // Verify the result:
        assertEquals(1, result.size(), "The size of the list should be 1.");
        assertEquals(1, result.get(0).getId(), "The TaxReturn ID should be 1.");
        assertEquals(2024, result.get(0).getYear(), "The TaxReturn year should be 2024.");
        assertEquals(1, result.get(0).getUserId(), "The TaxReturn user ID should be 1.");
    }

    // Find all TaxReturns by userId and year:
    @Test
    void findAllByUserIdAndYear() {

        // Define stubbing:
        when(taxReturnRepository.findAllByUserIdAndYear(1, 2024)).thenReturn(List.of(returnedNewTaxReturn));

        // Call the method to be tested:
        List<TaxReturnDto> result = taxReturnService.findAllByUserIdAndYear(1, 2024);

        // Verify the result:
        assertEquals(1, result.size(), "The size of the list should be 1.");
        assertEquals(1, result.get(0).getId(), "The TaxReturn ID should be 1.");
        assertEquals(2024, result.get(0).getYear(), "The TaxReturn year should be 2024.");
        assertEquals(1, result.get(0).getUserId(), "The TaxReturn user ID should be 1.");
    }

    // Update TaxReturn:
    @Test
    void updateTaxReturn() {

        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(updatedTaxReturn.mapToEntity()));
        when(taxReturnRepository.saveAndFlush(updatedTaxReturn.mapToEntity())).thenReturn(updatedTaxReturn.mapToEntity());

        // Call the method to be tested:
        UserDataDto result = taxReturnService.updateTaxReturn(1, updatedTaxReturn);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturn ID should be 1.");
        assertEquals(2024, result.getYear(), "The TaxReturn year should be 2024.");
        assertEquals(1, result.getUserId(), "The TaxReturn user ID should be 1.");
        assertEquals("TestFirstName", result.getFirstName(), "The TaxReturn first name should be TestFirstName.");
        assertEquals("TestLastName", result.getLastName(), "The TaxReturn last name should be TestLastName.");
        assertEquals("TestAddress", result.getAddress(), "The TaxReturn address should be TestAddress.");
        assertEquals("TestCity", result.getCity(), "The TaxReturn city should be TestCity.");
        assertEquals(State.FL, result.getState(), "The TaxReturn state should be FL.");
        assertEquals("TestZipCode", result.getZip(), "The TaxReturn zip code should be TestZipCode.");
    }

    // Delete TaxReturn Success:
    @Test
    void deleteTaxReturn() {

        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(updatedTaxReturn.mapToEntity()));

        //Define ArgumentCaptor:
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);

        // Call the method to be tested:
        taxReturnService.deleteTaxReturn(1, 1);

        // Capture the argument passed to the deleteById method
        verify(taxReturnRepository).deleteById(idCaptor.capture());

        // Verify the result:
        assertEquals(1, idCaptor.getValue(), "The TaxReturn ID should be 1.");
    }

    // Delete TaxReturn Unauthorized:
    @Test
    void deleteTaxReturnUnauthorized() {

        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(updatedTaxReturn.mapToEntity()));

        // Verify the exception
        assertThrows(UnauthorizedException.class, () -> taxReturnService.deleteTaxReturn(1, 2), "UnauthorizedException should be thrown.");
    }

    // Claim deductions success:
    @Test
    void claimDeductionSuccess() {
        // Define stubbing:
        when(taxReturnDeductionRepository.saveAndFlush(deductionRequest.mapToEntity())).thenReturn(returnedDeduction);

        // Call the method to be tested:
        TaxReturnDeductionDto result = taxReturnService.claimDeduction(1, deductionRequest);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturnDeduction ID should be 1.");
        assertEquals(1, result.getTaxReturn(), "The TaxReturnDeduction TaxReturn ID should be 1.");
        assertEquals(1, result.getDeduction(), "The TaxReturnDeduction Deduction ID should be 1.");
        assertEquals("TestDeduction", result.getDeductionName(), "The TaxReturnDeduction Deduction name should be TestDeduction.");
        assertFalse(result.isItemized(), "The TaxReturnDeduction itemized should be false.");
        assertEquals(BigDecimal.valueOf(1000.00), result.getAmountSpent(), "The TaxReturnDeduction amount spent should be 1000.00.");
        assertEquals(BigDecimal.valueOf(3850.00), result.getAgiLimit(), "The TaxReturnDeduction AGI limit should be 3850.00.");
    }

    // Claim deductions duplicate:
    @Test
    void claimDeductionDuplicateException() {
        // Define stubbing:
        when(taxReturnDeductionRepository.saveAndFlush(deductionRequest.mapToEntity())).thenThrow(new DuplicateDataException("Duplicate deduction", "TestDeduction"));

        // Verify the exception
        assertThrows(DuplicateDataException.class, () -> taxReturnService.claimDeduction(1, deductionRequest), "DuplicateDataException should be thrown.");
    }

    // Get refund:
    @Test
    void getRefund() {
        // Define stubbing:
        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(updatedTaxReturn.mapToEntity()));

        // Call the method to be tested:
        RefundDto result = taxReturnService.getRefund(1, 1);

        // Verify the result:
        assertEquals(BigDecimal.ZERO.setScale(2), result.getFederalRefund(), "The TaxReturn federal refund should be 0.00.");
        assertEquals(BigDecimal.ZERO.setScale(2), result.getStateRefund(), "The TaxReturn state refund should be 0.00.");
    }

    // Get TaxReturnDeduction by ID success:
    @Test
    void getTaxReturnDeductionByIdSuccess() {
        // Define stubbing:
        when(taxReturnDeductionRepository.findById(1)).thenReturn(Optional.of(returnedDeduction));

        // Call the method to be tested:
        TaxReturnDeductionDto result = taxReturnService.getTaxReturnDeductionById(1);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturnDeduction ID should be 1.");
        assertEquals(1, result.getTaxReturn(), "The TaxReturnDeduction TaxReturn ID should be 1.");
        assertEquals(1, result.getDeduction(), "The TaxReturnDeduction Deduction ID should be 1.");
        assertEquals("TestDeduction", result.getDeductionName(), "The TaxReturnDeduction Deduction name should be TestDeduction.");
        assertFalse(result.isItemized(), "The TaxReturnDeduction itemized should be false.");
        assertEquals(BigDecimal.valueOf(1000.00), result.getAmountSpent(), "The TaxReturnDeduction amount spent should be 1000.00.");
        assertEquals(BigDecimal.valueOf(3850.00), result.getAgiLimit(), "The TaxReturnDeduction AGI limit should be 3850.00.");
    }

    // Get TaxReturnDeduction by ID failure:
    @Test
    void getTaxReturnDeductionByIdFailure() {
        // Define stubbing:
        when(taxReturnDeductionRepository.findById(1)).thenReturn(Optional.empty());

        // Verify the exception
        assertThrows(NotFoundException.class, () -> taxReturnService.getTaxReturnDeductionById(1), "NotFoundException should be thrown.");
    }

    // Get all deductions for a TaxReturn:
    @Test
    void getDeductions() {
        // Define stubbing:
        when(taxReturnDeductionRepository.findAllByTaxReturnId(1)).thenReturn(List.of(returnedDeduction));

        // Call the method to be tested:
        List<TaxReturnDeductionDto> result = taxReturnService.getDeductions(1);

        // Verify the result:
        assertEquals(1, result.size(), "The size of the list should be 1.");
        assertEquals(1, result.get(0).getId(), "The TaxReturnDeduction ID should be 1.");
        assertEquals(1, result.get(0).getTaxReturn(), "The TaxReturnDeduction TaxReturn ID should be 1.");
        assertEquals(1, result.get(0).getDeduction(), "The TaxReturnDeduction Deduction ID should be 1.");
        assertEquals("TestDeduction", result.get(0).getDeductionName(), "The TaxReturnDeduction Deduction name should be TestDeduction.");
        assertFalse(result.get(0).isItemized(), "The TaxReturnDeduction itemized should be false.");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getAmountSpent(), "The TaxReturnDeduction amount spent should be 1000.00.");
        assertEquals(BigDecimal.valueOf(3850.00), result.get(0).getAgiLimit(), "The TaxReturnDeduction AGI limit should be 3850.00.");
    }

    // Update a TaxReturnDeduction:
    @Test
    void updateTaxReturnDeduction() {

        TaxReturnDeductionDto updatedDeduction = new TaxReturnDeductionDto(returnedDeduction);

        // Define stubbing:
        when(taxReturnDeductionRepository.findById(1)).thenReturn(Optional.of(returnedDeduction));
        when(taxReturnDeductionRepository.saveAndFlush(updatedDeduction.mapToEntity())).thenReturn(returnedDeduction);

        // Call the method to be tested:
        TaxReturnDeductionDto result = taxReturnService.updateTaxReturnDeduction(1, updatedDeduction);

        // Verify the result:
        assertEquals(1, result.getId(), "The TaxReturnDeduction ID should be 1.");
        assertEquals(1, result.getTaxReturn(), "The TaxReturnDeduction TaxReturn ID should be 1.");
        assertEquals(1, result.getDeduction(), "The TaxReturnDeduction Deduction ID should be 1.");
        assertEquals("TestDeduction", result.getDeductionName(), "The TaxReturnDeduction Deduction name should be TestDeduction.");
        assertFalse(result.isItemized(), "The TaxReturnDeduction itemized should be false.");
    }

    // Delete a TaxReturnDeduction:
    @Test
    void deleteTaxReturnDeduction() {
        // Define stubbing:
        when(taxReturnDeductionRepository.findById(1)).thenReturn(Optional.of(returnedDeduction));

        //Define ArgumentCaptor:
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);

        // Call the method to be tested:
        taxReturnService.deleteTaxReturnDeduction(1);

        // Capture the argument passed to the deleteById method
        verify(taxReturnDeductionRepository).deleteById(idCaptor.capture());

        // Verify the result:
        assertEquals(1, idCaptor.getValue(), "The TaxReturnDeduction ID should be 1.");
    }

    // Delete all by User ID:
    @Test
    void deleteAllByUserId() {

        //Define ArgumentCaptor:
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);

        // Call the method to be tested:
        taxReturnService.deleteAllByUserId(1);

        // Capture the argument passed to the deleteById method
        verify(taxReturnRepository).deleteAllByUserId(idCaptor.capture());

        // Verify the result:
        assertEquals(1, idCaptor.getValue(), "The UserId should be 1.");
    }

    // Return list of all FilingStatuses:
    @Test
    void getFilingStatuses() {
        // Call the method to be tested:
        List<String> result = taxReturnService.getFilingStatuses();

        // Verify the result:
        assertEquals(5, result.size(), "The size of the list should be 4.");
        assertEquals("Single", result.get(0), "The first FilingStatus should be Single.");
        assertEquals("Married: Filing Jointly", result.get(1), "The second FilingStatus should be Married Filing Jointly.");
        assertEquals("Married: Filing Separately", result.get(2), "The third FilingStatus should be Married Filing Separately.");
        assertEquals("Head of Household", result.get(3), "The fourth FilingStatus should be Head of Household.");
        assertEquals("Widow", result.get(4), "The fifth FilingStatus should be Widow.");
    }
}
