package ru.jvdev.univalidator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 25.08.2016
 */
public enum FieldTypes {
    STRING, INTEGER, DOUBLE;

    public static List<String> valuesAsStrings() {
        return Arrays.stream(FieldTypes.values())
            .map(FieldTypes::name)
            .collect(Collectors.toList());
    }

    public String toString() {
        return name().toLowerCase();
    }
}
