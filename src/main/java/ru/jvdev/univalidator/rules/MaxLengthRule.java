package ru.jvdev.univalidator.rules;

import java.text.MessageFormat;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 24.08.2016
 */
public class MaxLengthRule implements ValidationRule {

    private int maxLength;

    public MaxLengthRule(Integer maxLength) {
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be > 0");
        }
        this.maxLength = maxLength;
    }

    @Override
    public boolean check(Object value) {
         return value.toString().length() <= maxLength;
    }

    @Override
    public String getErrorMessage() {
        return MessageFormat.format("Must have at most {0} characters", maxLength);
    }
}
