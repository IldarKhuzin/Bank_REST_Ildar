package ru.ildar.bankcards.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ildar.bankcards.dto.request.CardCreateDto;
import ru.ildar.bankcards.dto.response.CardResponseDto;
import ru.ildar.bankcards.entity.Card;
import ru.ildar.bankcards.entity.CardStatus;
import ru.ildar.bankcards.entity.User;
import ru.ildar.bankcards.exception.CardOperationException;
import ru.ildar.bankcards.repository.CardRepository;
import ru.ildar.bankcards.repository.UserRepository;
import ru.ildar.bankcards.util.CardNumberEncryptor;
import ru.ildar.bankcards.util.CardNumberMasker;
import ru.ildar.bankcards.util.SecurityUtil;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberEncryptor cardNumberEncryptor;
    private final CardNumberMasker cardNumberMasker;

    private String generateCardNumber() {
        return String.format("%04d %04d %04d %04d",
                (int) (Math.random() * 10000),
                (int) (Math.random() * 10000),
                (int) (Math.random() * 10000),
                (int) (Math.random() * 10000));
    }

    @Override
    public CardResponseDto createCard(CardCreateDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new CardOperationException("Пользователь не найден"));

        String plainNumber = generateCardNumber();
        String encryptedNumber = cardNumberEncryptor.encrypt(plainNumber);

        Card card = Card.builder()
                .encryptedNumber(encryptedNumber)
                .owner(user)
                .expirationDate(dto.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(dto.getInitialBalance())
                .build();

        card = cardRepository.save(card);

        String maskedNumber = cardNumberMasker.mask(plainNumber);

        return CardResponseDto.builder()
                .id(card.getId())
                .number(maskedNumber)
                .ownerUsername(user.getUsername())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    @Override
    public CardResponseDto getCardById(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardOperationException("Карта не найдена"));

        String plainNumber = cardNumberEncryptor.decrypt(card.getEncryptedNumber());
        String maskedNumber = cardNumberMasker.mask(plainNumber);

        return CardResponseDto.builder()
                .id(card.getId())
                .number(maskedNumber)
                .ownerUsername(card.getOwner().getUsername())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    @Override
    public Page<CardResponseDto> getCardsForUser(String username, Pageable pageable) {
        return cardRepository.findAllByOwnerUsername(username, pageable)
                .map(card -> {
                    String plainNumber = cardNumberEncryptor.decrypt(card.getEncryptedNumber());
                    String maskedNumber = cardNumberMasker.mask(plainNumber);

                    return CardResponseDto.builder()
                            .id(card.getId())
                            .number(maskedNumber)
                            .ownerUsername(card.getOwner().getUsername())
                            .expirationDate(card.getExpirationDate())
                            .status(card.getStatus())
                            .balance(card.getBalance())
                            .build();
                });
    }

    @Override
    public CardResponseDto blockCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardOperationException("Карта не найдена"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        String plainNumber = cardNumberEncryptor.decrypt(card.getEncryptedNumber());
        String maskedNumber = cardNumberMasker.mask(plainNumber);

        return CardResponseDto.builder()
                .id(card.getId())
                .number(maskedNumber)
                .ownerUsername(card.getOwner().getUsername())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    @Override
    public CardResponseDto activateCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardOperationException("Карта не найдена"));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardOperationException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        String plainNumber = cardNumberEncryptor.decrypt(card.getEncryptedNumber());
        String maskedNumber = cardNumberMasker.mask(plainNumber);

        return CardResponseDto.builder()
                .id(card.getId())
                .number(maskedNumber)
                .ownerUsername(card.getOwner().getUsername())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    @Override
    public void deleteCard(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardOperationException("Карта не найдена");
        }
        cardRepository.deleteById(cardId);
    }

    @Override
    public Card getCardEntityById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardOperationException("Карта не найдена"));
    }

    @Override
    public Page<CardResponseDto> getUserCards(int page, int size) {
        // Получаем username из контекста безопасности
        // Здесь лучше прокинуть username из SecurityContext в контроллер, но для примера:
        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new CardOperationException("Пользователь не аутентифицирован"));

        Pageable pageable = PageRequest.of(page, size);

        return getCardsForUser(username, pageable);
    }

    @Override
    public void requestCardBlock(UUID id) {
        // Проверяем, что карта принадлежит текущему пользователю
        Card card = getCardEntityById(id);

        String username = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new CardOperationException("Пользователь не аутентифицирован"));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new CardOperationException("Нет доступа к данной карте");
        }

        // Логика заявки на блокировку карты (например, можно создать задачу, отправить уведомление)
        // Пока просто меняем статус на BLOCKED с каким-то условием
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
}                              