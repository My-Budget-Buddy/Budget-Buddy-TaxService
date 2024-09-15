package com.skillstorm.taxservice.services;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    private final String imageBucket = "test-bucket"; // Mocked environment value

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        s3Service = new S3Service(s3Client, imageBucket);
    }

  @Test
    public void testUploadFile() {
        // Prepare input
        String key = "test-file.txt";
        byte[] fileContent = "file content".getBytes();

        // Act
        s3Service.uploadFile(key, fileContent);

        // Capture the arguments
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        // Verify the method call
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        // Verify PutObjectRequest
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(imageBucket, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());

        // Verify RequestBody contains the correct file content
        RequestBody capturedRequestBody = requestBodyCaptor.getValue();
        try {
          byte[] capturedBytes = capturedRequestBody.contentStreamProvider().newStream().readAllBytes();
          assertArrayEquals(fileContent, capturedBytes);  // Ensure the byte arrays match
        } catch (Exception e) {
          System.out.println(e);
        }
    }

    @Test
    public void testGetObject() {
        // Prepare input
        String key = "test-file.txt";

        // Mock the GetObjectResponse and ResponseInputStream
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);
        ResponseInputStream<GetObjectResponse> mockResponseInputStream = mock(ResponseInputStream.class);

        // Mock the S3Client to return the mock ResponseInputStream
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseInputStream);

        // Act
        InputStream result = s3Service.getObject(key);

        // Capture the request
        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(requestCaptor.capture());

        // Verify request
        GetObjectRequest request = requestCaptor.getValue();
        assertEquals(imageBucket, request.bucket());
        assertEquals(key, request.key());

        // Verify the result
        assertNotNull(result);
        assertSame(mockResponseInputStream, result); // Ensure the returned stream is the mock
    }
}
