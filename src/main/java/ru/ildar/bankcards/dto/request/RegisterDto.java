package ru.ildar.bankcards.dto.request;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String password;
    private String email;
}
