package net.thevpc.nhttp.server.error;

public class NWebForbiddenSecurityException extends SecurityException implements NWebErrorCodeAware {
    private NWebErrorCode code;
    public NWebForbiddenSecurityException(NWebErrorCode code, String s) {
        super(s);
        this.code=code;
    }

    public NWebErrorCode getAppErrorCode() {
        return code;
    }
}
