package ru.ildar.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ildar.bankcards.dto.request.LoginDto;
import ru.ildar.bankcards.dto.request.RegisterDto;
import ru.ildar.bankcards.dto.response.JwtResponseDto;
import ru.ildar.bankcards.entity.Role;
import ru.ildar.bankcards.entity.User;
import ru.ildar.bankcards.repository.UserRepository;
import ru.ildar.bankcards.security.JwtProvider;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public JwtResponseDto register(RegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());  // Устанавливаем email
        user.setRoles(Set.of(Role.ROLE_USER));

        user = userRepository.save(user);

        String token = jwtProvider.generateToken(user.getUsername());

        return new JwtResponseDto(token, "Bearer", token);
    }

    @Override
    public JwtResponseDto login(LoginDto loginDto) {
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        String token = jwtProvider.generateToken(user.getUsername());

        return new JwtResponseDto(token, "Bearer", token);
    }
}
