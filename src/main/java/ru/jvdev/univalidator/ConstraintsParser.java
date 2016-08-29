package ru.jvdev.univalidator;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import jdk.nashorn.api.scripting.URLReader;
import ru.jvdev.univalidator.exception.ConstraintsParserException;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 25.08.2016
 */
public class ConstraintsParser {

    private static final Map<String, Set<String>> CONSTRAINTS_BY_FIELD_TYPES = new HashMap<>();
    private static final String CANT_BE_BLANK = "'%s' can't be blank";
    private static final String MUST_HAVE_STRING_VALUE = "'%s' must have string value";

    private static final String FIELDNAME = "fieldname";
    private static final String TYPE = "type";


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

    public List<FieldWithConstraints> parse(String filename) {
        URL fileURL = getClass().getClassLoader().getResource(filename);
        return parse(new URLReader(fileURL));
    }

    public List<FieldWithConstraints> parse(Reader reader) {
        List<Map<String, Object>> rawConstraints = parseRaw(reader);
        return buildFieldsWithConstraints(rawConstraints);
    }

    public List<Map<String, Object>> parseRaw(Reader reader) {
        List<Map<String, Object>> constraints = new ArrayList<>();

        Gson gson = new Gson();
        JsonReader jsonReader = gson.newJsonReader(reader);

        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                constraints.add(readFieldConstraints(jsonReader));
            }
            jsonReader.endArray();
        } catch (IOException e) {
            throw new ConstraintsParserException("Could not parse constraints file", e);
        }
        return constraints;
    }

    private List<FieldWithConstraints> buildFieldsWithConstraints(List<Map<String, Object>> rawConstraints) {
        List<FieldWithConstraints> fieldsWithConstraints = new ArrayList<>();

        // convert raw constraints to fieldsWithConstraints
        for (Map<String, Object> constraintsFileEntry : rawConstraints) {
            FieldWithConstraints field = new FieldWithConstraints();

            for (String key : constraintsFileEntry.keySet()) {
                Object value = constraintsFileEntry.get(key);
                if (field.getFieldname() == null) {
                    if (FIELDNAME.equals(key)) {
                        if (value instanceof String) {
                            field.setFieldname(value.toString());
                        } else {
                            throw new ConstraintsParserException(String.format(MUST_HAVE_STRING_VALUE, FIELDNAME));
                        }
                        continue;
                    } else {
                        throw new ConstraintsParserException("'fieldname' must be listed in the first place");
                    }
                }
                if (field.getType() == null) {
                    if (TYPE.equals(key)) {
                        if (value instanceof String) {
                            field.setType(value.toString());
                        } else {
                            throw new ConstraintsParserException(String.format(MUST_HAVE_STRING_VALUE, TYPE));
                        }
                        // TODO type value must be one of string, integer, double
                        continue;
                    } else {
                        throw new ConstraintsParserException("'type' must be listed in the second place");
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
                throw new ConstraintsParserException(String.format(CANT_BE_BLANK, FIELDNAME));
            }
            if (!fieldname.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$")) {
                throw new ConstraintsParserException("'fieldname' must be valid Java variable name");
            }

            String type = f.getType();
            if (StringUtils.isBlank(type)) {
                throw new ConstraintsParserException(String.format(CANT_BE_BLANK, TYPE));
            }
            if (fieldTypes.contains(type)) {
                throw new ConstraintsParserException("'type' must be one of " + StringUtils.join(fieldTypes, ", "));
            }

            Set<String> allowedConstraints = CONSTRAINTS_BY_FIELD_TYPES.get(type);
            for (String constraintName : f.getConstraints().keySet()) {
                if (!allowedConstraints.contains(constraintName)) {
                    throw new ConstraintsParserException(
                        "Constraint '" + constraintName + "' is not allowed for field '" + fieldname + "' with type '" + type + "'");
                }
                // TODO check constraint values
            }
        }
        return fieldsWithConstraints;
    }

    private Map<String, Object> readFieldConstraints(JsonReader jsonReader) {
        Type itemsMapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> fieldConstraintsMap = new Gson().fromJson(jsonReader, itemsMapType);
        return fieldConstraintsMap;
    }
}
