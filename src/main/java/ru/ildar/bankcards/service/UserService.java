package ru.ildar.bankcards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ildar.bankcards.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User findById(UUID userId);

    User findByUsername(String username);

    Page<User> findAll(Pageable pageable);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(UUID userId);

    // Добавим этот метод для контроллера
    List<User> getAllUsers();

    // Метод, соответствующий getUserById в контроллере
    User getUserById(UUID userId);
}
