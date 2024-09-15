package com.skillstorm.taxservice.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
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

    @Mock
    private Environment environment;

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


    @Test
    public void testFindById_NotFoundException() {
        // Given: taxReturnCreditRepository returns empty Optional
        int id = 1;
        when(taxReturnCreditRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then: Expect NotFoundException
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taxReturnCreditService.findById(id);
        });

        assertEquals("tax return credit not found with id: " + id, exception.getMessage());
    }

    @Test
    public void testFindByTaxReturnId_NotFoundException() {
        // Given: taxReturnCreditRepository returns empty Optional
        int taxReturnId = 1;
        when(taxReturnCreditRepository.findByTaxReturnId(taxReturnId)).thenReturn(Optional.empty());
        when(environment.getProperty("taxreturncredit.not.found")).thenReturn("Tax return credit not found with ID: ");

        // When & Then: Expect NotFoundException
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taxReturnCreditService.findByTaxReturnId(taxReturnId);
        });

        assertEquals("Tax return credit not found with ID: " + taxReturnId, exception.getMessage());
    }

    @Test
    public void testCreateTaxReturnCredit_IllegalArgumentException() {
        // Given: taxReturnRepository returns empty Optional
        TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();
        taxReturnCreditDto.setTaxReturnId(1);
        when(taxReturnRepository.findById(taxReturnCreditDto.getTaxReturnId())).thenReturn(Optional.empty());

        // When & Then: Expect IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taxReturnCreditService.createTaxReturnCredit(taxReturnCreditDto);
        });

        assertEquals("no tax return exists with id: " + taxReturnCreditDto.getTaxReturnId(), exception.getMessage());
    }

    @Test
    public void testUpdateTaxReturnCredit_NotFoundException() {
        // Given: taxReturnCreditRepository returns empty Optional
        TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();
        taxReturnCreditDto.setTaxReturnId(1);
        when(taxReturnCreditRepository.findByTaxReturnId(taxReturnCreditDto.getTaxReturnId())).thenReturn(Optional.empty());
        when(environment.getProperty("taxreturncredit.not.found")).thenReturn("Tax return credit not found with ID: ");

        // When & Then: Expect NotFoundException
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taxReturnCreditService.updateTaxReturnCredit(taxReturnCreditDto);
        });

        assertEquals("Tax return credit not found with ID: " + taxReturnCreditDto.getTaxReturnId(), exception.getMessage());
    }

    @Test
    public void testDeleteTaxReturnCredit_NotFoundException() {
        // Given: taxReturnCreditRepository does not find the ID
        int id = 1;
        when(taxReturnCreditRepository.existsById(id)).thenReturn(false);
        when(environment.getProperty("taxreturncredit.not.found")).thenReturn("Tax return credit not found with ID: ");

        // When & Then: Expect NotFoundException
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taxReturnCreditService.deleteTaxReturnCredit(id);
        });

        assertEquals("Tax return credit not found with ID: " + id, exception.getMessage());
    }
}
