package ru.jvdev.univalidator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import jdk.nashorn.api.scripting.URLReader;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 25.08.2016
 */
public class ConstraintsParser {

    public List<Map<String, Object>> parse(String filename) {
        List<Map<String, Object>> constraints = new ArrayList<>();

        Gson gson = new Gson();
        URL constraintsFileURL = getClass().getClassLoader().getResource(filename);
        JsonReader jsonReader = gson.newJsonReader(new URLReader(constraintsFileURL));

        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                constraints.add(readFieldConstraints(jsonReader));
            }
            jsonReader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return constraints;
    }

    private Map<String, Object> readFieldConstraints(JsonReader jsonReader) {
        Type itemsMapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> fieldConstraintsMap = new Gson().fromJson(jsonReader, itemsMapType);
        return fieldConstraintsMap;
    }
}
