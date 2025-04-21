package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeAware;
import net.thevpc.nuts.web.NHttpCode;

public class NWebHttpException extends NException implements NMsgCodeAware {
    private NHttpCode httpCode;
    private NMsgCode appErrorCode;

    public NWebHttpException(NMsg message, NMsgCode appErrorCode, NHttpCode httpCode) {
        super(message);
        this.httpCode = httpCode;
        this.appErrorCode = NMsgCode.ofMessage(
                message.toString(),
                appErrorCode == null ? null : appErrorCode.getCode(), appErrorCode == null ? new String[0] : appErrorCode.getParams()
        );
    }

    public NHttpCode getHttpCode() {
        return httpCode;
    }

    public NMsgCode getMsgCode() {
        return appErrorCode;
    }
}
