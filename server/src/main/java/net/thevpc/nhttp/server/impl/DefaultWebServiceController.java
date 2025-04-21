package net.thevpc.nhttp.server.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.error.NWebUnauthorizedSecurityException;
import net.thevpc.nhttp.server.model.DefaultNWebContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NMsgCodeException;
import net.thevpc.nuts.web.NHttpCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

public class DefaultWebServiceController implements HttpHandler {
    protected NWebContext webContainer;

    public DefaultWebServiceController(DefaultNWebContext webContainer) {
        this.webContainer = webContainer;
    }

    public void handle(HttpExchange t) {
        try (NWebCallContextImpl rc = new NWebCallContextImpl(webContainer, t)) {
            rc.trace(Level.INFO, NMsg.ofPlain("incoming call"));
            NSession.of().runWith(() -> {
                try {
                    rc.runWithUnsafe(() -> handle(rc));
                } catch (Throwable ex) {
                    if (isSimpleThrowable(ex)) {
                        rc.trace(Level.SEVERE, NMsg.ofC("Failed call (%s)", ex));
                    } else {
                        StringBuilder sb = new StringBuilder();
                        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            try (PrintStream pos = new PrintStream(bos)) {
                                ex.printStackTrace(pos);
                                pos.flush();
                            }
                            sb.append(bos.toString());
                        } catch (IOException ex2) {
                            //
                        }
                        rc.trace(Level.SEVERE, NMsg.ofC("Failed call (%s) : %s", ex, sb.toString()));
                    }
                    rc.setErrorResponse(ex).sendResponse();
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
