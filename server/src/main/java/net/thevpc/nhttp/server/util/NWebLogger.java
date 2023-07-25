package net.thevpc.nhttp.server.util;

import net.thevpc.nuts.NMsg;

public interface NWebLogger {
    void out(NMsg msg);

    void err(NMsg msg);
}
