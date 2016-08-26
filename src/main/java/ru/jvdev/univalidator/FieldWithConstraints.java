package ru.jvdev.univalidator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 25.08.2016
 */
public class FieldWithConstraints {
    private String fieldname;
    private String type;
    private Map<String, Object> constraints = new LinkedHashMap<>();

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addConstraint(String name, Object value) {
        constraints.put(name, value);
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public boolean hasConstraint(String name) {
        return constraints.containsKey(name);
    }
}
