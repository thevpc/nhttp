package net.thevpc.nhttp.server.servlet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.thevpc.nhttp.server.api.NWebContainer;
import net.thevpc.nhttp.server.api.NHttpServer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

public class ServletHttpHandler implements HttpHandler {
    private NWebServletConfig servletConfig;
    private NHttpServer server;
    private NWebContainer container;
    private ServletConfigImpl myServletConfig;
    private ServletContext servletContext;

    public ServletHttpHandler(
            NWebServletConfig servletConfig,
            NWebContainer container,
            NHttpServer server,
            ServletConfigImpl myServletConfig
    ) {
        this.servletConfig = servletConfig;
        this.container = container;
        this.server = server;
        this.myServletConfig = myServletConfig;
        this.servletContext = myServletConfig.getServletContext();
    }

    public void init() {
        try {
            servletConfig.getServlet().init(myServletConfig);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpServletRequestImpl req = null;
        HttpServletResponseImpl resp = null;
        try {
            req = new HttpServletRequestImpl(this, exchange);
            resp = new HttpServletResponseImpl(this, exchange);
            //required in async
            req.resp = resp;
            servletConfig.getServlet().service(req, resp);
        } catch (Exception e) {
            try {
                if (req != null && resp != null) {
                    if (!resp.isCommitted()) {
                        resp.setStatus(500, "Server Error");
                        resp.addHeader("X-SERVER-ERROR", req.toString());
                        resp.setContentLengthLong(0);
                        resp.flushBuffer();
                        resp.close();
                    }
                    return;
                }
            } catch (Exception ex) {
                //
            }
            throw new IOException(e);
        }finally {
            if(!req.isAsyncStarted()){
                if (!resp.isCommitted()) {
                    resp.flushBuffer();
                }
                resp.close();
            }
        }
    }

    public NWebServletConfig getServletConfig() {
        return servletConfig;
    }

    public NHttpServer getServer() {
        return server;
    }

    public NWebContainer getContainer() {
        return container;
    }

    public ServletConfigImpl getMyServletConfig() {
        return myServletConfig;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
