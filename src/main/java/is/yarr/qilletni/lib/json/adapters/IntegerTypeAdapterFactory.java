package is.yarr.qilletni.lib.json.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import is.yarr.qilletni.api.lang.types.IntType;
import is.yarr.qilletni.api.lang.types.conversion.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IntegerTypeAdapterFactory implements TypeAdapterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerTypeAdapterFactory.class);
    
    private final TypeConverter typeConverter;
    
    public IntegerTypeAdapterFactory(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() != Integer.class && !IntType.class.isAssignableFrom(typeToken.getRawType())) {
            LOGGER.debug("Type {} is not assignable from Integer or IntType, returning null", typeToken.getRawType());
            return null; // Return null so Gson will use default behavior
        }
        
        LOGGER.debug("Creating type adapter for {}", typeToken.getRawType());
        
        return new TypeAdapter<>() {
            // Write both I guess
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value instanceof IntType integerType) {
                    out.value(integerType.getValue());
                } else {
                    out.value(((Integer) value).intValue());
                }
            }

            // Read only to StringType
            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                return (T) typeConverter.convertToQilletniType(in.nextInt());
            }
        };
    }
    
}
