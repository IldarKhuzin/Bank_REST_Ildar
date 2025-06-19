package ru.ildar.bankcards.service;

import ru.ildar.bankcards.dto.request.LoginDto;
import ru.ildar.bankcards.dto.response.JwtResponseDto;

public interface AuthService {

    JwtResponseDto login(LoginDto loginDto);

}
