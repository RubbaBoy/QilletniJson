package is.yarr.qilletni.lib.json.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import is.yarr.qilletni.api.lang.types.BooleanType;
import is.yarr.qilletni.api.lang.types.StringType;
import is.yarr.qilletni.api.lang.types.conversion.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BooleanTypeAdapterFactory implements TypeAdapterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooleanTypeAdapterFactory.class);
    
    private final TypeConverter typeConverter;
    
    public BooleanTypeAdapterFactory(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() == Boolean.class || BooleanType.class.isAssignableFrom(typeToken.getRawType())) {
            return new TypeAdapter<>() {
                // Write both I guess
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value instanceof BooleanType booleanType) {
                        out.value(booleanType.getValue());
                    } else {
                        out.value(((Boolean) value).booleanValue());
                    }
                }

                // Read only to StringType
                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }

                    return (T) typeConverter.convertToQilletniType(in.nextBoolean());
                }
            };
        }
        
        // For other types, return null so Gson will use default behavior
        return null;
    }
    
}
