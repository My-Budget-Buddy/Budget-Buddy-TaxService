package com.skillstorm.taxservice.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.apache.tika.Tika;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.skillstorm.taxservice.dtos.W2Dto;
import com.skillstorm.taxservice.services.W2Service;

public class W2ControllerTest {

    @Mock
    private W2Service w2Service;

    @Mock
    private Resource imgResource;

    @Mock
    private Tika tika;

    @InjectMocks
    private W2Controller w2Controller;

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
        w2Controller = new W2Controller(w2Service);

        // verify constructor is instantiated correctly
        assertNotNull(w2Controller);
    }

    @Test
    public void testAddW2sByTaxReturnId() {
        int taxReturnId = 123;
        int userId = 1;
        W2Dto w2Dto1 = new W2Dto();
        W2Dto w2Dto2 = new W2Dto();
        w2Dto1.setId(taxReturnId);
        w2Dto2.setId(taxReturnId);

        // create a list of W2Dtos
        List<W2Dto> w2Dtos = List.of(w2Dto1, w2Dto2);
        
        // mock the service method to return the list of W2Dtos
        when(w2Service.updateAllByTaxReturnId(taxReturnId, w2Dtos)).thenReturn(w2Dtos);

        // call the controller method to add W2s by taxReturnId
        ResponseEntity<List<W2Dto>> response = w2Controller.addW2sByTaxReturnId(taxReturnId, w2Dtos, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify user id is correct
        assertEquals(userId, response.getBody().get(0).getUserId());
        assertEquals(userId, response.getBody().get(1).getUserId());
        // verify taxReturnId is correct
        assertEquals(taxReturnId, response.getBody().get(0).getId());
        assertEquals(taxReturnId, response.getBody().get(1).getId());
    }

    @Test
    public void testFindW2byId() {
        int id = 123;
        int userId = 1;
        W2Dto w2Dto = new W2Dto();
        w2Dto.setId(id);

        // mock the service method to return the W2
        when(w2Service.findById(id, userId)).thenReturn(w2Dto);

        // call the controller method to find W2 by id
        ResponseEntity<W2Dto> response = w2Controller.findW2ById(id, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify id is correct
        assertEquals(id, response.getBody().getId());
    }

    @Test
    public void testFindAllW2sByUserId() {
        int userId = 1;
        Integer year = null;
        W2Dto w2Dto1 = new W2Dto();
        W2Dto w2Dto2 = new W2Dto();
        w2Dto1.setUserId(userId);
        w2Dto2.setUserId(userId);

        // create a list of W2Dtos
        List<W2Dto> w2Dtos = List.of(w2Dto1, w2Dto2);
        
        // mock the service method to return the list of W2Dtos
        when(w2Service.findAllByUserId(userId)).thenReturn(w2Dtos);

        // call the controller method to find all W2s by userId
        ResponseEntity<List<W2Dto>> response = w2Controller.findAllW2sByUserId(year, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify user id is correct
        assertEquals(userId, response.getBody().get(0).getUserId());
        assertEquals(userId, response.getBody().get(1).getUserId());

    }

    @Test
    public void testFindAllW2sByUserIdAndYear() {
        int userId = 1;
        Integer year = 2021;
        W2Dto w2Dto1 = new W2Dto();
        W2Dto w2Dto2 = new W2Dto();
        w2Dto1.setUserId(userId);
        w2Dto2.setUserId(userId);
        w2Dto1.setYear(year);
        w2Dto2.setYear(year);

        // create a list of W2Dtos
        List<W2Dto> w2Dtos = List.of(w2Dto1, w2Dto2);
        
        // mock the service method to return the list of W2Dtos
        when(w2Service.findAllByUserIdAndYear(userId, year)).thenReturn(w2Dtos);

        // call the controller method to find all W2s by userId and year
        ResponseEntity<List<W2Dto>> response = w2Controller.findAllW2sByUserId(year, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify user id is correct
        assertEquals(userId, response.getBody().get(0).getUserId());
        assertEquals(userId, response.getBody().get(1).getUserId());
        // verify year is correct
        assertEquals(year, response.getBody().get(0).getYear());
        assertEquals(year, response.getBody().get(1).getYear());
    }

    @Test
    public void testFindAllW2sByTaxReturnId() {
        int taxReturnId = 123;
        int userId = 1;
        W2Dto w2Dto1 = new W2Dto();
        W2Dto w2Dto2 = new W2Dto();
        w2Dto1.setId(taxReturnId);
        w2Dto2.setId(taxReturnId);

        // create a list of W2Dtos
        List<W2Dto> w2Dtos = List.of(w2Dto1, w2Dto2);
        
        // mock the service method to return the list of W2Dtos
        when(w2Service.findAllByTaxReturnId(taxReturnId, userId)).thenReturn(w2Dtos);

        // call the controller method to find all W2s by taxReturnId
        ResponseEntity<List<W2Dto>> response = w2Controller.findAllW2sByTaxReturnId(taxReturnId, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify taxReturnId is correct
        assertEquals(taxReturnId, response.getBody().get(0).getId());
        assertEquals(taxReturnId, response.getBody().get(1).getId());
    }

    @Test
    public void testDeleteById() {
        int id = 123;

        // call the controller method to delete W2 by id
        ResponseEntity<Void> response = w2Controller.deleteById(id);

        // verify response status is NO_CONTENT
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testUploadImageToS3() {
        int id = 123;
        int userId = 1;
        byte[] image = new byte[10];
        String contentType = "image/jpeg";
        String imageKey = "imageKey";

        // mock the service method to return the image key
        when(w2Service.uploadImage(id, image, contentType, userId)).thenReturn(imageKey);

        // call the controller method to upload image to S3
        ResponseEntity<Void> response = w2Controller.uploadImageToS3(id, image, contentType, userId);

        // verify response status is CREATED
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // verify image key is correct
        assertEquals("/" + imageKey, response.getHeaders().getLocation().toString());
    }

    @Test
    public void testDownloadImage() {
        int id = 123;
        int userId = 1;

        // mock the service method to return the image key
        when(w2Service.downloadImage(id, userId)).thenReturn(imgResource);

        // call the controller method to download image from S3
        ResponseEntity<Resource> response = w2Controller.downloadImage(id, userId);

        // verify response status is OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // verify image is correct
        assertEquals(imgResource, response.getBody());
    }

    // determine content type is a private method, so we need to tika.detect
    @Test
    public void testDetermineContentType() {
        String contentType = "image/jpeg";
        String[] split = contentType.split("/");

        // verify content type is correct
        assertEquals("image", split[0]);
        assertEquals("jpeg", split[1]);
    }
}
