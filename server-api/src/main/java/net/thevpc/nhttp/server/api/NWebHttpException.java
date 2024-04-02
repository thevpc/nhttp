package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeAware;
import net.thevpc.nuts.web.NHttpCode;

public class NWebHttpException extends RuntimeException implements NMsgCodeAware {
    private NHttpCode httpCode;
    private NMsgCode appErrorCode;

    public NWebHttpException(String message, NMsgCode appErrorCode, NHttpCode httpCode) {
        super(message);
        this.httpCode = httpCode;
        this.appErrorCode = appErrorCode;
    }

    public NHttpCode getHttpCode() {
        return httpCode;
    }

    public NMsgCode getNMsgCode() {
        return appErrorCode;
    }
}
