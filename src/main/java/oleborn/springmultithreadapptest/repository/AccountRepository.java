package oleborn.springmultithreadapptest.repository;

import oleborn.springmultithreadapptest.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//JpaRepository: Предоставляет базовые методы для работы с базой данных (CRUD).
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber); // Поиск по номеру счета.
}
