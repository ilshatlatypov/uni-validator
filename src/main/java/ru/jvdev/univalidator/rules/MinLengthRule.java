package ru.jvdev.univalidator.rules;

import java.text.MessageFormat;

import com.sun.istack.internal.NotNull;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public class MinLengthRule implements ValidationRule {

    private int minLength;

    public MinLengthRule(Integer minLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        this.minLength = minLength;
    }

    @Override
    public boolean check(@NotNull Object value) {
        return value.toString().length() >= minLength;
    }

    @Override
    public String getErrorMessage() {
        return MessageFormat.format("Must have at least {0} characters", minLength);
    }
}
