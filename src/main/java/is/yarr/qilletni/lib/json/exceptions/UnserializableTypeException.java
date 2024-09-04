package is.yarr.qilletni.lib.json.exceptions;

import is.yarr.qilletni.api.exceptions.QilletniException;

public class UnserializableTypeException extends QilletniException {

    public UnserializableTypeException() {
        super();
    }

    public UnserializableTypeException(String message) {
        super(message);
    }

    public UnserializableTypeException(Throwable cause) {
        super(cause);
    }
}
