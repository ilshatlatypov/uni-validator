package ru.jvdev.univalidator.rules;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public class RequiredRule implements ValidationRule {

    public boolean check(Object value) {
        return value != null;
    }

    @Override
    public String getErrorMessage() {
        return "Field is required";
    }
}
