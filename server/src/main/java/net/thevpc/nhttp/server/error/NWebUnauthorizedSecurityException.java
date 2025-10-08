package net.thevpc.nhttp.server.error;

import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.text.NMsgCodeAware;

public class NWebUnauthorizedSecurityException extends SecurityException implements NMsgCodeAware {
    private NMsgCode code;

    public NWebUnauthorizedSecurityException(NMsgCode code, String s) {
        super(s);
        this.code = code;
    }

    public NMsgCode getMsgCode() {
        return code;
    }
}
