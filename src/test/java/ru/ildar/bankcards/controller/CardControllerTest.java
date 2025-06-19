package ru.ildar.bankcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.ildar.bankcards.dto.request.CardCreateDto;
import ru.ildar.bankcards.dto.request.TransferRequestDto;
import ru.ildar.bankcards.dto.response.CardResponseDto;
import ru.ildar.bankcards.entity.CardStatus;
import ru.ildar.bankcards.security.JwtAuthFilter;
import ru.ildar.bankcards.security.JwtTokenProvider;
import ru.ildar.bankcards.service.CardService;
import ru.ildar.bankcards.service.TransferService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCard_shouldReturnCardResponse() throws Exception {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(UUID.randomUUID());
        dto.setExpirationDate(LocalDate.now().plusYears(1));
        dto.setInitialBalance(new BigDecimal("1000"));

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(UUID.randomUUID())
                .number("**** **** **** 1234")
                .ownerUsername("user")
                .expirationDate(dto.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(dto.getInitialBalance())
                .build();

        Mockito.when(cardService.createCard(Mockito.any(CardCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerUsername").value("user"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.expirationDate").value(dto.getExpirationDate().toString()))
                .andExpect(jsonPath("$.balance").value(dto.getInitialBalance().doubleValue()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void blockCard_shouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .number("**** **** **** 1234")
                .ownerUsername("admin")
                .expirationDate(LocalDate.now().plusYears(3))
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.ZERO)
                .build();

        Mockito.when(cardService.blockCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(put("/api/cards/{id}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void activateCard_shouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();

        // Если activateCard возвращает void, используйте doNothing()
        Mockito.doNothing().when(cardService).activateCard(cardId);

        mockMvc.perform(put("/api/cards/{id}/activate", cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void activateCard_shouldReturnCard() throws Exception {
        UUID cardId = UUID.randomUUID();

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .number("**** **** **** 1234")
                .ownerUsername("user")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000"))
                .build();

        Mockito.when(cardService.activateCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(put("/api/cards/{id}/activate", cardId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCard_shouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/cards/{id}", cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getUserCards_shouldReturnPage() throws Exception {
        CardResponseDto dto = CardResponseDto.builder()
                .id(UUID.randomUUID())
                .number("**** **** **** 1234")
                .ownerUsername("user")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000"))
                .build();

        Page<CardResponseDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(cardService.getUserCards(0, 5)).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].ownerUsername").value("user"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void requestCardBlock_shouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();

        Mockito.doNothing().when(cardService).requestCardBlock(cardId);

        mockMvc.perform(post("/api/cards/{id}/request-block", cardId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void transfer_shouldReturnOk() throws Exception {
        TransferRequestDto dto = new TransferRequestDto();
        dto.setFromCardId(UUID.randomUUID());
        dto.setToCardId(UUID.randomUUID());
        dto.setAmount(new BigDecimal("100"));

        Mockito.doNothing().when(transferService).transfer(Mockito.any(TransferRequestDto.class));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void blockCard_shouldReturnCard() throws Exception {
        UUID cardId = UUID.randomUUID();

        CardResponseDto responseDto = CardResponseDto.builder()
                .id(cardId)
                .number("**** **** **** 1234")
                .ownerUsername("user")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.BLOCKED)
                .balance(new BigDecimal("1000"))
                .build();

        Mockito.when(cardService.blockCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(put("/api/cards/{id}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.number").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerUsername").value("user"));
    }


}
