package ru.ildar.bankcards.dto.responce;

import jdk.jshell.Snippet;
import lombok.*;
import ru.ildar.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {
    private UUID id;
    private String number;
    private String ownerUsername;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
}
