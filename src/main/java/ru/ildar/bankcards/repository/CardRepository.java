package ru.ildar.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ildar.bankcards.entity.Card;
import ru.ildar.bankcards.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findByOwner(User owner, Pageable pageable);

    // ДОБАВЛЕНО:
    Page<Card> findAllByOwnerUsername(String username, Pageable pageable);

    Optional<Card> findByEncryptedNumber(String encryptedNumber);

    boolean existsByEncryptedNumber(String encryptedNumber);
}
