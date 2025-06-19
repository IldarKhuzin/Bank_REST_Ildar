package ru.ildar.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ildar.bankcards.dto.request.TransferRequestDto;
import ru.ildar.bankcards.entity.Card;
import ru.ildar.bankcards.entity.CardStatus;
import ru.ildar.bankcards.entity.User;
import ru.ildar.bankcards.exception.CardOperationException;
import ru.ildar.bankcards.repository.CardRepository;
import ru.ildar.bankcards.repository.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void transferBetweenCards(UUID userId, TransferRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CardOperationException("Пользователь не найден"));

        Card fromCard = cardRepository.findById(dto.getFromCardId())
                .orElseThrow(() -> new CardOperationException("Исходящая карта не найдена"));

        Card toCard = cardRepository.findById(dto.getToCardId())
                .orElseThrow(() -> new CardOperationException("Целевая карта не найдена"));

        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new CardOperationException("Перевод возможен только между своими картами");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Исходящая карта не активна");
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Целевая карта не активна");
        }

        BigDecimal amount = dto.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Сумма перевода должна быть положительной");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new CardOperationException("Недостаточно средств на исходящей карте");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Override
    public void transfer(TransferRequestDto dto) {
        // Можно реализовать при необходимости
    }
}
