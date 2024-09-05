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
        System.out.println("isArray = " + isArray);

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
                    var subType = listType.getSubType();
                    System.out.println("listType.getSubType(); = " + subType);
                    for (QilletniType item : listType.getItems()) {
                        // TODO
                    }
                    out.endArray();
//                    out.value(stringType.getValue());
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
                QilletniTypeClass<?> commonType = null;

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
                        // TODO: Map, list, entities
                        default -> throw new IOException("Unsupported token in array: " + token);
                    };

                    // Add value to the list
                    list.add(value);

                    // Determine common type, promoting int to double if necessary
                    if (value != null) {
                        QilletniTypeClass<?> valueClass = null;
                        var intentionalSwitch = false;

                        if (value instanceof StringType) {
                            valueClass = QilletniTypeClass.STRING;
                        } else if (value instanceof BooleanType) {
                            valueClass = QilletniTypeClass.BOOLEAN;
                        } else if (value instanceof DoubleType) {
                            LOGGER.debug("{} is double", value);
                            if (commonType == QilletniTypeClass.INT) {
                                LOGGER.debug("    promoting to double");
                                valueClass = QilletniTypeClass.DOUBLE;  // Promote to double if needed
                                intentionalSwitch = true;
                            } else {
                                LOGGER.debug("    setting double");
                                valueClass = QilletniTypeClass.DOUBLE;
                            }
                        } else if (value instanceof IntType) {
                            LOGGER.debug("{} is int", value);
                            if (commonType == QilletniTypeClass.DOUBLE) {
                                LOGGER.debug("    keeping double");
                                valueClass = QilletniTypeClass.DOUBLE;
                                // Keep commonType as Double (since integers can fit in doubles)
                            } else {
                                LOGGER.debug("    setting int");
                                valueClass = QilletniTypeClass.INT;
                            }
                        }

                        if (commonType == null) {
                            commonType = valueClass;
                        } else if (commonType != valueClass && !intentionalSwitch) {
                            if (!commonType.getClass().isAssignableFrom(value.getClass())) {
                                throw new MismatchedJsonArrayTypesException("Inconsistent types in array. Expected: " + commonType.getTypeName() + ", but got: " + valueClass.getTypeName());
                            }
                        }

                        if (intentionalSwitch) {
                            commonType = valueClass;
                        }
                    }
                }

                in.endArray();

                LOGGER.debug("Common type for list: {}", commonType);

                if (commonType == null) {
                    return (T) listInitializer.createList(Collections.emptyList());
                }

                return (T) listInitializer.createList(list, commonType); // TODO: Return ListType
            }
        };
    }

}
