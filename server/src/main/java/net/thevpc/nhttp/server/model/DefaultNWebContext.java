package net.thevpc.nhttp.server.model;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.DefaultNHttpServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.impl.DefaultNWebTokenBuilder;
import net.thevpc.nhttp.server.impl.DefaultWebServiceController;
import net.thevpc.nhttp.server.impl.JwtWebTokenEncoder;
import net.thevpc.nhttp.server.impl.NWebCallContextImpl;
import net.thevpc.nhttp.server.util.NWebAppLoggerDefault;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DefaultNWebContext implements NWebContext {
    private String contextPath;
    private String displayName;
    private Map<String, String> initParameters;

    private NWebCallHandler handler;
    private NWebUserResolver userResolver;
    private NWebLogger defaultLogger;
    private NWebLogger logger;
    private String defaultPrivateKey = "nhttp-server-default-key-" + UUID.randomUUID().toString();
    private String privateKey;
    private DefaultWebServiceController cc;
    private Configurator configurator;
    private NHttpServer nHttpServer;
    private NWebTokenBuilder webTokenBuilder;
    private NWebTokenBuilder defaultTokenBuilder = new DefaultNWebTokenBuilder();
    private NWebTokenEncoder webTokenEncoder;
    private NWebTokenEncoder defaultTokenEncoder = new JwtWebTokenEncoder();


    long accessTokenDuration = 0;
    long refreshTokenDuration = 0;
    private static long ONE_HOUR = 60 * 60 * 1000;

    public DefaultNWebContext(String contextPath, String displayName, NHttpServer nHttpServer) {
        this.contextPath = NStringUtils.firstNonBlank(NStringUtils.strip(contextPath), "/");
        this.displayName = NStringUtils.firstNonBlank(displayName, "Path " + contextPath);
        this.initParameters = new HashMap<>();
        this.nHttpServer = nHttpServer;
    }

    @Override
    public String getServerName() {
        return nHttpServer.getServerName();
    }

    @Override
    public NWebTokenBuilder getDefaultTokenBuilder() {
        return defaultTokenBuilder;
    }

    public NWebTokenEncoder getTokenEncoder() {
        return webTokenEncoder == null ? defaultTokenEncoder : webTokenEncoder;
    }

    public NWebContext setTokenEncoder(NWebTokenEncoder webTokenEncoder) {
        this.webTokenEncoder = webTokenEncoder;
        return this;
    }

    @Override
    public NWebTokenBuilder getTokenBuilder() {
        return webTokenBuilder == null ? getDefaultTokenBuilder() : webTokenBuilder;
    }

    @Override
    public NWebContext setTokenBuilder(NWebTokenBuilder tokenBuilder) {
        this.webTokenBuilder = tokenBuilder;
        return this;
    }

    public long getAccessTokenDuration() {
        return accessTokenDuration <= 0 ? 23 * ONE_HOUR : accessTokenDuration;
    }

    public long getRefreshTokenDuration() {
        long d0 = getAccessTokenDuration();
        long d = refreshTokenDuration <= 0 ? 23 * ONE_HOUR : refreshTokenDuration;
        if (d <= d0) {
            d = 2 * d0;
        }
        return d;
    }

    @Override
    public NWebContext setConfigurator(Configurator configurator) {
        this.configurator = configurator;
        return this;
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    @Override
    public NWebCallContext createContext(Object baseContext) {
        return new NWebCallContextImpl(this, (HttpExchange) baseContext);
    }

    public String getPrivateKey() {
        if (!NBlankable.isBlank(privateKey)) {
            return privateKey;
        }
        return defaultPrivateKey;
    }

    public NWebContext setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public HttpServer getServer() {
        return ((DefaultNHttpServer) nHttpServer).getServer();
    }

    public String getContextPath() {
        return NStringUtils.firstNonBlank(contextPath, "/");
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }


    @Override
    public NWebContext setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    @Override
    public NWebCallHandler getHandler() {
        return handler;
    }

    @Override
    public NWebContext setHandler(NWebCallHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public NWebUserResolver getUserResolver() {
        return userResolver;
    }

    @Override
    public NWebContext setUserResolver(NWebUserResolver userResolver) {
        this.userResolver = userResolver;
        return this;
    }

    @Override
    public NWebContext setLogger(NWebLogger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public NWebContext start() {
        if (cc == null) {
            cc = new DefaultWebServiceController(this);
            getServer().createContext(getContextPath(), cc);
        }
        return this;
    }

    @Override
    public NWebLogger getLogger() {
        if (logger != null) {
            return logger;
        }

        NWebLogger logger2 = nHttpServer.getLogger();
        if (logger2 != null) {
            return logger2;
        }

        if (defaultLogger == null) {
            defaultLogger = new NWebAppLoggerDefault(new File("server.log"), 1024 * 1024 * 500);
        }
        return defaultLogger;
    }

    @Override
    public NWebContext doWith(Consumer<NWebContext> runnable) {
        if (runnable != null) {
            runnable.accept(this);
        }
        return this;
    }

}
