package com.ecomerce.app.controller;

import com.ecomerce.app.dto.UserRequest;
import com.ecomerce.app.dto.UserResponse;
import com.ecomerce.app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        UserResponse res = userService.create(request);
        return ResponseEntity.ok(res);
    }
}
