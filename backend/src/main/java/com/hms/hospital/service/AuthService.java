package com.hms.hospital.service;

import com.hms.hospital.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);
}
