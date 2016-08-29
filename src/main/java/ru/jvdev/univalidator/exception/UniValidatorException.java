package ru.jvdev.univalidator.exception;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public class UniValidatorException extends RuntimeException {

    public UniValidatorException(String message) {
        super(message);
    }

    public UniValidatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
