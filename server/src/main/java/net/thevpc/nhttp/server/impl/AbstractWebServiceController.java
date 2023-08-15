package net.thevpc.nhttp.server.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.api.NWebUserResolver;
import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

import java.util.logging.Level;

public abstract class AbstractWebServiceController implements HttpHandler {
    public static final int OK = 200;
    protected NWebUserResolver userResolver;
    protected NSession session;
    protected NWebLogger logger;
    protected String path;
    protected HttpServer server;

    public AbstractWebServiceController(String path, NWebUserResolver userResolver, NSession session, NWebLogger logger) {
        this.userResolver = userResolver;
        this.session = session;
        this.logger = logger;
        this.path = path;
    }

    public void handle(HttpExchange t) {
        NWebServerHttpContextImpl rc = new NWebServerHttpContextImpl(server,t, userResolver, session,logger);
        rc.trace(Level.INFO, NMsg.ofPlain("incoming call"));
        try {

            rc.runWithUnsafe(() -> handle(rc));
        } catch (Throwable ex) {
            rc.trace(Level.SEVERE, NMsg.ofC("failed call %s", ex));
            rc.sendError(ex);
        }
    }


    public String getPath() {
        return path;
    }

    public void bind(HttpServer server) {
        this.server=server;
        server.createContext(path, this);
    }

    public abstract void handle(NWebServerHttpContextImpl rc);

}
