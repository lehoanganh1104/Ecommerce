package com.example.demo.service;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateRoleRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse updateUserRole(Long userId, UpdateRoleRequest request);
    UserResponse uploadUserImage(MultipartFile file);
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UpdateUserRequest request);
    Page<UserResponse> getAllUser(String search, Pageable pageable);
    void deleteUser(Long id);
    UserResponse getUserById(Long id);
}
