package net.thevpc.nhttp.server.error;

import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.text.NMsgCodeAware;

public class NWebForbiddenSecurityException extends SecurityException implements NMsgCodeAware {
    private NMsgCode code;
    public NWebForbiddenSecurityException(NMsgCode code, String s) {
        super(s);
        this.code=code;
    }

    public NMsgCode msgCode() {
        return code;
    }
}
