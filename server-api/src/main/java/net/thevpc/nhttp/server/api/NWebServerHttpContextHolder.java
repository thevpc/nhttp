package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.NOptional;

public class NWebServerHttpContextHolder {
    public static final InheritableThreadLocal<NWebServerHttpContext> current = new InheritableThreadLocal<>();
    public static NOptional<NWebServerHttpContext> current() {
        return NOptional.ofNamed(current.get(), "context");
    }
}
