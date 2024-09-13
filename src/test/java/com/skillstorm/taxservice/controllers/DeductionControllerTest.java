package com.skillstorm.taxservice.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.skillstorm.taxservice.dtos.DeductionDto;
import com.skillstorm.taxservice.services.DeductionService;

public class DeductionControllerTest {
    
    @Mock
    private DeductionService deductionService;

    @InjectMocks
    private DeductionController deductionController;

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
    public void testConstructor() {
        deductionController = new DeductionController(deductionService);

        // verify constructor is instantiated correctly
        assertNotNull(deductionController);
    }

    @Test
    public void testFindDeductionById() {
        int id = 1;
        DeductionDto dto = new DeductionDto(id);

        // stub findbyid to return this dto
        when(deductionService.findById(anyInt())).thenReturn(dto);

        // store response from finddeductionbyid and wrap inside response enitity
        ResponseEntity<DeductionDto> response = deductionController.findDeductionById(id);

        // verify it returns OK status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify the dto id matches
        assertEquals(id, response.getBody().getId());
    }

    @Test
    public void testFindAllDeductions() {
        // create dtos for list
        DeductionDto dto1 = new DeductionDto(1);
        DeductionDto dto2 = new DeductionDto(2);

        // create dto list for stub return
        List<DeductionDto> dtoList = Arrays.asList(dto1, dto2);

        // stub findall to return mocked list
        when(deductionService.findAll()).thenReturn(dtoList);

        // store repsonse from findall and wrap inside response entity
        ResponseEntity<List<DeductionDto>> response = deductionController.findAllDeductions();

        // verify it returns OK status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify size matches
        assertEquals(2, response.getBody().size());
        // verify id matches for both dtos
        assertEquals(1, response.getBody().get(0).getId());
        assertEquals(2, response.getBody().get(1).getId());
    }
}
