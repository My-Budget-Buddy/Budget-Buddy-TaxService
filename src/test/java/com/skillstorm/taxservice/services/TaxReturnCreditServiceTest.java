package com.skillstorm.taxservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.models.TaxReturnCredit;
import com.skillstorm.taxservice.repositories.TaxReturnCreditRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;

class TaxReturnCreditServiceTest {

    @Mock
    private TaxReturnCreditRepository taxReturnCreditRepository;

    @Mock
    private TaxReturnRepository taxReturnRepository;

    @InjectMocks
    private TaxReturnCreditService taxReturnCreditService;

    private TaxReturnCredit taxReturnCredit;
    private TaxReturnCreditDto taxReturnCreditDto;

    @BeforeEach
    public void setUp() {
      MockitoAnnotations.openMocks(this);
      taxReturnCredit = createTaxReturnCredit();
      taxReturnCreditDto = createTaxReturnCreditDto();
    }

    @Test
    void testFindById() {
        when(taxReturnCreditRepository.findById(1)).thenReturn(Optional.of(taxReturnCredit));

        TaxReturnCreditDto result = taxReturnCreditService.findById(1);

        assertEquals(1, result.getTaxReturnId());
    }

    @Test
    void testFindByTaxReturnId() {
        when(taxReturnCreditRepository.findByTaxReturnId(1)).thenReturn(Optional.of(taxReturnCredit));

        TaxReturnCreditDto result = taxReturnCreditService.findByTaxReturnId(1);

        assertEquals(1, result.getTaxReturnId());
    }

    @Test
    void testCreateTaxReturnCredit() {
      TaxReturn taxReturn = createTaxReturn();
      when(taxReturnRepository.findById(taxReturnCreditDto.getTaxReturnId())).thenReturn(Optional.of(taxReturn));
      when(taxReturnCreditRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

      TaxReturnCreditDto result = taxReturnCreditService.createTaxReturnCredit(taxReturnCreditDto);

      assertEquals(taxReturnCreditDto.getNumDependentsAotc(), result.getNumDependentsAotc());
    }

    @Test
    void testUpdateTaxReturnCredit() {
      when(taxReturnCreditRepository.findByTaxReturnId(taxReturnCreditDto.getTaxReturnId())).thenReturn(Optional.of(taxReturnCredit));
      when(taxReturnCreditRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

      TaxReturnCreditDto result = taxReturnCreditService.updateTaxReturnCredit(taxReturnCreditDto);

      assertEquals(taxReturnCreditDto.getNumDependentsAotc(), result.getNumDependentsAotc());
    }

    @Test
    void testDeleteTaxReturnCredit() {
      when(taxReturnCreditRepository.existsById(taxReturnCreditDto.getTaxReturnId())).thenReturn(true);

      taxReturnCreditService.deleteTaxReturnCredit(1);

      verify(taxReturnCreditRepository).deleteById(1);
    }

    // Setup method
    private TaxReturnCredit createTaxReturnCredit() {
      TaxReturnCredit taxReturnCredit = new TaxReturnCredit();
      TaxReturn taxReturn = new TaxReturn();
      taxReturn.setId(1);
      taxReturnCredit.setTaxReturn(taxReturn);
      return taxReturnCredit;
    }

    private TaxReturn createTaxReturn() {
      TaxReturn taxReturn = new TaxReturn();
      taxReturn.setId(1);
      return taxReturn;
    }

    private TaxReturnCreditDto createTaxReturnCreditDto() {
      TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();
      taxReturnCreditDto.setTaxReturnId(1);
      return taxReturnCreditDto;
    }
}
