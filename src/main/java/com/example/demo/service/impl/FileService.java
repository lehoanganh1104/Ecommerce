package com.example.demo.service.impl;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IProductImageMapper;
import com.example.demo.mapper.IUserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    UserRepository userRepository;
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;
    IUserMapper userMapper;
    IProductImageMapper productImageMapper;

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
                System.out.println("Đã tạo thư mục: " + uploadDir.toAbsolutePath());
            } catch (IOException e){
                throw new AppException(ErrException.DIRECTORY_CREATION_FAILED);
            }
        }
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFileName);
        try{
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Ảnh đã lưu vào: " + destination.toAbsolutePath()); // In ra đường dẫn ảnh
        } catch (IOException e) {
            throw new AppException(ErrException.FILE_STORE_FAILED);
        }
        return uniqueFileName;
    }

    @PreAuthorize("isAuthenticated()")
    public UserResponse uploadUserImage(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User user = userRepository.findByUserNameAndDeletedFalse(userName)
                .orElseThrow(()-> new AppException(ErrException.USER_NOT_FOUND));
        String fileName = storeImage(file, "userImage");
        user.setImageUrl(fileName);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }
}
