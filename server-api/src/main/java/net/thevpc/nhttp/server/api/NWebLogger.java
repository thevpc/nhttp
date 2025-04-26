package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public interface NWebLogger {
    void log(NHttpLogMsg msg);

    default void info(NMsg msg) {
        log(new NHttpLogMsg().setMessage(msg).setLevel(Level.INFO));
    }

    default void err(NMsg msg) {
        log(new NHttpLogMsg().setMessage(msg).setLevel(Level.SEVERE));
    }
}
