package ru.ildar.bankcards.service;

import ru.ildar.bankcards.dto.request.LoginDto;
import ru.ildar.bankcards.dto.request.RegisterDto;
import ru.ildar.bankcards.dto.response.JwtResponseDto;

public interface AuthService {

    JwtResponseDto register(RegisterDto dto);

    JwtResponseDto login(LoginDto loginDto);
}
