package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.util.NMsg;

public interface NWebLogger {
    void out(NMsg msg);

    void err(NMsg msg);
}
