package com.springtest.cookapi.integration;

import com.springtest.cookapi.infrastructure.services.cloudinary.CloudinaryService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestCloudinaryConfig {

    @Bean
    @Primary
    public CloudinaryService mockCloudinaryService() {
        CloudinaryService mockService = Mockito.mock(CloudinaryService.class);

        try {
            when(mockService.AddImageToCloudinary(any(MultipartFile.class)))
                    .thenReturn("https://test-cloudinary.com/test-image.jpg");

            when(mockService.DeleteImageFromCloudinary(anyString()))
                    .thenReturn(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mockService;
    }
}