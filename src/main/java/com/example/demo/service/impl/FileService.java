package com.example.demo.service.impl;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IUserMapper;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    UserRepository userRepository;
    IUserMapper userMapper;

    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType!= null && contentType.startsWith("image/");
    }

    public String storeImage(MultipartFile file, String subDirectory) {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new AppException(ErrException.FILE_NOT_PROVIDED);
        }
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID() + "_"  + fileName;
        java.nio.file.Path uploadDir = Paths.get("uploads", subDirectory);
        if (!Files.exists(uploadDir)) {
            try{
                Files.createDirectories(uploadDir);
                System.out.println("Create folder: " + uploadDir.toAbsolutePath());
            } catch (IOException e){
                throw new AppException(ErrException.DIRECTORY_CREATION_FAILED);
            }
        }
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFileName);
        try{
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image saved into: " + destination.toAbsolutePath());
        } catch (IOException e) {
            throw new AppException(ErrException.FILE_STORE_FAILED);
        }
        return uniqueFileName;
    }

    public void deleteImage(String fileName, String subDirectory) {
        if (fileName == null || fileName.isBlank()) return;

        Path imagePath = Paths.get("uploads", subDirectory, fileName);
        try {
            Files.deleteIfExists(imagePath);
            System.out.println("Image deleted: " + imagePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Can't delete: " + imagePath.toAbsolutePath());
        }
    }

    public String replaceImage(String oldFileName, MultipartFile newFile, String subDirectory) {
        deleteImage(oldFileName, subDirectory);
        return storeImage(newFile, subDirectory);
    }
}
