package com.skillstorm.taxservice.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.units.qual.s;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.skillstorm.taxservice.dtos.RefundDto;
import com.skillstorm.taxservice.dtos.TaxReturnDeductionDto;
import com.skillstorm.taxservice.dtos.TaxReturnDto;
import com.skillstorm.taxservice.dtos.UserDataDto;
import com.skillstorm.taxservice.services.TaxReturnService;

public class TaxReturnControllerTest {

     @Mock
    private TaxReturnService taxReturnSvc;      //mock object

    @InjectMocks
    private TaxReturnController taxReturnCtl;       // the tested class which the mock object will be injected to
    private AutoCloseable closeable;                // used to manage mock objects (open and close them)

    /**
     * Opening all mock objects
     */
    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addTaxReturnTest() {
        UserDataDto user = new UserDataDto();

        when(taxReturnSvc.addTaxReturn(user)).thenReturn(user);

        UserDataDto response = taxReturnCtl.addTaxReturn(user, 1).getBody();

        assertEquals(response, user);
    }   

    @Test
    public void findByIdTest() {
        UserDataDto user = new UserDataDto();
        TaxReturnDto taxReturnDto = new TaxReturnDto();

        taxReturnDto.setUserId(user.getId());
        when(taxReturnSvc.findById(1, user.getId())).thenReturn(taxReturnDto);

        TaxReturnDto response = taxReturnCtl.findById(1, user.getId()).getBody();

        assertEquals(response, taxReturnDto);
    }

    @Test
    public void getRefundTest() {
        UserDataDto user = new UserDataDto();
        RefundDto taxReturnDto = new RefundDto(new BigDecimal(1000), new BigDecimal(500));

        when(taxReturnSvc.getRefund(1, user.getId())).thenReturn(taxReturnDto);

        RefundDto response = taxReturnCtl.getRefund(1, user.getId()).getBody();

        assertEquals(response, taxReturnDto);
    }

    @Test
    public void findAllByUserIdTest() {
        UserDataDto user1 = new UserDataDto();

        TaxReturnDto tr1 = new TaxReturnDto();
        TaxReturnDto tr2 = new TaxReturnDto();

        tr1.setUserId(user1.getId());
        tr2.setUserId(user1.getId());
        tr1.setYear(2020);
        tr2.setYear(2021);

        List<TaxReturnDto> taxReturnDtos = Arrays.asList(tr1, tr2);

        when(taxReturnSvc.findAllByUserId(user1.getId())).thenReturn(taxReturnDtos);
        when(taxReturnSvc.findAllByUserIdAndYear(user1.getId(), 2021)).thenReturn(taxReturnDtos);


        List<TaxReturnDto> response1 = taxReturnCtl.findAllByUserId(null, user1.getId()).getBody();
        List<TaxReturnDto> response2 = taxReturnCtl.findAllByUserId(2021, user1.getId()).getBody();
    

        assertEquals(response1, taxReturnDtos);
        assertEquals(response2, taxReturnDtos);
    }

    @Test
    public void updateTaxReturnTest() {
        UserDataDto user = new UserDataDto();
        TaxReturnDto taxReturnDto = new TaxReturnDto();

        taxReturnDto.setUserId(user.getId());
        when(taxReturnSvc.updateTaxReturn(1, taxReturnDto)).thenReturn(user);

        UserDataDto response = taxReturnCtl.updateTaxReturn(1, taxReturnDto, user.getId()).getBody();

        assertEquals(response, user);
    }

    @Test
    public void deleteTaxReturnTest() {
        UserDataDto user = new UserDataDto();
        TaxReturnDto taxReturnDto = new TaxReturnDto();

        taxReturnDto.setUserId(user.getId());
        when(taxReturnSvc.findById(1, user.getId())).thenReturn(taxReturnDto);

        assertAll(() -> taxReturnCtl.deleteTaxReturn(1, user.getId()));
    }

    @Test
    public void claimDeductionTest() {
        TaxReturnDeductionDto taxReturnDeductionDto = new TaxReturnDeductionDto();

        when(taxReturnSvc.claimDeduction(1, taxReturnDeductionDto)).thenReturn(taxReturnDeductionDto);

        TaxReturnDeductionDto response = taxReturnCtl.claimDeduction(1, taxReturnDeductionDto).getBody();

        assertEquals(response,taxReturnDeductionDto);
    }

    @Test
    public void getTaxReturnDeductionByIdTest() {
        TaxReturnDeductionDto taxReturnDeductionDto = new TaxReturnDeductionDto();

        when(taxReturnSvc.getTaxReturnDeductionById(1)).thenReturn(taxReturnDeductionDto);

        TaxReturnDeductionDto response = taxReturnCtl.getTaxReturnDeductionById(1).getBody();

        assertEquals(response, taxReturnDeductionDto);
    }

    @Test
    public void getDeductionsTest() {
        TaxReturnDeductionDto taxReturnDeductionDto = new TaxReturnDeductionDto();
        List<TaxReturnDeductionDto> taxReturnDeductionDtos = Arrays.asList(taxReturnDeductionDto);

        when(taxReturnSvc.getDeductions(1)).thenReturn(taxReturnDeductionDtos);

        List<TaxReturnDeductionDto> response = taxReturnCtl.getDeductions(1).getBody();

        assertEquals(response, taxReturnDeductionDtos);
    }

    @Test
    public void updateTaxReturnDeductionTest() {
        TaxReturnDeductionDto taxReturnDeductionDto = new TaxReturnDeductionDto();
        TaxReturnDeductionDto taxReturnDeductionDto2 = new TaxReturnDeductionDto();

        when(taxReturnSvc.updateTaxReturnDeduction(1, taxReturnDeductionDto)).thenReturn(taxReturnDeductionDto2);

        TaxReturnDeductionDto response = taxReturnCtl.updateTaxReturnDeduction(1, taxReturnDeductionDto).getBody();

        assertEquals(response, taxReturnDeductionDto2);
    }

    @Test
    public void deleteTaxReturnDeductionTest() {
        assertAll(() -> taxReturnCtl.deleteTaxReturnDeduction(1));
    }

    @Test
    public void deleteAllByUserIdTest() {
        assertAll(() -> taxReturnCtl.deleteAllByUserId(1));
    }
    
    @Test
    public void getFilingStatusesTest() {
        List<String> filingStatuses = Arrays.asList("Single", "Married Filing Jointly", "Married Filing Separately", "Head of Household");

        when(taxReturnSvc.getFilingStatuses()).thenReturn(filingStatuses);

        List<String> response = taxReturnCtl.getFilingStatuses().getBody();

        assertEquals(response, filingStatuses);
    }



    /**
     * Closes all mock objects
     * @throws Exception
     */
    @AfterEach
    public void teardown() throws Exception{
        closeable.close();
    }
    
}
