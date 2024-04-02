package net.thevpc.nhttp.server.security;

import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nhttp.server.error.NWebUnauthorizedSecurityException;
import net.thevpc.nhttp.server.api.NWebToken;
import net.thevpc.nhttp.server.api.NWebUser;
import net.thevpc.nuts.util.NUnsafeRunnable;

public class NWebSecurityContext {

    public static ThreadLocal<NWebSecurityContext> current = new ThreadLocal<>();
    private NWebUser user;
    private NWebToken token;

    public NWebSecurityContext(NWebUser user, NWebToken token) {
        this.user = user;
        this.token = token;
    }

    public NWebUser getUser() {
        return user;
    }

    public NWebSecurityContext setUser(NWebUser user) {
        this.user = user;
        return this;
    }

    public NWebToken getToken() {
        return token;
    }

    public NWebSecurityContext setToken(NWebToken token) {
        this.token = token;
        return this;
    }


    public static NWebSecurityContext currentRequired() {
        NWebSecurityContext t = current.get();
        if (t == null) {
            throw new NWebUnauthorizedSecurityException(new NMsgCode("Security.Unauthorized"), "missing security context");
        }
        return t;
    }

    public static void runWith(NWebSecurityContext a, NUnsafeRunnable callable) throws Throwable {
        NWebSecurityContext t = current.get();
        current.set(a);
        try {
            callable.run();
        } finally {
            current.set(t);
        }
    }
}
