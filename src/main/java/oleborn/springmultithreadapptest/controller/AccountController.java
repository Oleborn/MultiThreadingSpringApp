package oleborn.springmultithreadapptest.controller;


import lombok.RequiredArgsConstructor;
import oleborn.springmultithreadapptest.model.Account;
import oleborn.springmultithreadapptest.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public Account getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id); // Возвращает аккаунт по ID
    }

    @PostMapping("/create")
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account); // Создает новый аккаунт
    }

    @PutMapping("/update/{id}")
    public Account updateAccount(@PathVariable Long id, @RequestBody Account account) {
        return accountService.updateAccount(id, account); // Обновляет аккаунт по ID
    }

    @DeleteMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id); // Удаляет аккаунт
        return "Аккаунт с ID " + id + " удален.";
    }

    @GetMapping("/balance/{accountNumber}")
    public BigDecimal getBalance(@PathVariable String accountNumber) {
        return accountService.getBalance(accountNumber); // Возвращает сумму на счету аккаунта по ID
    }

    @PostMapping("/transfer")
    public CompletableFuture<String> transferCompletable(@RequestParam String fromAccount,
                                                         @RequestParam String toAccount,
                                                         @RequestParam BigDecimal amount) {
        return accountService.transferAsync(fromAccount, toAccount, amount)
                .thenApply(v -> "Запрос на перевод денежных средств обработан.");  // Возвращает сообщение по завершению.
    }
}
