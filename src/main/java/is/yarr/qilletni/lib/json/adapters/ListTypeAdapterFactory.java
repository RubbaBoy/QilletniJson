package is.yarr.qilletni.lib.json.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import is.yarr.qilletni.api.lang.types.BooleanType;
import is.yarr.qilletni.api.lang.types.DoubleType;
import is.yarr.qilletni.api.lang.types.IntType;
import is.yarr.qilletni.api.lang.types.ListType;
import is.yarr.qilletni.api.lang.types.QilletniType;
import is.yarr.qilletni.api.lang.types.StringType;
import is.yarr.qilletni.api.lang.types.conversion.TypeConverter;
import is.yarr.qilletni.api.lang.types.list.ListInitializer;
import is.yarr.qilletni.api.lang.types.typeclass.QilletniTypeClass;
import is.yarr.qilletni.lib.json.exceptions.MismatchedJsonArrayTypesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ListTypeAdapterFactory implements TypeAdapterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListTypeAdapterFactory.class);

    private final TypeConverter typeConverter;
    private final ListInitializer listInitializer;

    public ListTypeAdapterFactory(TypeConverter typeConverter, ListInitializer listInitializer) {
        this.typeConverter = typeConverter;
        this.listInitializer = listInitializer;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        var isArray = typeToken.getRawType().isArray();

        if (!isArray && !ListType.class.isAssignableFrom(typeToken.getRawType())) {
            LOGGER.debug("Type {} and {} is not a ListType, returning null", typeToken.getRawType(), typeToken.getType());
            return null; // Return null so Gson will use default behavior
        }

        LOGGER.debug("Creating ListTypeAdapter for type {} and {} ({})", typeToken.getRawType(), typeToken.getType(), isArray);

        return new TypeAdapter<>() {
            // Write both I guess
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value instanceof ListType listType) {
                    out.beginArray();
                    for (QilletniType item : listType.getItems()) {
                        switch (item) {
                            case StringType stringType -> out.value(stringType.getValue());
                            case IntType intType -> out.value(intType.getValue());
                            case DoubleType doubleType -> out.value(doubleType.getValue());
                            case BooleanType booleanType -> out.value(booleanType.getValue());
                            case ListType listType1 -> gson.getAdapter(ListType.class).write(out, listType1);
                            default -> throw new IOException("Unsupported type in list: " + item.getClass());
                        }
                    }
                    out.endArray();
                } else {
                    out.value((String) value);
                }
            }

            // Read only to StringType
            @Override
            public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                in.beginArray();

                var list = new ArrayList<QilletniType>();

                while (in.hasNext()) {
                    var token = in.peek();  // Peek at the next token to determine its type
                    var element = JsonParser.parseReader(in);

                    var value = switch (token) {
                        case NUMBER -> {
                            double num = element.getAsDouble();
                            if (Math.floor(num) == num) {
                                yield gson.getAdapter(IntType.class).fromJsonTree(element);  // It's an integer
                            } else {
                                yield gson.getAdapter(DoubleType.class).fromJsonTree(element);  // It's a double
                            }
                        }
                        case STRING -> gson.getAdapter(StringType.class).fromJsonTree(element);
                        case BOOLEAN -> gson.getAdapter(BooleanType.class).fromJsonTree(element);
                        case BEGIN_ARRAY -> gson.getAdapter(ListType.class).fromJsonTree(element);
                        // TODO: Map, entities(?)
                        default -> throw new IOException("Unsupported token in array: " + token);
                    };

                    list.add(value);
                }

                in.endArray();

                return (T) listInitializer.createList(list, QilletniTypeClass.ANY);
            }
        };
    }

}
