package ru.ildar.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import ru.ildar.bankcards.dto.request.CardCreateDto;
import ru.ildar.bankcards.dto.response.CardResponseDto;
import ru.ildar.bankcards.entity.*;
import ru.ildar.bankcards.exception.CardOperationException;
import ru.ildar.bankcards.repository.CardRepository;
import ru.ildar.bankcards.repository.UserRepository;
import ru.ildar.bankcards.util.CardNumberEncryptor;
import ru.ildar.bankcards.util.CardNumberMasker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberEncryptor cardNumberEncryptor;

    @Mock
    private CardNumberMasker cardNumberMasker;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        UUID userId = UUID.randomUUID();

        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);
        dto.setExpirationDate(LocalDate.now().plusYears(1));
        dto.setInitialBalance(new BigDecimal("1000.00"));

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Подменим генерацию номера, чтобы тест был стабильным
        // Здесь вызов cardService.createCard будет генерить рандомный номер,
        // для теста можно "переопределить" generateCardNumber через spy
        // Но проще мокнуть cardNumberEncryptor и cardNumberMasker.

        // Мокируем шифрование и маскирование
        when(cardNumberEncryptor.encrypt(anyString())).thenReturn("encryptedNumber");
        when(cardNumberMasker.mask(anyString())).thenReturn("**** **** **** 1234");

        Card savedCard = Card.builder()
                .id(UUID.randomUUID())
                .encryptedNumber("encryptedNumber")
                .owner(user)
                .expirationDate(dto.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(dto.getInitialBalance())
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardResponseDto responseDto = cardService.createCard(dto);

        assertNotNull(responseDto);
        assertEquals(savedCard.getId(), responseDto.getId());
        assertEquals("**** **** **** 1234", responseDto.getNumber());
        assertEquals(user.getUsername(), responseDto.getOwnerUsername());
        assertEquals(dto.getExpirationDate(), responseDto.getExpirationDate());
        assertEquals(CardStatus.ACTIVE, responseDto.getStatus());
        assertEquals(dto.getInitialBalance(), responseDto.getBalance());

        verify(userRepository).findById(userId);
        verify(cardRepository).save(any(Card.class));
        verify(cardNumberEncryptor).encrypt(anyString());
        verify(cardNumberMasker).mask(anyString());
    }

    @Test
    void createCard_shouldThrowExceptionIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);
        dto.setExpirationDate(LocalDate.now().plusYears(1));
        dto.setInitialBalance(new BigDecimal("1000.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.createCard(dto));
        assertEquals("Пользователь не найден", ex.getMessage());
    }

    @Test
    void getCardById_shouldReturnCardResponse() {
        UUID cardId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).username("testuser").build();

        Card card = Card.builder()
                .id(cardId)
                .encryptedNumber("encryptedNumber")
                .owner(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardNumberEncryptor.decrypt("encryptedNumber")).thenReturn("1234 5678 9012 3456");
        when(cardNumberMasker.mask("1234 5678 9012 3456")).thenReturn("**** **** **** 3456");

        CardResponseDto dto = cardService.getCardById(cardId);

        assertNotNull(dto);
        assertEquals(cardId, dto.getId());
        assertEquals("**** **** **** 3456", dto.getNumber());
        assertEquals(user.getUsername(), dto.getOwnerUsername());
        assertEquals(card.getExpirationDate(), dto.getExpirationDate());
        assertEquals(CardStatus.ACTIVE, dto.getStatus());
        assertEquals(new BigDecimal("500.00"), dto.getBalance());

        verify(cardRepository).findById(cardId);
    }

    @Test
    void getCardById_shouldThrowExceptionIfNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.getCardById(cardId));
        assertEquals("Карта не найдена", ex.getMessage());
    }

    @Test
    void blockCard_shouldBlockCardSuccessfully() {
        UUID cardId = UUID.randomUUID();
        User user = User.builder().username("user").build();

        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .encryptedNumber("encrypted")
                .owner(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("100"))
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardNumberEncryptor.decrypt("encrypted")).thenReturn("1234 5678 9012 3456");
        when(cardNumberMasker.mask("1234 5678 9012 3456")).thenReturn("**** **** **** 3456");

        CardResponseDto dto = cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, dto.getStatus());

        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_shouldThrowExceptionIfAlreadyBlocked() {
        UUID cardId = UUID.randomUUID();

        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.blockCard(cardId));
        assertEquals("Карта уже заблокирована", ex.getMessage());
    }

    @Test
    void activateCard_shouldActivateCardSuccessfully() {
        UUID cardId = UUID.randomUUID();
        User user = User.builder().username("user").build();

        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.BLOCKED)
                .encryptedNumber("encrypted")
                .owner(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("100"))
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardNumberEncryptor.decrypt("encrypted")).thenReturn("1234 5678 9012 3456");
        when(cardNumberMasker.mask("1234 5678 9012 3456")).thenReturn("**** **** **** 3456");

        CardResponseDto dto = cardService.activateCard(cardId);

        assertEquals(CardStatus.ACTIVE, dto.getStatus());

        verify(cardRepository).save(card);
    }

    @Test
    void activateCard_shouldThrowExceptionIfAlreadyActive() {
        UUID cardId = UUID.randomUUID();

        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.activateCard(cardId));
        assertEquals("Карта уже активна", ex.getMessage());
    }

    @Test
    void deleteCard_shouldDeleteSuccessfully() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(true);

        cardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_shouldThrowExceptionIfNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(false);

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.deleteCard(cardId));
        assertEquals("Карта не найдена", ex.getMessage());
    }

    @Test
    void getCardEntityById_shouldReturnCard() {
        UUID cardId = UUID.randomUUID();

        Card card = Card.builder().id(cardId).build();
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        Card result = cardService.getCardEntityById(cardId);

        assertEquals(card, result);
    }

    @Test
    void getCardEntityById_shouldThrowExceptionIfNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardOperationException ex = assertThrows(CardOperationException.class, () -> cardService.getCardEntityById(cardId));
        assertEquals("Карта не найдена", ex.getMessage());
    }

    @Test
    void getCardsForUser_shouldReturnPage() {
        String username = "user";
        Pageable pageable = PageRequest.of(0, 2);

        User user = User.builder().username(username).build();

        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .encryptedNumber("encrypted1")
                .owner(user)
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .build();

        Card card2 = Card.builder()
                .id(UUID.randomUUID())
                .encryptedNumber("encrypted2")
                .owner(user)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.BLOCKED)
                .balance(new BigDecimal("200"))
                .build();

        List<Card> cards = List.of(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());

        when(cardRepository.findAllByOwnerUsername(username, pageable)).thenReturn(cardPage);
        when(cardNumberEncryptor.decrypt("encrypted1")).thenReturn("1234 5678 9012 3456");
        when(cardNumberEncryptor.decrypt("encrypted2")).thenReturn("2345 6789 0123 4567");
        when(cardNumberMasker.mask("1234 5678 9012 3456")).thenReturn("**** **** **** 3456");
        when(cardNumberMasker.mask("2345 6789 0123 4567")).thenReturn("**** **** **** 4567");

        Page<CardResponseDto> dtoPage = cardService.getCardsForUser(username, pageable);

        assertEquals(2, dtoPage.getTotalElements());

        CardResponseDto firstDto = dtoPage.getContent().get(0);
        assertEquals(card1.getId(), firstDto.getId());
        assertEquals("**** **** **** 3456", firstDto.getNumber());

        CardResponseDto secondDto = dtoPage.getContent().get(1);
        assertEquals(card2.getId(), secondDto.getId());
        assertEquals("**** **** **** 4567", secondDto.getNumber());

        verify(cardRepository).findAllByOwnerUsername(username, pageable);
    }
}
