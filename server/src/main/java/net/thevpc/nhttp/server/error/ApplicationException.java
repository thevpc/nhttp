package net.thevpc.nhttp.server.error;

import net.thevpc.nhttp.server.api.NWebErrorCode;
import net.thevpc.nhttp.server.api.NWebErrorCodeAware;

public class ApplicationException extends RuntimeException implements NWebErrorCodeAware {
    private NWebErrorCode code;

    public ApplicationException(NWebErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApplicationException(NWebErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    @Override
    public NWebErrorCode getAppErrorCode() {
        return code;
    }
}
