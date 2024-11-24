package oleborn.springmultithreadapptest.service;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oleborn.springmultithreadapptest.exception.AccountNotFountByIDException;
import oleborn.springmultithreadapptest.exception.AccountNotFountByNumberException;
import oleborn.springmultithreadapptest.exception.BalanceException;
import oleborn.springmultithreadapptest.model.Account;
import oleborn.springmultithreadapptest.repository.AccountRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Сервис для управления банковскими операциями, включая асинхронные переводы.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    /*
    Карта, которая связывает каждый идентификатор счета
    (fromAccount или toAccount) с объектом блокировки (ReentrantLock).

    Используется для хранения одной блокировки на каждый аккаунт, чтобы гарантировать,
    что операции с одним и тем же счетом не могут выполняться одновременно из нескольких потоков.
     */
    private final Map<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();

    // Пул потоков для обработки операций
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(AccountNotFountByIDException::new);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFountByNumberException::new);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account); // Сохраняет новый аккаунт
    }

    public Account updateAccount(Long id, Account updatedAccount) {
        Account account = getAccountById(id);
        account.setAccountNumber(updatedAccount.getAccountNumber());
        account.setBalance(updatedAccount.getBalance());
        account.setOwnerName(updatedAccount.getOwnerName());
        return accountRepository.save(account); // Сохраняет изменения
    }

    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFountByIDException();
        }
        accountRepository.deleteById(id); // Удаляет аккаунт
    }

    public BigDecimal getBalance(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        return account.getBalance();
    }

    /**
     * Выполняет асинхронный перевод денежных средств между двумя счетами.
     * <p>
     * Метод использует асинхронное выполнение задачи с помощью {@link CompletableFuture}.
     * Он обеспечивает безопасность потоков с использованием реентрантных блокировок
     * {@link java.util.concurrent.locks.ReentrantLock}, которые предотвращают одновременные
     * операции над одними и теми же счетами.
     * </p>
     *
     * @param fromAccount номер счета отправителя.
     * @param toAccount   номер счета получателя.
     * @param amount      сумма перевода, представлена в формате {@link BigDecimal}.
     * @return {@link CompletableFuture}, представляющий завершение операции. Результат выполнения
     * операции может быть либо строкой с подтверждением, либо исключением.
     * @throws BalanceException                 если баланс отправителя недостаточен для выполнения перевода.
     * @throws AccountNotFountByNumberException если один из счетов не найден в базе данных.
     * @throws Exception                        другие исключения, которые могут возникнуть при выполнении операции.
     *                                          <p>
     */

    public CompletableFuture<Void> transferAsync(String fromAccount, String toAccount, BigDecimal amount) {
        // Возвращаем CompletableFuture, который выполняет задачу асинхронно
        return CompletableFuture.supplyAsync(() ->
                {  // Асинхронное выполнение с возвратом результата.

                    /*
                    В пессимистичной блокировке при чтении данных захватывается блокировка, которая
                    предотвращает изменение этих данных другими потоками, пока текущая транзакция не завершится.
                    Этот подход может уменьшить вероятность возникновения ошибок, связанных с оптимистичной блокировкой.

                    Метод computeIfAbsent — это метод интерфейса Map в Java, который:
                    Проверяет, есть ли в карте значение для заданного ключа (fromAccount или toAccount).
                    Если значение уже существует, оно возвращается.
                    Если значение отсутствует, выполняется переданная функция (k -> new ReentrantLock()),
                    которая создает новое значение (в данном случае новый ReentrantLock), добавляет его в карту,
                    и затем возвращает это значение.

                    Таким образом:

                    Если блокировка для указанного счета уже существует, она используется.
                    Если блокировка еще не создана, она создается и добавляется в карту.

                    ReentrantLock — это класс из пакета java.util.concurrent.locks, представляющий более гибкую
                    альтернативу традиционным синхронизированным блокам (synchronized).

                    Особенности ReentrantLock:
                    Реентрантность: Если поток уже захватил блокировку, он может повторно захватить ее без блокировки
                    самого себя.
                    Гибкость: Поддерживает возможность проверки состояния блокировки (например, isLocked()) или попытки
                    захвата блокировки без ожидания (tryLock()).
                    Явное управление: Поток может явно захватывать (lock()) и освобождать (unlock()) блокировку.
                     */
                    String firstLock = fromAccount.compareTo(toAccount) < 0 ? fromAccount : toAccount;
                    String secondLock = fromAccount.equals(firstLock) ? toAccount : fromAccount;

                    ReentrantLock firstAccountLock = accountLocks.computeIfAbsent(firstLock, k -> new ReentrantLock());
                    ReentrantLock secondAccountLock = accountLocks.computeIfAbsent(secondLock, k -> new ReentrantLock());
                    /*
                    При блокировке нескольких объектов следует соблюдать консистентный порядок
                    (например, сортировать идентификаторы счетов), чтобы исключить взаимную блокировку.
                     */


                    firstAccountLock.lock();
                    secondAccountLock.lock();
                    try {
                        // Получаем объект счета отправителя по номеру счета
                        Account sender = getAccountByNumber(fromAccount);

                        // Получаем объект счета получателя по номеру счета
                        Account receiver = getAccountByNumber(toAccount);

                        // Проверяем, хватает ли средств на счете отправителя
                        if (sender.getBalance().compareTo(amount) < 0) {
                            //Если недостаточно средств, выбрасываем исключение
                            throw new BalanceException();
                        }
                        // Уменьшаем баланс отправителя
                        sender.setBalance(sender.getBalance().subtract(amount));

                        // Увеличиваем баланс получателя
                        receiver.setBalance(receiver.getBalance().add(amount));

                        // Сохраняем изменения в базе данных для отправителя
                        accountRepository.save(sender);

                        // Сохраняем изменения в базе данных для получателя
                        accountRepository.save(receiver);

                        // Возвращаем строку с подтверждением завершения перевода
                        return "Перевод денежных средств между " + fromAccount + " и " + toAccount + " на сумму " + amount + ", успешно проведен.";
                    } catch (OptimisticLockingFailureException e) {
                        System.err.println("Optimistic lock failure, retrying...");
                    } finally {
                        secondAccountLock.unlock();
                        firstAccountLock.unlock();
                    }
                    return null;
                    //указываем явное использование пула потоков, чтобы их было не более 10
                }, executorService)
                // 11. Метод thenAccept обрабатывает успешный результат выполнения задачи
                .thenAccept(result -> {
                    // 12. Печатаем результат перевода в лог
                    log.info(result);
                })
                .exceptionally(ex -> {  // 13. Обработка исключений, если задача завершилась с ошибкой
                    // 14. Печатаем сообщение об ошибке в консоль
                    log.error("Перевод не выполнен: {}", ex.getMessage());
                    // Возвращаем, так как exceptionally требует возвращаемого значения
                    if (ex.getCause().getClass().equals(BalanceException.class)) {
                        throw new BalanceException();
                    } else if (ex.getCause().getClass().equals(AccountNotFountByNumberException.class)) {
                        throw new AccountNotFountByNumberException();
                    } else {
                        throw new RuntimeException();
                    }
                });
    }

    //создан в тестовых целях для демонстрации возможностей synchronized
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        Account sender = getAccountByNumber(fromAccount);
        Account receiver = getAccountByNumber(toAccount);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BalanceException();
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);
    }

    //вызывать в специальных случаях, например, при остановке приложения, чтобы корректно завершить выполнение всех задач.
    public void shutdownExecutor() {
        executorService.shutdown(); // Завершает работу пула потоков.
    }
}
