package com.springtest.cookapi.infrastructure.services.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.springtest.cookapi.domain.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements ICloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(@Value("${app.cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public Map addImageToCloudinary(MultipartFile file) throws IOException {
        Map params1 = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", true,
                "folder", "cook-api"
        );

        validateFile(file);

        return cloudinary.uploader().upload(file.getBytes(), params1);
    }

    public void deleteImageFromCloudinary(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("PublicId nie może być pusty");
        }

        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        if (!"ok".equals(deleteResult.get("result"))) {
            throw new BadRequestException("Delete image failed");
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        validateImageType(file);

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File exceeded 10MB");
        }
    }

    private void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/jpg") &&
                        !contentType.equals("image/png"))) {

            throw new BadRequestException("Only images are allowed");
        }

        if (fileName != null) {
            String fileExtension = getFileExtension(fileName).toLowerCase();
            if (!fileExtension.equals("jpg") &&
                    !fileExtension.equals("jpeg") &&
                    !fileExtension.equals("png")) {

                throw new BadRequestException("Only images are allowed");
            }
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }
}
