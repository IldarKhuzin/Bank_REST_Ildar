package ru.ildar.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     * Формат вывода: группы по 4 символа, остальные заменены на '*'.
     * Например: "1234567812345678" -> "**** **** **** 5678"
     *
     * @param cardNumber номер карты в виде строки, только цифры
     * @return маскированный номер
     */
    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Номер карты должен содержать минимум 4 цифры");
        }

        String last4 = cardNumber.substring(cardNumber.length() - 4);
        int maskLength = cardNumber.length() - 4;

        StringBuilder masked = new StringBuilder();

        // Добавляем звёздочки по 4, разделённые пробелами
        int fullGroups = maskLength / 4;
        int remainder = maskLength % 4;

        for (int i = 0; i < fullGroups; i++) {
            masked.append("**** ");
        }

        // Остаток символов звездочками, если есть
        if (remainder > 0) {
            for (int i = 0; i < remainder; i++) {
                masked.append("*");
            }
            masked.append(" ");
        }

        masked.append(last4);

        return masked.toString();
    }
}
