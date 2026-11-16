package com.hms.hospital.service;

import com.hms.hospital.dto.request.UserCreateRequest;
import com.hms.hospital.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    List<UserResponse> findAll();
}
