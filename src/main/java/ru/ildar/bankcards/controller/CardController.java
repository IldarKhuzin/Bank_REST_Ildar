package ru.ildar.bankcards.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ildar.bankcards.dto.request.CardCreateDto;
import ru.ildar.bankcards.dto.request.TransferRequestDto;
import ru.ildar.bankcards.dto.response.CardResponseDto;
import ru.ildar.bankcards.service.CardService;
import ru.ildar.bankcards.service.TransferService;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;

    // ADMIN: создание карты
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateDto dto) {
        return ResponseEntity.ok(cardService.createCard(dto));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<Void> blockCard(@PathVariable UUID id) {
        cardService.blockCard(id);
        return ResponseEntity.noContent().build(); // 204 без тела
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    // USER: просмотр своих карт
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponseDto>> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(cardService.getUserCards(page, size));
    }

    // USER: запрос блокировки своей карты
    @PostMapping("/{id}/request-block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> requestCardBlock(@PathVariable UUID id) {
        cardService.requestCardBlock(id);
        return ResponseEntity.ok().build();
    }

    // USER: перевод между своими картами
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequestDto dto) {
        transferService.transfer(dto);
        return ResponseEntity.ok().build();
    }
}
