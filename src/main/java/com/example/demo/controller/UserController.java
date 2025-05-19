package com.example.demo.controller;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateRoleRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.AppException;
import com.example.demo.service.IUserService;
import com.example.demo.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request){
        try {
            UserResponse response = userService.createUser(request);
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex){
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<UserResponse> response = userService.getAllUser(search, pageable);
            ApiResponse<Page<UserResponse>> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex){
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id){
        try {
            UserResponse response = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AppException ex){
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request){
        try {
            UserResponse response = userService.updateUser(id, request);
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex){
            ApiResponse<?> apiResponse = ApiResponse.error(ex.getErrException().getCode(), ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PutMapping("updateRole/{id}")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            UserResponse response = userService.updateUserRole(id, request);
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(response);
            return ResponseEntity.ok(apiResponse);
        } catch (AppException ex) {
            ApiResponse<?> apiResponse = ApiResponse.error(
                    ex.getErrException().getCode(),
                    ex.getErrException().getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (AppException ex){
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    ex.getErrException().getCode(),
                    ex.getErrException().getMessage()
            ));
        }
    }
}
