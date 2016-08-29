package ru.jvdev.univalidator;

import java.text.MessageFormat;

import ru.jvdev.univalidator.exception.UniValidatorException;
import ru.jvdev.univalidator.rules.ValidationRule;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 26.08.2016
 */
public class RuleCreator {
    private static final String COULD_NOT_CREATE_RULE = "Could not create an instance of rule class {0}";

    public ValidationRule create(Class ruleClass) {
        try {
            return (ValidationRule) ruleClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new UniValidatorException(MessageFormat.format(COULD_NOT_CREATE_RULE, ruleClass.getName()), e);
        }
    }

    public ValidationRule create(Class ruleClass, Integer value) {
        return create(ruleClass, (Object) value);
    }

    public ValidationRule create(Class ruleClass, String value) {
        return create(ruleClass, (Object) value);
    }

    private ValidationRule create(Class ruleClass, Object value) {
        try {
            return (ValidationRule) ruleClass.getConstructor(value.getClass()).newInstance(value);
        } catch (ReflectiveOperationException e) {
            throw new UniValidatorException(MessageFormat.format(COULD_NOT_CREATE_RULE, ruleClass.getName()), e);
        }
    }
}
