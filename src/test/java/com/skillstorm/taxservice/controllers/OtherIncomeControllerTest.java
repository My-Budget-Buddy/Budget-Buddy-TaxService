package com.skillstorm.taxservice.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
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

import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.services.OtherIncomeService;

public class OtherIncomeControllerTest {
    
    @Mock
    private OtherIncomeService otherIncomeService; 

    @InjectMocks
    private OtherIncomeController otherIncomeController;

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
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();

        when(otherIncomeService.findByTaxReturnId(anyInt())).thenReturn(otherIncomeDto);

        ResponseEntity<OtherIncomeDto> response = otherIncomeController.findByTaxReturnId(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(otherIncomeDto, response.getBody());
    }

    @Test
    public void testAddOtherIncome() {
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();

        when(otherIncomeService.createOtherIncome(any(OtherIncomeDto.class))).thenReturn(otherIncomeDto);

        ResponseEntity<OtherIncomeDto> response = otherIncomeController.addOtherIncome(otherIncomeDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(otherIncomeDto, response.getBody());
    }

    @Test
    public void testUpdateOtherIncome() {
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();

        when(otherIncomeService.updateOtherIncome(any(OtherIncomeDto.class))).thenReturn(otherIncomeDto);

        ResponseEntity<OtherIncomeDto> response = otherIncomeController.updateOtherIncome(otherIncomeDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(otherIncomeDto, response.getBody());
    }

    @Test
    public void testDeleteOtherIncome() {
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();

        ResponseEntity<Void> response = otherIncomeController.deleteOtherIncome(otherIncomeDto);

        verify(otherIncomeService).deleteOtherIncome(any(OtherIncomeDto.class));
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test 
    public void testDeleteOtherIncomeById() {
        ResponseEntity<Void> response = otherIncomeController.deleteOtherIncomeById(anyInt());

        verify(otherIncomeService).deleteOtherIncomeById(anyInt());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
