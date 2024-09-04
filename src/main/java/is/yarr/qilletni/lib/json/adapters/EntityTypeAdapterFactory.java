package is.yarr.qilletni.lib.json.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import is.yarr.qilletni.api.lang.types.EntityType;
import is.yarr.qilletni.api.lang.types.JavaType;
import is.yarr.qilletni.api.lang.types.QilletniType;
import is.yarr.qilletni.api.lang.types.StringType;
import is.yarr.qilletni.api.lang.types.conversion.TypeConverter;
import is.yarr.qilletni.api.lang.types.entity.EntityInitializer;
import is.yarr.qilletni.lib.json.exceptions.UnserializableTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EntityTypeAdapterFactory implements TypeAdapterFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeAdapterFactory.class);
    
    private final TypeConverter typeConverter;
    private final EntityInitializer entityInitializer;

    public EntityTypeAdapterFactory(TypeConverter typeConverter, EntityInitializer entityInitializer) {
        this.typeConverter = typeConverter;
        this.entityInitializer = entityInitializer;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!EntityType.class.isAssignableFrom(type.getRawType())) {
            return null; // Only handle EntityType and its subclasses
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value instanceof EntityType entityType) {
                    if (entityType.getEntityDefinition().getTypeName().equals("Map")) {
                        JavaType javaType = entityType.getEntityScope().<JavaType>lookup("_map").getValue();
                        HashMap<QilletniType, QilletniType> hashMap = javaType.getReference(HashMap.class);

                        // Can't serialize maps that don't have a string as the key
                        if (hashMap.keySet().stream().anyMatch(qilletniType -> !(qilletniType instanceof StringType))) {
                            throw new UnserializableTypeException("All keys in a map must be of type string");
                        }
                        
                        LOGGER.debug("Serializing map: {}", hashMap);

                        var newMap = new HashMap<String, QilletniType>();
                        hashMap.forEach((key, mapVal) -> newMap.put(((StringType) key).getValue(), mapVal));

                        JsonElement serialized = gson.toJsonTree(newMap);
                        LOGGER.debug("Serialized map: {}", serialized);
                        out.jsonValue(serialized.toString());
                        return;
                    }
                    throw new UnserializableTypeException("Cannot serialize type %s".formatted(entityType.typeName()));
                }
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = JsonParser.parseReader(in);

                Map<String, QilletniType> map = gson.fromJson(jsonElement, new TypeToken<Map<String, QilletniType>>() {}.getType());

                var newMap = new HashMap<QilletniType, QilletniType>();
                map.forEach((key, mapVal) -> newMap.put(typeConverter.convertToQilletniType(key), mapVal));
                
                LOGGER.debug("Created Map entity with contents: {}", newMap);

                return (T) entityInitializer.initializeEntity("Map", newMap);
            }
        }.nullSafe(); // Ensure the adapter handles nulls safely
    }
}
