package com.example.demo.controller;

import com.example.demo.common.constants.SuccessMessage;
import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateRoleRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request){
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message(SuccessMessage.USER_CREATED)
                .data(response)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> response = userService.getAllUser(search, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USERS_FETCHED)
                .data(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id){
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_FETCHED)
                .data(response)
                .build()
            );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(@Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_UPDATED)
                .data(response)
                .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_FETCHED)
                .data(response)
                .build());
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request){
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PutMapping("updateRole/{id}")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse response = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_UPDATED)
                .data(response)
                .build()
        );
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<Void>> uploadUserImage(@RequestParam("file") MultipartFile file) {

        userService.uploadUserImage(file);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_IMAGE_UPLOADED)
                .data(null)
                .build());
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(SuccessMessage.USER_DELETED)
                .data(null)
                .build()
        );
    }
}
