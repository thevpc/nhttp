package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.util.NOptional;

public class NWebServerHttpContextHolder {
    public static final InheritableThreadLocal<NWebCallContext> current = new InheritableThreadLocal<>();
    public static NOptional<NWebCallContext> current() {
        return NOptional.ofNamed(current.get(), "context");
    }
}
