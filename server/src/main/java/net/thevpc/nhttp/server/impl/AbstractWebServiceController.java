package net.thevpc.nhttp.server.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.api.NWebHttpException;
import net.thevpc.nhttp.server.api.NWebUserResolver;
import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nhttp.server.error.NWebUnauthorizedSecurityException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsgCodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

public abstract class AbstractWebServiceController implements HttpHandler {
    public static final int OK = 200;
    protected NWebUserResolver userResolver;
    protected NWebLogger logger;
    protected String path;
    protected HttpServer server;

    public AbstractWebServiceController(String path, NWebUserResolver userResolver, NWebLogger logger) {
        this.userResolver = userResolver;
        this.logger = logger;
        this.path = path;
    }

    public void handle(HttpExchange t) {
        try(NWebServerHttpContextImpl rc = new NWebServerHttpContextImpl(server, t, userResolver, getLogger())) {
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


    public String getPath() {
        return path;
    }

    public NWebLogger getLogger() {
        return logger;
    }

    public void bind(HttpServer server) {
        this.server = server;
        server.createContext(path, this);
    }

    public abstract void handle(NWebServerHttpContextImpl rc);

}
