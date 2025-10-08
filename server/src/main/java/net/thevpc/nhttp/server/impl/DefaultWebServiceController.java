package net.thevpc.nhttp.server.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.error.NWebUnauthorizedSecurityException;
import net.thevpc.nhttp.server.model.DefaultNWebContext;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.text.NMsgCodeException;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.net.NHttpCode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class DefaultWebServiceController implements HttpHandler {
    protected NWebContext webContainer;

    public DefaultWebServiceController(DefaultNWebContext webContainer) {
        this.webContainer = webContainer;
    }

    public void handle(HttpExchange t) {
        try (NWebCallContextImpl rc = new NWebCallContextImpl(webContainer, t)) {
            NChronometer ch = NChronometer.startNow();
            NSession.of().runWith(() -> {
                Throwable error = null;
                String prefix = "   ";
                List<NHttpLogMsg> seen = new ArrayList<>();
                rc.setTracer(new Consumer<NHttpLogMsg>() {
                    @Override
                    public void accept(NHttpLogMsg nMsg) {
                        seen.add(nMsg);
                    }
                });
                try {
                    rc.runWithUnsafe(() -> handle(rc));
                } catch (Throwable ex) {
                    error = ex;
                    rc.setErrorResponse(ex).sendResponse();
                } finally {
                    rc.setTracer(null);
                    ch.stop();
                    NDuration duration = ch.getDuration();
                    if (error == null) {
                        rc.trace(new NHttpLogMsg()
                                .setDuration(duration)
                                .setLevel(Level.INFO)
                                .setMessage(NMsg.ofC(" Successful call"))
                        );
                        for (NHttpLogMsg nMsg : seen) {
                            rc.trace(nMsg.appendPrefix(prefix));
                        }
                    } else {
                        rc.trace(new NHttpLogMsg()
                                .setDuration(duration)
                                .setLevel(Level.SEVERE)
                                .setMessage(NMsg.ofC("Failed call", error))
                        );
                        for (NHttpLogMsg nMsg : seen) {
                            rc.trace(nMsg.appendPrefix(prefix));
                        }
                        if (!isSimpleThrowable(error)) {
                            rc.error(NMsg.ofC("%s>> Detected error : %s", prefix, NStringUtils.stacktrace(error)));
                        }
                    }
                }
            });
        }
    }

    public boolean isSimpleThrowable(Throwable ex) {
        if (ex instanceof NWebHttpException) {
            return true;
        }
        if (ex instanceof NMsgCodeException) {
            return true;
        }
        if (ex instanceof NWebUnauthorizedSecurityException) {
            return true;
        }
        return false;
    }

    public void handle(NWebCallContextImpl rc) {
        NWebCallHandler h = webContainer.getHandler();
        if (h != null) {
            h.handle(rc);
        } else {
            rc.setErrorResponse(new NWebHttpException(
                    NMsg.ofC("unsupported path %s", rc.getPath()), NMsgCode.ofCode("NOT_FOUND", rc.getPath()), NHttpCode.NOT_FOUND
            ));
        }
    }

}
