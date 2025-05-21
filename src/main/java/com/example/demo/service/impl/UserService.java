package com.example.demo.service.impl;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateRoleRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrException;
import com.example.demo.mapper.IUserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    FileService fileService;
    IUserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCaseAndDeletedFalse(request.getUsername())) {
            throw new AppException(ErrException.USER_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrException.EMAIL_ALREADY_USED);
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrException.PHONE_NUMBER_ALREADY_USED);
        }

        if (userRepository.existsByUsernameIgnoreCaseAndDeletedFalse(request.getUsername())){
            throw new AppException(ErrException.USER_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("returnObject.userName == authentication.name or hasRole('ROLE_ADMIN')")
    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        if (!request.getEmail().equals(user.getEmail())) {
            boolean existsEmail = userRepository.existsByEmailAndDeletedFalse(request.getEmail());
            if (existsEmail) {
                throw new AppException(ErrException.EMAIL_ALREADY_USED);
            }
            user.setEmail(request.getEmail());
        }

        if (!request.getPhoneNumber().equals(user.getPhoneNumber())) {
            boolean existsPhone = userRepository.existsByPhoneNumberAndDeletedFalse(request.getPhoneNumber());
            if (existsPhone) {
                throw new AppException(ErrException. PHONE_NUMBER_ALREADY_USED);
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public Page<UserResponse> getAllUser(String search, Pageable pageable) {
        Page<User> users;

        if (search == null || search.isBlank()){
            users = userRepository.findAllByDeletedFalse(pageable);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        }
        return users.map(userMapper::toUserResponse);
    }
    
    @PreAuthorize("returnObject.userName == authentication.name or hasRole('ROLE_ADMIN')")
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("isAuthenticated()")
    public UserResponse uploadUserImage(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User user = userRepository.findByUsernameAndDeletedFalse(userName)
                .orElseThrow(()-> new AppException(ErrException.USER_NOT_FOUND));
        String newImage = fileService.replaceImage(user.getImageUrl(), file, "users");
        user.setImageUrl(newImage);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_FOUND));

        user.setDeleted(true);
        userRepository.save(user);
    }
}
