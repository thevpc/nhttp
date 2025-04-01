package net.thevpc.nhttp.server.api;

import java.util.Map;
import java.util.function.Consumer;

public interface NWebContext {
    String getContextPath();

    String getDisplayName();

    String getServerName();

    Map<String, String> getInitParameters();

    NWebContext setHandler(NWebCallHandler handler);

    NWebUserResolver getUserResolver();

    NWebContext setUserResolver(NWebUserResolver userResolver);

    NWebLogger getLogger();

    NWebContext setLogger(NWebLogger logger);

    NWebContext start();

    String getPrivateKey();

    NWebTokenBuilder getDefaultTokenBuilder();

    NWebTokenEncoder getTokenEncoder();

    NWebTokenBuilder getTokenBuilder();

    NWebCallContext createContext(Object baseContext);

    NWebContext setTokenEncoder(NWebTokenEncoder encoder);

    NWebContext setTokenBuilder(NWebTokenBuilder tokenBuilder);

    NWebContext setContextPath(String contextPath);

    NWebContext setConfigurator(Configurator configurator);

    NWebCallHandler getHandler();

    interface Configurator {
        void initializeConfig();
    }

    NWebContext doWith(Consumer<NWebContext> runnable);

    long getAccessTokenDuration();

    long getRefreshTokenDuration();

}
