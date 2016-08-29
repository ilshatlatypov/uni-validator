package ru.jvdev.univalidator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ru.jvdev.univalidator.exception.UniValidatorException;
import ru.jvdev.univalidator.rules.MaxLengthRule;
import ru.jvdev.univalidator.rules.MinLengthRule;
import ru.jvdev.univalidator.rules.NotBlankRule;
import ru.jvdev.univalidator.rules.RequiredRule;
import ru.jvdev.univalidator.rules.ValidationRule;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 23.08.2016
 */
public class UniValidator {

    private static final Map<String, Class> RULE_CLASS_BY_CONSTRAINTS = new HashMap<>();

    static {
        RULE_CLASS_BY_CONSTRAINTS.put("required", RequiredRule.class);
        RULE_CLASS_BY_CONSTRAINTS.put("notBlank", NotBlankRule.class);
        RULE_CLASS_BY_CONSTRAINTS.put("minLength", MinLengthRule.class);
        RULE_CLASS_BY_CONSTRAINTS.put("maxLength", MaxLengthRule.class);
    }

    private static final Map<String, String> CONSTRAINT_TYPES = new HashMap<>();
    static {
        CONSTRAINT_TYPES.put("required", "boolean");
        CONSTRAINT_TYPES.put("notBlank", "boolean");
        CONSTRAINT_TYPES.put("minLength", "integer");
        CONSTRAINT_TYPES.put("maxLength", "integer");
    }

    private ConstraintsParser constraintsParser = new ConstraintsParser();
    private RuleCreator ruleCreator = new RuleCreator();

    public List<ValidationError> validate(Object obj, String constraintsFileName) {
        List<FieldWithConstraints> fieldsWithConstraints = constraintsParser.parse(constraintsFileName);
        Map<String, List<ValidationRule>> rulesByFields = buildRulesByFieldsMap(fieldsWithConstraints);

        List<ValidationError> validationErrors = new ArrayList<>();

        for (String field : rulesByFields.keySet()) {
            Object value = getFieldValue(obj, field);
            List<ValidationRule> rules = rulesByFields.get(field);
            for (ValidationRule rule : rules) {
                if (!rule.check(value)) {
                    ValidationError error = new ValidationError();
                    error.setField(field);
                    error.setActualValue(value);
                    error.setErrorMessage(rule.getErrorMessage());
                    validationErrors.add(error);
                }
            }
        }
        return validationErrors;
    }

    private Map<String, List<ValidationRule>> buildRulesByFieldsMap(List<FieldWithConstraints> fieldsWithConstraints) {

        Map<String, List<ValidationRule>> rulesByFields = new HashMap<>();

        for (FieldWithConstraints f : fieldsWithConstraints) {

            List<ValidationRule> rules = new ArrayList<>();
            if (!f.hasConstraint("required")) {
                rules.add(ruleCreator.create(RequiredRule.class));
            }

            Map<String, Object> constraints = f.getConstraints();
            for (String constraintName : constraints.keySet()) {
                Class ruleClass = RULE_CLASS_BY_CONSTRAINTS.get(constraintName);
                if (ruleClass == null) {
                    throw new UniValidatorException("Could not find rule class for constraint '" + constraintName + "'");
                }

                Object constraintValue = f.getConstraints().get(constraintName);
                String constraintValueType = CONSTRAINT_TYPES.get(constraintName);

                if (constraintValueType.equals("boolean")) {
                    if (Boolean.TRUE.equals(constraintValue)) {
                        rules.add(ruleCreator.create(ruleClass));
                    }
                } else if (constraintValueType.equals("integer")) {
                    rules.add(ruleCreator.create(ruleClass, ((Double) constraintValue).intValue()));
                } else if (constraintValueType.equals("string")) {
                    rules.add(ruleCreator.create(ruleClass, (String) constraintValue));
                }
            }
            rulesByFields.put(f.getFieldname(), rules);
        }
        return rulesByFields;
    }

    private <T> Object getFieldValue(T obj, String field) {
        String getterName = "get" + StringUtils.capitalize(field);

        Method getter;
        try {
            getter = obj.getClass().getMethod(getterName);
        } catch (NoSuchMethodException e) {
            String message = MessageFormat.format("Could not find getter method {0} for field {1}", getterName, field);
            throw new UniValidatorException(message, e);
        }

        try {
            return getter.invoke(obj);
        } catch (IllegalAccessException e) {
            throw new UniValidatorException("Could not access getter method " + getterName, e);
        } catch (InvocationTargetException e) {
            throw new UniValidatorException("An exception was thrown by getter method " + getterName, e);
        }
    }
}
