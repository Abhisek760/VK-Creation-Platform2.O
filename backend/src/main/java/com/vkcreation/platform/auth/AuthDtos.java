package com.vkcreation.platform.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@NotBlank @Size(max=160) String fullName,
                                  @Email @NotBlank @Size(max=254) String email,
                                  @Size(max=32) String phone,
                                  @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresInMs, String userId,
                               String fullName, String email, String role) {}
}
