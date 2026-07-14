package com.example.attendance.auth;

import java.util.List;

public record AuthUserResponse(
        String username,
        List<String> authorities
) {}
