package ru.ildar.bankcards.service;

import jakarta.validation.Valid;
import ru.ildar.bankcards.dto.request.TransferRequestDto;
import ru.ildar.bankcards.exception.CardOperationException;

import java.util.UUID;

public interface TransferService {

    void transferBetweenCards(UUID userId, TransferRequestDto transferRequestDto) throws CardOperationException;

    void transfer(@Valid TransferRequestDto dto);
}
