package ru.jvdev.univalidator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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

    private static final Map<String, Set<String>> CONSTRAINTS_BY_FIELD_TYPES = new HashMap<>();
    static {
        Set<String> forStrings = new HashSet<>();
        forStrings.add("required");
        forStrings.add("notBlank");
        forStrings.add("minLength");
        forStrings.add("maxLength");
        CONSTRAINTS_BY_FIELD_TYPES.put("string", forStrings);

        Set<String> forIntegers = new HashSet<>();
        forIntegers.add("required");
        forIntegers.add("min");
        forIntegers.add("max");
        CONSTRAINTS_BY_FIELD_TYPES.put("integer", forIntegers);
    }

    private ConstraintsParser constraintsParser = new ConstraintsParser();
    private RuleCreator ruleCreator = new RuleCreator();

    public List<ValidationError> validate(Object obj, String constraintsFileName) {
        List<Map<String, Object>> rawConstraints = constraintsParser.parse(constraintsFileName);
        List<FieldWithConstraints> fieldsWithConstraints = buildFieldsWithConstraints(rawConstraints);
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

    private List<FieldWithConstraints> buildFieldsWithConstraints(List<Map<String, Object>> constraintsFileContent) {
        List<FieldWithConstraints> fieldsWithConstraints = new ArrayList<>();

        // convert raw constraints to fieldsWithConstraints
        for (Map<String, Object> constraintsFileEntry : constraintsFileContent) {
            FieldWithConstraints field = new FieldWithConstraints();

            for (String key : constraintsFileEntry.keySet()) {
                Object value = constraintsFileEntry.get(key);
                if (field.getFieldname() == null) {
                    if ("fieldname".equals(key)) {
                        field.setFieldname(value.toString());
                        continue;
                    } else {
                        throw new UniValidatorException("'fieldname' must be listed in the first place");
                    }
                }
                if (field.getType() == null) {
                    if ("type".equals(key)) {
                        field.setType(value.toString());
                        // TODO type value must be one of string, integer, double
                        continue;
                    } else {
                        throw new UniValidatorException("'type' must be listed in the second place");
                    }
                }
                field.addConstraint(key, value);
            }
            fieldsWithConstraints.add(field);
        }

        // validate fieldsWithConstraints
        final List<String> fieldTypes = FieldTypes.valuesAsStrings();
        for (FieldWithConstraints f : fieldsWithConstraints) {
            String fieldname = f.getFieldname();
            if (StringUtils.isBlank(fieldname)) {
                throw new UniValidatorException("Field name value can't be blank");
            }
            if (!fieldname.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$")) {
                throw new UniValidatorException("Field name must be valid Java variable name");
            }

            String type = f.getType();
            if (StringUtils.isBlank(type)) {
                throw new UniValidatorException("Type can't be blank");
            }
            if (fieldTypes.contains(type)) {
                throw new UniValidatorException("Type must be one of " + StringUtils.join(fieldTypes, ", "));
            }

            Set<String> allowedConstraints = CONSTRAINTS_BY_FIELD_TYPES.get(type);
            for (String constraintName : f.getConstraints().keySet()) {
                if (!allowedConstraints.contains(constraintName)) {
                    throw new UniValidatorException(
                        "Constraint '" + constraintName + "' is not allowed for field '" + fieldname + "' with type '" + type + "'");
                }
                // TODO check constraint values
            }
        }
        return fieldsWithConstraints;
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
