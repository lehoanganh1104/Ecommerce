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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    IUserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUserNameIgnoreCaseAndDeletedFalse(request.getUserName())) {
            throw new AppException(ErrException.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrException.EMAIL_EXISTED);
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrException.PHONE_NUMBER_EXISTED);
        }

        if (userRepository.existsByUserNameIgnoreCaseAndDeletedFalse(request.getUserName())){
            throw new AppException(ErrException.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PostAuthorize("returnObject.userName == authentication.name or hasRole('ROLE_ADMIN')")
    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));

        if (!request.getEmail().equals(user.getEmail())) {
            boolean existsEmail = userRepository.existsByEmailAndDeletedFalse(request.getEmail());
            if (existsEmail) {
                throw new AppException(ErrException.EMAIL_EXISTED);
            }
            user.setEmail(request.getEmail());
        }

        if (!request.getPhoneNumber().equals(user.getPhoneNumber())) {
            boolean existsPhone = userRepository.existsByPhoneNumberAndDeletedFalse(request.getPhoneNumber());
            if (existsPhone) {
                throw new AppException(ErrException. PHONE_NUMBER_EXISTED);
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
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));

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
            users = userRepository.findByUserNameContainingIgnoreCaseAndDeletedFalse(search, pageable);
        }
        return users.map(userMapper::toUserResponse);
    }

    @PostAuthorize("returnObject.userName == authentication.name or hasRole('ROLE_ADMIN')")
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrException.USER_NOT_EXISTED));

        user.setDeleted(true);
        userRepository.save(user);
    }
}
