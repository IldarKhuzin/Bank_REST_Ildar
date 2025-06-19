package ru.ildar.bankcards.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ildar.bankcards.dto.request.CardCreateDto;
import ru.ildar.bankcards.dto.response.CardResponseDto;
import ru.ildar.bankcards.entity.Card;
import ru.ildar.bankcards.exception.CardOperationException;

import java.util.UUID;

public interface CardService {

    CardResponseDto createCard(CardCreateDto cardCreateDto);

    CardResponseDto getCardById(UUID cardId);

    Page<CardResponseDto> getCardsForUser(String username, Pageable pageable);

    CardResponseDto blockCard(UUID cardId) throws CardOperationException;

    CardResponseDto activateCard(UUID cardId) throws CardOperationException;

    void deleteCard(UUID cardId) throws CardOperationException;

    Card getCardEntityById(UUID cardId) throws CardOperationException;

    Page<CardResponseDto> getUserCards(int page, int size);

    void requestCardBlock(UUID id);
}