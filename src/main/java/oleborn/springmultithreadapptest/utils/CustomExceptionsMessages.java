package oleborn.springmultithreadapptest.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomExceptionsMessages {
    INSUFFICIENT_BALANCE("Недостаточный баланс для проведения операции!"),
    ACCOUNT_NOT_FOUNT_BY_NUMBER("Аккаунт по указанному номеру счета не найден!"),
    ACCOUNT_NOT_FOUNT_BY_ID("Аккаунт по указанному номеру ID не найден!");
    private String message;
}
