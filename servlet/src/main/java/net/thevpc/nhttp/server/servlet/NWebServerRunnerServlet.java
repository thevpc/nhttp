package net.thevpc.nhttp.server.servlet;

import net.thevpc.nhttp.server.api.*;
import net.thevpc.nuts.util.NStringBuilder;

import javax.servlet.*;
import java.util.*;

public class NWebServerRunnerServlet implements NWebServerRunner {
    private List<NWebServletConfig> servlets;
    private List<ServletContextListener> servletContextListeners;
    private NWebConfig appWebServer;

    public NWebServerRunnerServlet() {
        this.servlets = new ArrayList<>();
        this.servletContextListeners = new ArrayList<>();
    }

    public NWebServerRunnerServlet addListener(ServletContextListener c){
        this.servletContextListeners.add(c);
        return this;
    }

    public NWebServerRunnerServlet addServlet(NWebServletConfig c){
        this.servlets.add(c);
        return this;
    }

    public List<ServletContextListener> getServletContextListeners() {
        return servletContextListeners;
    }

    @Override
    public void bootstrap(NWebConfig appWebServer) {
        this.appWebServer = appWebServer;
    }

    @Override
    public NWebUserResolver userResolver() {
        return null;
    }

    @Override
    public void createContext(NWebContainer container) {
        NHttpServer ws = appWebServer.getServer();
        ServletContextImpl rootContext = new ServletContextImpl(ws,
                "/",
                container.getDisplayName(),
                container.getInitParameters()
        );
        for (ServletContextListener servletContextListener : servletContextListeners) {
            servletContextListener.contextInitialized(new ServletContextEvent(rootContext));
        }
        for (NWebServletConfig servlet : servlets) {
            String contextPath = container.getContextPath();
            ServletConfigImpl config = new ServletConfigImpl(servlet, ws, rootContext);

            String url = servlet.getUrl();
            if(url==null){
                url="";
            }
            NStringBuilder sb=new NStringBuilder();
            sb.append(contextPath.trim());
            if(sb.endsWith("/")){
                while (url.startsWith("/")){
                    url=url.substring(1);
                }
                sb.append(url);
            }else{
                while (url.startsWith("/")){
                    url=url.substring(1);
                }
                sb.append("/");
                sb.append(url);
            }
            ws.getServer().createContext(sb.toString(), new ServletHttpHandler(
                    servlet,
                    container,
                    ws,
                    config
            ));
            try {
                servlet.getServlet().init(config);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void initializeConfig() {

    }

}
