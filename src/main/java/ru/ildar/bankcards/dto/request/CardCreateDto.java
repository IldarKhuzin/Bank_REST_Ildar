package ru.ildar.bankcards.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CardCreateDto {

    @NotNull
    private UUID userId;

    @Future
    @NotNull
    private LocalDate expirationDate;

    @NotNull
    private BigDecimal initialBalance;
}
