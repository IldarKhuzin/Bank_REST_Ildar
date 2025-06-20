package ru.ildar.bankcards.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ildar.bankcards.dto.request.LoginDto;
import ru.ildar.bankcards.dto.request.RegisterDto;
import ru.ildar.bankcards.dto.response.JwtResponseDto;
import ru.ildar.bankcards.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginDto loginDto) {
        JwtResponseDto jwtResponse = authService.login(loginDto);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody RegisterDto registerDto) {
        JwtResponseDto jwtResponse = authService.register(registerDto);
        return ResponseEntity.ok(jwtResponse);
    }
}
