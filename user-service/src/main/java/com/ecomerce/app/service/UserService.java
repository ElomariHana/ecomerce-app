package com.ecomerce.app.service;

import com.ecomerce.app.dto.UserRequest;
import com.ecomerce.app.dto.UserResponse;
import com.ecomerce.app.model.User;
import com.ecomerce.app.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public UserResponse create (UserRequest request) {

        User user = User.builder().
                firstname(request.getFirstname())
                        .lastname(request.getLastname())
                                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .firstname(savedUser.getFirstname())
                .lastname(savedUser.getLastname())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }
}
