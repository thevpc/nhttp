package net.thevpc.nhttp.server.servlet;

import javax.servlet.http.HttpServlet;
import java.util.Map;

public class NWebServletConfig {
    private String name;
    private String url;
    private HttpServlet servlet;
    private Map<String,String> initParameters;

    public NWebServletConfig(String name, String url,HttpServlet servlet, Map<String, String> initParameters) {
        this.name = name;
        this.url = url;
        this.servlet = servlet;
        this.initParameters = initParameters;
    }

    public String getName() {
        return name;
    }

    public HttpServlet getServlet() {
        return servlet;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }

    public String getUrl() {
        return url;
    }
}
