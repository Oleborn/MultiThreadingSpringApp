package oleborn.springmultithreadapptest.exception;


import oleborn.springmultithreadapptest.utils.CustomExceptionsMessages;

public class AccountNotFountByNumberException extends RuntimeException {
    public AccountNotFountByNumberException() {
        super(CustomExceptionsMessages.ACCOUNT_NOT_FOUNT_BY_NUMBER.getMessage());
    }
}
