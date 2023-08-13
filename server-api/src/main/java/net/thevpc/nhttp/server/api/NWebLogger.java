package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.NMsg;

public interface NWebLogger {
    void out(NMsg msg);

    void err(NMsg msg);
}
