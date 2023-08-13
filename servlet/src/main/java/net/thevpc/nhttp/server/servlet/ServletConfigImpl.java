package net.thevpc.nhttp.server.servlet;

import net.thevpc.nhttp.server.api.NHttpServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class ServletConfigImpl implements ServletConfig {
    private NWebServletConfig servlet;
    private NHttpServer server;
    private ServletContext servletContext;

    public ServletConfigImpl(NWebServletConfig servlet, NHttpServer server, ServletContext servletContext) {
        this.servlet = servlet;
        this.server = server;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servlet.getName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return servletContext.getInitParameter(s);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return servletContext.getInitParameterNames();
    }
}
