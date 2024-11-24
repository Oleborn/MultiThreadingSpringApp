package oleborn.springmultithreadapptest.exception;


import oleborn.springmultithreadapptest.utils.CustomExceptionsMessages;

public class AccountNotFountByIDException extends RuntimeException {
    public AccountNotFountByIDException() {
        super(CustomExceptionsMessages.ACCOUNT_NOT_FOUNT_BY_ID.getMessage());
    }
}
