package ru.jvdev.univalidator.rules;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public class NotBlankRule implements ValidationRule {

    @Override
    public boolean check(Object value) {
        return value != null && value.toString().trim().length() > 0;
    }

    @Override
    public String getErrorMessage() {
        return "Can't be blank";
    }
}
