package com.example.demo.controller;

import com.example.demo.custom.CloudinaryService;
import com.example.demo.dto.upload.UploadResponse;
import com.example.demo.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping
    public ApiResponse<UploadResponse> upload(@RequestParam MultipartFile file) throws IOException {
        Optional<UploadResponse> dataUpload = cloudinaryService.uploadFile(file);
        return ApiResponse.success("Upload successful", dataUpload.orElse(null));
    }
    @DeleteMapping
    public ApiResponse<String> delete(@RequestParam("publicId") String publicId)throws IOException {
        String result = cloudinaryService.deleteFile(publicId);
        return ApiResponse.success(result);
    }
}
