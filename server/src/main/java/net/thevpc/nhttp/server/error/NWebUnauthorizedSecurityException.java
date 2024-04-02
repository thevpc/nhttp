package net.thevpc.nhttp.server.error;

import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeAware;

public class NWebUnauthorizedSecurityException extends SecurityException implements NMsgCodeAware {
    private NMsgCode code;

    public NWebUnauthorizedSecurityException(NMsgCode code, String s) {
        super(s);
        this.code = code;
    }

    public NMsgCode getNMsgCode() {
        return code;
    }
}
