package net.thevpc.nhttp.server.error;

import net.thevpc.nhttp.server.api.NWebErrorCode;
import net.thevpc.nhttp.server.api.NWebErrorCodeAware;

public class NWebUnauthorizedSecurityException extends SecurityException implements NWebErrorCodeAware {
    private NWebErrorCode code;

    public NWebUnauthorizedSecurityException(NWebErrorCode code, String s) {
        super(s);
        this.code = code;
    }

    public NWebErrorCode getAppErrorCode() {
        return code;
    }
}
