package com.skillstorm.taxservice.services;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.skillstorm.taxservice.dtos.OtherIncomeDto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.OtherIncome;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.repositories.OtherIncomeRepository;
import com.skillstorm.taxservice.repositories.TaxReturnRepository;

public class OtherIncomeServiceTest {

    @Mock
    private OtherIncomeRepository otherIncomeRepository;

    @Mock
    private TaxReturnRepository taxReturnRepository;

    @Mock
    private Environment env;

    @InjectMocks
    private OtherIncomeService otherIncomeService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        closeable.close();
    }

    // -----------------------------------------------------------------------------------
    // Success Scenarios
    // -----------------------------------------------------------------------------------

    @Test
    void findById_ExistingId_ReturnsOtherIncomeDto() {
        // Given
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setId(1);
        int id = 1;
        OtherIncome otherIncome = new OtherIncome();
        otherIncome.setId(id);
        otherIncome.setTaxReturn(taxReturn);
        otherIncome.setLongTermCapitalGains(BigDecimal.TEN);

        when(otherIncomeRepository.findById(id)).thenReturn(Optional.of(otherIncome));

        // When
        OtherIncomeDto result = otherIncomeService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.TEN, result.getLongTermCapitalGains());
    }

    @Test
    void sumOtherIncome_ValidOtherIncomeDto_ReturnsSum() throws IllegalAccessException {
        // Given
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setLongTermCapitalGains(BigDecimal.valueOf(100));
        otherIncomeDto.setShortTermCapitalGains(BigDecimal.valueOf(200));
        otherIncomeDto.setOtherInvestmentIncome(BigDecimal.valueOf(300));
        otherIncomeDto.setNetBusinessIncome(BigDecimal.valueOf(400));
        otherIncomeDto.setAdditionalIncome(BigDecimal.valueOf(500));

        // When
        BigDecimal sum = otherIncomeDto.getSum();

        // Then
        assertEquals(BigDecimal.valueOf(1500), sum);
    }

    @Test
    void createOtherIncome_ValidDto_ReturnsCreatedDto() {
        // Given
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setId(1);
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setTaxReturnId(taxReturn.getId());

        when(taxReturnRepository.findById(1)).thenReturn(Optional.of(new TaxReturn()));
        when(otherIncomeRepository.save(any(OtherIncome.class))).thenAnswer(invocation -> {
            OtherIncome saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // When
        OtherIncomeDto result = otherIncomeService.createOtherIncome(otherIncomeDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTaxReturnId());
    }

    @Test
    void updateOtherIncome_ExistingDto_ReturnsUpdatedDto() {
        // Given
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setId(1);
        OtherIncome existingOtherIncome = new OtherIncome();
        existingOtherIncome.setTaxReturn(taxReturn);
        existingOtherIncome.setLongTermCapitalGains(BigDecimal.TEN);
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setTaxReturnId(taxReturn.getId());

        when(otherIncomeRepository.findByTaxReturnId(taxReturn.getId())).thenReturn(Optional.of(existingOtherIncome));
        when(otherIncomeRepository.save(any(OtherIncome.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OtherIncomeDto result = otherIncomeService.updateOtherIncome(otherIncomeDto);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getLongTermCapitalGains());
    }

    @Test
    void deleteOtherIncome_ExistingDto_DeletesDto() {
        // Given
        TaxReturn taxReturn = new TaxReturn();
        OtherIncome existingOtherIncome = new OtherIncome();
        existingOtherIncome.setTaxReturn(new TaxReturn());

        when(otherIncomeRepository.findByTaxReturnId(taxReturn.getId())).thenReturn(Optional.of(existingOtherIncome));

        // When
        otherIncomeService.deleteOtherIncome(new OtherIncomeDto());

        // Then
        verify(otherIncomeRepository, times(1)).delete(existingOtherIncome);
    }

    @Test
    public void testFindByTaxReturnId_Success() {
        // Given
        int taxReturnId = 1;
        OtherIncome otherIncome = new OtherIncome();
        TaxReturn taxReturn = new TaxReturn();
        otherIncome.setTaxReturn(taxReturn);

        when(otherIncomeRepository.findByTaxReturnId(taxReturnId)).thenReturn(Optional.of(otherIncome));

        // When
        OtherIncomeDto result = otherIncomeService.findByTaxReturnId(taxReturnId);

        // Then
        assertNotNull(result);
        verify(otherIncomeRepository).findByTaxReturnId(taxReturnId);
    }

    @Test
    public void testDeleteOtherIncomeById_Success() {
        // Given
        int otherIncomeId = 1;
        doNothing().when(otherIncomeRepository).deleteById(otherIncomeId);

        // When
        otherIncomeService.deleteOtherIncomeById(otherIncomeId);

        // Then
        verify(otherIncomeRepository).deleteById(otherIncomeId);
    }

    // -----------------------------------------------------------------------------------
    // Failure Scenarios
    // -----------------------------------------------------------------------------------

    @Test
    public void testFindByTaxReturnId_NotFound() {
        // Given
        int taxReturnId = 1;
        when(otherIncomeRepository.findByTaxReturnId(taxReturnId)).thenReturn(Optional.empty());
        when(env.getProperty("otherincome.not.found")).thenReturn("Other income not found for tax return ID: ");

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            otherIncomeService.findByTaxReturnId(taxReturnId);
        });

        assertEquals("Other income not found for tax return ID: " + taxReturnId, exception.getMessage());
        verify(otherIncomeRepository).findByTaxReturnId(taxReturnId);
    }

    @Test
    public void testDeleteOtherIncomeById_Failure() {
        // Given
        int otherIncomeId = 1;
        doThrow(new RuntimeException("Delete failed")).when(otherIncomeRepository).deleteById(otherIncomeId);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            otherIncomeService.deleteOtherIncomeById(otherIncomeId);
        });

        assertEquals("Delete failed", exception.getMessage());
        verify(otherIncomeRepository).deleteById(otherIncomeId);
    }

    @Test
    public void testDeleteOtherIncome_NotFoundException() {
        // Given
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setTaxReturnId(1);
        when(otherIncomeRepository.findByTaxReturnId(otherIncomeDto.getTaxReturnId())).thenReturn(Optional.empty());
        when(env.getProperty("otherincome.not.found")).thenReturn("Other income not found for tax return ID: ");

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            otherIncomeService.deleteOtherIncome(otherIncomeDto);
        });

        assertEquals("Other income not found for tax return ID: " + otherIncomeDto.getTaxReturnId(), exception.getMessage());
    }

    @Test
    public void testCreateOtherIncome_IllegalArgumentException() {
        // Given
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setTaxReturnId(1);
        when(taxReturnRepository.findById(otherIncomeDto.getTaxReturnId())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            otherIncomeService.createOtherIncome(otherIncomeDto);
        });

        assertEquals("No existing tax return with ID: " + otherIncomeDto.getTaxReturnId(), exception.getMessage());
    }

    @Test
    public void testUpdateOtherIncome_NotFoundException() {
        // Given
        OtherIncomeDto otherIncomeDto = new OtherIncomeDto();
        otherIncomeDto.setTaxReturnId(1);
        when(otherIncomeRepository.findByTaxReturnId(otherIncomeDto.getTaxReturnId())).thenReturn(Optional.empty());
        when(env.getProperty("otherincome.not.found")).thenReturn("Other income not found for tax return ID: ");

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            otherIncomeService.updateOtherIncome(otherIncomeDto);
        });

        assertEquals("Other income not found for tax return ID: " + otherIncomeDto.getTaxReturnId(), exception.getMessage());
    }

    @Test
    public void testFindById_NotFoundException() {
        // Given
        int id = 1;
        when(otherIncomeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            otherIncomeService.findById(id);
        });

        assertEquals("other income not found with id: " + id, exception.getMessage());
    }
}
