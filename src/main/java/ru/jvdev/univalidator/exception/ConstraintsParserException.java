package ru.jvdev.univalidator.exception;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 26.08.2016
 */
public class ConstraintsParserException extends RuntimeException {

    public ConstraintsParserException(String message) {
        super(message);
    }

    public ConstraintsParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
