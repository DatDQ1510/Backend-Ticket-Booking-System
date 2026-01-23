package com.example.demo.custom;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.upload.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Optional<UploadResponse> uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto")
            );

            log.info("Upload result: {}", uploadResult);

            // Validate upload result
            if (uploadResult == null || uploadResult.get("secure_url") == null || uploadResult.get("public_id") == null) {
                log.error("Invalid upload result from Cloudinary: {}", uploadResult);
                return Optional.empty();
            }

            UploadResponse response = new UploadResponse();
            response.setUrl(uploadResult.get("secure_url").toString());
            response.setPublicId(uploadResult.get("public_id").toString());

            log.info("✅ File uploaded successfully: url={}, publicId={}", response.getUrl(), response.getPublicId());

            return Optional.of(response);

        } catch (Exception e) {
            log.error("❌ Error uploading file to Cloudinary: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }


    public String deleteFile(String publicId) throws IOException {
        if(publicId == null || publicId.isEmpty()) {
            return "Invalid publicId";
        }
        try{
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return result.get("result").toString();
        }
        catch (Exception e){
            log.error("Error deleting file from Cloudinary: {}", e.getMessage(), e);
            return "Error deleting file";
        }

    }
}
