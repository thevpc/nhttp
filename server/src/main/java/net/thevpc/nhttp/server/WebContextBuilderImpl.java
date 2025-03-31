package net.thevpc.nhttp.server;

import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.impl.AbstractWebServiceController;
import net.thevpc.nhttp.server.impl.NWebServerHttpContextImpl;
import net.thevpc.nhttp.server.util.NWebAppLoggerDefault;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;

public class WebContextBuilderImpl implements WebContextBuilder {
    private WebContextBuilder.Handler handler;
    private WebContextBuilder.UserResolver userResolver;
    private WebContextBuilder.TokenResolver tokenResolver;
    private NWebLogger defaultLogger;
    private NWebLogger logger;
    private HttpServer server;
    protected String contextPath="/";

    public WebContextBuilderImpl(HttpServer server) {
        this.server = server;
    }


    public String getContextPath() {
        return contextPath;
    }

    public WebContextBuilder setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public WebContextBuilder setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public UserResolver getUserResolver() {
        return userResolver;
    }

    @Override
    public WebContextBuilder setUserResolver(UserResolver userResolver) {
        this.userResolver = userResolver;
        return this;
    }

    @Override
    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    @Override
    public WebContextBuilder setTokenResolver(TokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
        return this;
    }

    @Override
    public NWebLogger getLogger() {
        return logger;
    }

    @Override
    public WebContextBuilder setLogger(NWebLogger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public void bind() {
        AbstractWebServiceController cc = new AbstractWebServiceController(
                "/", new NWebUserResolver() {
            @Override
            public NWebToken parseToken(String token) {
                if (tokenResolver != null) {
                    return tokenResolver.parseToken(token);
                }
                return null;
            }

            @Override
            public NWebUser loadUser(NWebToken token) {
                if (userResolver != null) {
                    return userResolver.loadUser(token);
                }
                return null;
            }
        }, null) {
            @Override
            public void handle(NWebServerHttpContextImpl rc) {
                if (handler != null) {
                    handler.handle(rc);
                }
            }

            @Override
            public NWebLogger getLogger() {
                if (logger != null) {
                    return logger;
                }
                if (defaultLogger == null) {
                    defaultLogger = new NWebAppLoggerDefault(new File("server.log"), 1024 * 1024 * 500);
                }
                return defaultLogger;
            }
        };
        server.createContext(NStringUtils.firstNonBlank(contextPath,"/"), cc);
    }
}
