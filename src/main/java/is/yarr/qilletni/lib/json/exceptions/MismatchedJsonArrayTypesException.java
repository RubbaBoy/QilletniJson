package is.yarr.qilletni.lib.json.exceptions;

import is.yarr.qilletni.api.exceptions.QilletniException;

public class MismatchedJsonArrayTypesException extends QilletniException {

    public MismatchedJsonArrayTypesException() {
        super();
    }

    public MismatchedJsonArrayTypesException(String message) {
        super(message);
    }

    public MismatchedJsonArrayTypesException(Throwable cause) {
        super(cause);
    }
}
