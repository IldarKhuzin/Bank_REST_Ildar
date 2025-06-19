package ru.ildar.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDto {

    private String accessToken;
    private String tokenType = "Bearer";
    private String token;
}
