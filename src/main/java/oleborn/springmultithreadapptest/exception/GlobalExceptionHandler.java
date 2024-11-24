package oleborn.springmultithreadapptest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFountByIDException.class)
    public ResponseEntity<String> accountNotFoundExceptionHandler(AccountNotFountByIDException accountException) {
        return new ResponseEntity<>(accountException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BalanceException.class)
    public ResponseEntity<String> handleTransferException(BalanceException balanceException) {
        return new ResponseEntity<>(balanceException.getMessage(), HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(AccountNotFountByNumberException.class)
    public ResponseEntity<String> accountNotFoundExceptionHandler(AccountNotFountByNumberException accountException) {
        return new ResponseEntity<>(accountException.getMessage(), HttpStatus.NOT_FOUND);
    }
}
