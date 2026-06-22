package com.aiexpensetracker.expense_tracker.auth;

import com.aiexpensetracker.expense_tracker.auth.dto.AuthResponse;
import com.aiexpensetracker.expense_tracker.auth.dto.LoginRequest;
import com.aiexpensetracker.expense_tracker.auth.dto.RegisterRequest;
import com.aiexpensetracker.expense_tracker.category.CategoryService;
import com.aiexpensetracker.expense_tracker.user.User;
import com.aiexpensetracker.expense_tracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CategoryService categoryService; // ← add this field

    public AuthResponse register(RegisterRequest request) {
        // check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // create user — password is hashed, never stored plain
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        categoryService.seedDefaultCategories(user);

        // generate token for immediate login after register
        var token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // this throws automatically if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // if we reach here — credentials are correct
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }


}