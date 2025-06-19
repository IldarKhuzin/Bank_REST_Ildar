package ru.ildar.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequestDto {

    @NotNull
    private UUID fromCardId;

    @NotNull
    private UUID toCardId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
