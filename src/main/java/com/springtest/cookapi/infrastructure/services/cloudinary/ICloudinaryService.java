package com.springtest.cookapi.infrastructure.services.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ICloudinaryService {
    Map addImageToCloudinary(MultipartFile file) throws IOException;
    void deleteImageFromCloudinary(String publicId) throws IOException;
}
