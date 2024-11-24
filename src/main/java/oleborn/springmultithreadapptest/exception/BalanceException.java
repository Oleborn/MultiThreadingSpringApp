package oleborn.springmultithreadapptest.exception;


import oleborn.springmultithreadapptest.utils.CustomExceptionsMessages;

public class BalanceException extends RuntimeException {
    public BalanceException() {
        super(CustomExceptionsMessages.INSUFFICIENT_BALANCE.getMessage());
    }
}
