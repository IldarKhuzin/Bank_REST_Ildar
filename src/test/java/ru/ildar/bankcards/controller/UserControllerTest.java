package ru.ildar.bankcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.ildar.bankcards.entity.User;
import ru.ildar.bankcards.security.JwtTokenProvider;
import ru.ildar.bankcards.security.UserDetailsServiceImpl;
import ru.ildar.bankcards.service.UserService;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_shouldReturnList() throws Exception {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .build();

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_shouldReturnUser() throws Exception {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .username("user2")
                .build();

        Mockito.when(userService.getUserById(id)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/users/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

    }
}
