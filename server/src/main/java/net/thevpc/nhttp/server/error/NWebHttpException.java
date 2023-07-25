package net.thevpc.nhttp.server.error;

import net.thevpc.nhttp.commons.HttpCode;

public class NWebHttpException extends RuntimeException implements NWebErrorCodeAware {
    private HttpCode httpCode;
    private NWebErrorCode appErrorCode;

    public NWebHttpException(String message, NWebErrorCode appErrorCode, HttpCode httpCode) {
        super(message);
        this.httpCode = httpCode;
        this.appErrorCode = appErrorCode;
    }

    public HttpCode getHttpCode() {
        return httpCode;
    }

    public NWebErrorCode getAppErrorCode() {
        return appErrorCode;
    }
}
