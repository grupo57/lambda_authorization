package br.com.techchallenge.lambda.autorizer.exception;

public class AuthHeaderMissingException extends RuntimeException {

    public AuthHeaderMissingException(String message) {
        super(message);
    }

}
