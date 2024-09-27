package br.com.techchallenge.lambda.autorizer.exception;

public class AuthHeaderMalformedException extends RuntimeException {

    public AuthHeaderMalformedException(String message) {
        super(message);
    }
}
