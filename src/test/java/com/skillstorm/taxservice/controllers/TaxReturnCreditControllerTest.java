package com.skillstorm.taxservice.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.skillstorm.taxservice.dtos.TaxReturnCreditDto;
import com.skillstorm.taxservice.services.TaxReturnCreditService;

public class TaxReturnCreditControllerTest {
    
    @Mock
    private TaxReturnCreditService taxReturnCreditService;

    @InjectMocks
    private TaxReturnCreditController taxReturnCreditController;
    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    public void teardown() throws Exception {
        closeable.close();
    }

    @Test
    public void testFindByTaxReturnId() {
        TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();

        when(taxReturnCreditService.findByTaxReturnId(anyInt())).thenReturn(taxReturnCreditDto);

        ResponseEntity<TaxReturnCreditDto> response = taxReturnCreditController.findByTaxReturnId(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taxReturnCreditDto, response.getBody());
    }

    @Test
    public void testCreateTaxReturnCredit() {
        TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();

        when(taxReturnCreditService.createTaxReturnCredit(taxReturnCreditDto)).thenReturn(taxReturnCreditDto);

        ResponseEntity<TaxReturnCreditDto> response = taxReturnCreditController.createTaxReturnCredit(taxReturnCreditDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taxReturnCreditDto, response.getBody());
    }

    @Test
    public void testUpdateTaxReturnCredit() {
        TaxReturnCreditDto taxReturnCreditDto = new TaxReturnCreditDto();

        when(taxReturnCreditService.updateTaxReturnCredit(taxReturnCreditDto)).thenReturn(taxReturnCreditDto);

        ResponseEntity<TaxReturnCreditDto> response = taxReturnCreditController.updateTaxReturnCredit(taxReturnCreditDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(taxReturnCreditDto, response.getBody());
    }

    @Test
    public void testDeleteTaxReturnCredit() {

        ResponseEntity<Void> response = taxReturnCreditController.deleteTaxReturnCredit(0);

        verify(taxReturnCreditService).deleteTaxReturnCredit(anyInt());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
