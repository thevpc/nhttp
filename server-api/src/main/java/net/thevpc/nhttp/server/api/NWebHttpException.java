package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.web.NHttpCode;

public class NWebHttpException extends RuntimeException implements NWebErrorCodeAware {
    private NHttpCode httpCode;
    private NWebErrorCode appErrorCode;

    public NWebHttpException(String message, NWebErrorCode appErrorCode, NHttpCode httpCode) {
        super(message);
        this.httpCode = httpCode;
        this.appErrorCode = appErrorCode;
    }

    public NHttpCode getHttpCode() {
        return httpCode;
    }

    public NWebErrorCode getAppErrorCode() {
        return appErrorCode;
    }
}
