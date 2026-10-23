package com.vkcreation.platform.auth;

import com.vkcreation.platform.security.JwtService;
import com.vkcreation.platform.user.UserAccount;
import com.vkcreation.platform.user.UserAccountRepository;
import com.vkcreation.platform.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        this.users=users; this.passwordEncoder=passwordEncoder; this.authenticationManager=authenticationManager;
        this.jwtService=jwtService; this.expirationMs=expirationMs;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        UserAccount user = users.save(new UserAccount(UUID.randomUUID().toString(), request.fullName().trim(), email,
                request.phone()==null ? null : request.phone().trim(), passwordEncoder.encode(request.password()), UserRole.CLIENT));
        return response(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        return response((UserAccount) authentication.getPrincipal());
    }

    private AuthDtos.AuthResponse response(UserAccount user) {
        return new AuthDtos.AuthResponse(jwtService.generateToken(user), "Bearer", expirationMs,
                user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
