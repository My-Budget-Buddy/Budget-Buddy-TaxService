package com.skillstorm.taxservice.services;

import com.skillstorm.taxservice.constants.State;
import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.exceptions.NotFoundException;
import com.skillstorm.taxservice.models.TaxReturn;
import com.skillstorm.taxservice.models.W2;
import com.skillstorm.taxservice.repositories.W2Repository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class W2ServiceTest {
    
    @InjectMocks
    private static W2Service w2Service;
    
    @Mock
    private static W2Repository w2Repository;
    @Mock
    private static S3Service s3Service;
    @Spy
    private static Environment environment;

    
    private static W2Dto newW2;
    private static W2 returnedW2;
    private static List<W2Dto> newW2List;
    private static List<W2> returnedW2List;
    
    @BeforeEach
    public void setup() {
        w2Service = new W2Service(w2Repository, s3Service, environment);
        
        setupW2s();
    }

    private void setupW2s() {
        newW2 = new W2Dto();
        newW2.setYear(2024);
        newW2.setUserId(1);
        newW2.setEmployer("Test Employer");
        newW2.setState(State.AL);
        newW2.setWages(BigDecimal.valueOf(1000.00));
        newW2.setFederalIncomeTaxWithheld(BigDecimal.valueOf(300.00));
        newW2.setStateIncomeTaxWithheld(BigDecimal.ZERO);
        newW2.setSocialSecurityTaxWithheld(BigDecimal.valueOf(200.00));
        newW2.setMedicareTaxWithheld(BigDecimal.valueOf(100.00));

        returnedW2 = new W2();
        returnedW2.setId(1);
        returnedW2.setYear(2024);
        returnedW2.setUserId(1);
        returnedW2.setEmployer("Test Employer");
        returnedW2.setState(1);
        returnedW2.setWages(BigDecimal.valueOf(1000.00));
        returnedW2.setFederalIncomeTaxWithheld(BigDecimal.valueOf(300.00));
        returnedW2.setStateIncomeTaxWithheld(BigDecimal.ZERO);
        returnedW2.setSocialSecurityTaxWithheld(BigDecimal.valueOf(200.00));
        returnedW2.setMedicareTaxWithheld(BigDecimal.valueOf(100.00));
        returnedW2.setTaxReturn(new TaxReturn());
        returnedW2.setState(1);

        newW2List = List.of(newW2);
        returnedW2List = List.of(returnedW2);
    }

    // Add W2:
    @Test
    void addW2Test() {

        //Define stubbing:
        when(w2Repository.saveAndFlush(newW2.mapToEntity())).thenReturn(returnedW2);

        //Call the method to test:
        W2Dto result = w2Service.addW2(newW2);

        //Verify the result:
        assertEquals(1, result.getId(), "W2 ID should be 1");
        assertEquals(2024, result.getYear(), "Year should be 2024");
        assertEquals(1, result.getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Add List of W2s:
    @Test
    void addListW2sTest() {

        //Define stubbing:
        when(w2Repository.saveAll(newW2List.stream().map(W2Dto::mapToEntity).toList())).thenReturn(returnedW2List);

        //Call the method to test:
        List<W2Dto> result = w2Service.addListW2s(newW2List);

        //Verify the result:
        assertEquals(1, result.size(), "List size should be 1");
        assertEquals(1, result.get(0).getId(), "W2 ID should be 1");
        assertEquals(2024, result.get(0).getYear(), "Year should be 2024");
        assertEquals(1, result.get(0).getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.get(0).getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.get(0).getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.get(0).getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Find W2 by ID Success:
    @Test
    void findByIdSuccessTest() {

        //Define stubbing:
        when(w2Repository.findById(1)).thenReturn(java.util.Optional.of(returnedW2));

        //Call the method to test:
        W2Dto result = w2Service.findById(1, 1);

        //Verify the result:
        assertEquals(1, result.getId(), "W2 ID should be 1");
        assertEquals(2024, result.getYear(), "Year should be 2024");
        assertEquals(1, result.getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Find W2 by ID Failure:
    @Test
    void findByIdFailureTest() {

        //Define stubbing:
        when(w2Repository.findById(1)).thenReturn(Optional.empty());

        //Verify the exception:
        assertThrows(NotFoundException.class, () -> w2Service.findById(1,1), "NotFoundException should be thrown");
    }

    // Find all W2s by UserId:
    @Test
    void findAllByUserIdTest() {

        //Define stubbing:
        when(w2Repository.findAllByUserId(1)).thenReturn(returnedW2List);

        //Call the method to test:
        List<W2Dto> result = w2Service.findAllByUserId(1);

        //Verify the result:
        assertEquals(1, result.size(), "List size should be 1");
        assertEquals(1, result.get(0).getId(), "W2 ID should be 1");
        assertEquals(2024, result.get(0).getYear(), "Year should be 2024");
        assertEquals(1, result.get(0).getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.get(0).getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.get(0).getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.get(0).getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Find all W2s by UserId and Year:
    @Test
    void findAllByUserIdAndYearTest() {

        //Define stubbing:
        when(w2Repository.findAllByUserIdAndYear(1, 2024)).thenReturn(returnedW2List);

        //Call the method to test:
        List<W2Dto> result = w2Service.findAllByUserIdAndYear(1, 2024);

        //Verify the result:
        assertEquals(1, result.size(), "List size should be 1");
        assertEquals(1, result.get(0).getId(), "W2 ID should be 1");
        assertEquals(2024, result.get(0).getYear(), "Year should be 2024");
        assertEquals(1, result.get(0).getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.get(0).getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.get(0).getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.get(0).getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Find all W2s by TaxReturnId:
    @Test
    void findAllByTaxReturnIdTest() {

        //Define stubbing:
        when(w2Repository.findAllByTaxReturnId(1)).thenReturn(returnedW2List);

        //Call the method to test:
        List<W2Dto> result = w2Service.findAllByTaxReturnId(1,1);

        //Verify the result:
        assertEquals(1, result.size(), "List size should be 1");
        assertEquals(1, result.get(0).getId(), "W2 ID should be 1");
        assertEquals(2024, result.get(0).getYear(), "Year should be 2024");
        assertEquals(1, result.get(0).getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.get(0).getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.get(0).getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.get(0).getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Update W2 by ID Success:
    @Test
    void updateByIdSuccessTest() {

        //Define stubbing:
        when(w2Repository.existsById(1)).thenReturn(true);
        when(w2Repository.saveAndFlush(returnedW2)).thenReturn(returnedW2);

        //Call the method to test:
        W2Dto result = w2Service.updateById(1, new W2Dto(returnedW2));

        //Verify the result:
        assertEquals(1, result.getId(), "W2 ID should be 1");
        assertEquals(2024, result.getYear(), "Year should be 2024");
        assertEquals(1, result.getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Update All W2s by TaxReturnId:
    @Test
    void updateAllByTaxReturnIdTest() {

        //Define stubbing:
        when(w2Repository.saveAll(returnedW2List)).thenReturn(returnedW2List);

        //Call the method to test:
        List<W2Dto> result = w2Service.updateAllByTaxReturnId(1, returnedW2List.stream().map(W2Dto::new).toList());

        //Verify the result:
        assertEquals(1, result.size(), "List size should be 1");
        assertEquals(1, result.get(0).getId(), "W2 ID should be 1");
        assertEquals(2024, result.get(0).getYear(), "Year should be 2024");
        assertEquals(1, result.get(0).getUserId(), "User ID should be 1");
        assertEquals("Test Employer", result.get(0).getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(BigDecimal.valueOf(1000.00), result.get(0).getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.get(0).getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.get(0).getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.get(0).getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
    }

    // Delete W2 by ID:
    @Test
    void deleteW2ByIdTest() {

        //Define stubbing:
        when(w2Repository.existsById(1)).thenReturn(true);

        //Define ArgumentCaptor:
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);

        //Call the method to test:
        w2Service.deleteById(1);

        //Verify the method was called:
        verify(w2Repository).deleteById(idCaptor.capture());

        //Verify the result:
        assertEquals(1, idCaptor.getValue(), "W2 ID should be 1");
    }

    // Upload W2 Image:
    @Test
    void uploadW2ImageTest() {

        //Define stubbing:
        when(w2Repository.findById(1)).thenReturn(Optional.of(returnedW2));

        // Define ArgumentCaptors:
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> imageCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<W2> w2Captor = ArgumentCaptor.forClass(W2.class);

        //Call the method to test:
        byte[] image = {0};
        w2Service.uploadImage(1, image, "image/png", 1);

        //Verify the method was called:
        verify(s3Service).uploadFile(keyCaptor.capture(), imageCaptor.capture());
        verify(w2Repository).saveAndFlush(w2Captor.capture());

        // The w2Captor should capture the W2 object with the updated image key getting sent to the database:
        W2 result = w2Captor.getValue();

        //Verify the result:
        assertEquals("w2s/1/1.png", keyCaptor.getValue(), "Key should be 'w2s/1/1.png'");
        assertArrayEquals(new byte[]{0}, imageCaptor.getValue(), "Image should be a byte array");
        assertEquals(1, result.getId(), "W2 ID should be 1");
        assertEquals("Test Employer", result.getEmployer(), "Employer should be 'Test Employer'");
        assertEquals(2024, result.getYear(), "Year should be 2024");
        assertEquals(BigDecimal.valueOf(1000.00), result.getWages(), "Wages should be 1000.00");
        assertEquals(BigDecimal.valueOf(300.00), result.getFederalIncomeTaxWithheld(), "Federal Taxes Withheld should be 300.00");
        assertEquals(BigDecimal.valueOf(200.00), result.getSocialSecurityTaxWithheld(), "Social Security Taxes Withheld should be 200.00");
        assertEquals(BigDecimal.valueOf(100.00), result.getMedicareTaxWithheld(), "Medicare Taxes Withheld should be 100.00");
        assertEquals(1, result.getUserId(), "User ID should be 1");
        assertEquals("w2s/1/1.png", result.getImageKey(), "Image Key should be 'w2s/1/1.png'");
    }

    // Download W2 Image Success:
    @Test
    @SneakyThrows
    void testDownloadW2ImageSuccess() {

        byte[] imageBytes = {1, 2, 3};
        Resource resource = new ByteArrayResource(imageBytes);
        returnedW2.setImageKey("w2s/1/1.png");

        //Define stubbing:
        when(w2Repository.findById(1)).thenReturn(Optional.of(returnedW2));
        when(s3Service.getObject("w2s/1/1.png")).thenReturn(resource.getInputStream());

        //Call the method to test:
        Resource result = w2Service.downloadImage(1, 1);

        //Verify the result:
        verify(s3Service).getObject("w2s/1/1.png");
        assertArrayEquals(new byte[]{1,2,3}, result.getContentAsByteArray(), "Should return byte array {1,2,3}");
    }
}
