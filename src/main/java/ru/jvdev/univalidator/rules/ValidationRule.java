package ru.jvdev.univalidator.rules;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public interface ValidationRule {
    boolean check(Object value);
    String getErrorMessage();
}
