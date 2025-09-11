package com.springtest.cookapi.integration;

import com.springtest.cookapi.infrastructure.services.cloudinary.CloudinaryService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestCloudinaryConfig {

    @Bean
    @Primary
    public CloudinaryService mockCloudinaryService() {
        CloudinaryService mockService = Mockito.mock(CloudinaryService.class);
        Map<String, String> uploadResultMock = new HashMap<>();

        uploadResultMock.put("public_id", "test_image_123");
        uploadResultMock.put("secure_url", "https://test-cloudinary.com/test-image.jpg");

        try {
            when(mockService.AddImageToCloudinary(any(MultipartFile.class)))
                    .thenReturn(uploadResultMock);

            doNothing().when(mockService).DeleteImageFromCloudinary(anyString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mockService;
    }
}