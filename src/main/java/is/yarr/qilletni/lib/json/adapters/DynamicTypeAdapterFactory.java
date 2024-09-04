package is.yarr.qilletni.lib.json.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import is.yarr.qilletni.api.lang.types.BooleanType;
import is.yarr.qilletni.api.lang.types.DoubleType;
import is.yarr.qilletni.api.lang.types.IntType;
import is.yarr.qilletni.api.lang.types.StringType;
import is.yarr.qilletni.api.lang.types.conversion.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DynamicTypeAdapterFactory implements TypeAdapterFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTypeAdapterFactory.class);

    private final TypeConverter typeConverter;

    public DynamicTypeAdapterFactory(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        // Check if the type is a HashMap<String, Object>
        if (!Map.class.isAssignableFrom(typeToken.getRawType())) {
            return null; // Only handle Map types (e.g., HashMap)
        }

        return (TypeAdapter<T>) new DynamicMapTypeAdapter(gson);
    }

    private class DynamicMapTypeAdapter<K, V> extends TypeAdapter<Map<K, V>> {
        private final Gson gson;

        public DynamicMapTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, Map<K, V> map) throws IOException {
            out.beginObject();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                out.name(entry.getKey().toString());
                Object value = entry.getValue();
                switch (value) {
                    case StringType stringType -> gson.getAdapter(StringType.class).write(out, stringType);
                    case BooleanType booleanType -> gson.getAdapter(BooleanType.class).write(out, booleanType);
                    case String s -> out.value(s);  // Use default String serialization
                    case Boolean b -> out.value(b);  // Use default Boolean serialization
                    case null, default -> gson.toJson(value, value.getClass(), out);  // Fallback for any other type
                }
            }
            out.endObject();
        }

        @Override
        public Map<K, V> read(JsonReader in) throws IOException {
            Map<StringType, Object> map = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                var key = (StringType) typeConverter.convertToQilletniType(in.nextName());
                var element = JsonParser.parseReader(in);

                // Dynamically choose the type based on the content
                Object value = null;
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        value = gson.getAdapter(StringType.class).fromJsonTree(element);
                    } else if (primitive.isBoolean()) {
                        value = gson.getAdapter(BooleanType.class).fromJsonTree(element);
                    } else if (primitive.isNumber()) {
                        var number = primitive.getAsNumber();
                        if (number instanceof Double || number instanceof Float) {
                            value = gson.getAdapter(DoubleType.class).fromJsonTree(element);
                        } else {
                            value = gson.getAdapter(IntType.class).fromJsonTree(element);
                        }
                    }
                } else if (element.isJsonObject()) {
                    // Handle nested objects if needed
                    // TODO: Nested maps! Does this work already?
                    value = gson.fromJson(element, HashMap.class);
                } else if (element.isJsonArray()) {
                    // Handle arrays if needed
                    // TODO: List
                    value = gson.fromJson(element, Object[].class);
                }
                
                map.put(key, value);
            }
            in.endObject();
            return (Map<K, V>) map;
        }
    }
}
