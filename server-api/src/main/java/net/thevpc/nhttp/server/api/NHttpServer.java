package net.thevpc.nhttp.server.api;

import com.sun.net.httpserver.HttpServer;
import net.thevpc.nuts.util.NMsg;

public interface NHttpServer {
    NMsg getHeader();

    NHttpServer setHeader(NMsg header);

    String getDefaultLogFile();

    NHttpServer setDefaultLogFile(String defaultLogFile);

    String getDefaultPidFile();

    NHttpServer setDefaultPidFile(String defaultPidFile);

    String getServerName();

    int getServerPort();

    NWebLogger getLogger();

    HttpServer getServer();

    NHttpServer setOptions(NWebServerOptions options);

    NHttpServer setServerName(String serverName);

    Bootstrapper getBootstrapper();

    NHttpServer setBootstrapper(Bootstrapper bootstrapper);

    UserResolver getUserResolver();

    NHttpServer setUserResolver(UserResolver userResolver);

    ContextResolver getContextResolver();

    NHttpServer setContextResolver(ContextResolver contextResolver);

    Configurator getConfigurator();

    NHttpServer setConfigurator(Configurator configurator);

    NHttpServer start();

    NWebServerOptions getOptions();

    void stop();

    void stop(int delay);

    interface Bootstrapper {
        void bootstrap(NWebConfig appWebServer);
    }

    interface UserResolver {
        NWebUserResolver userResolver();
    }

    interface ContextResolver {
        void createContext(NWebContainer container);
    }

    interface Configurator {
        void initializeConfig();
    }

}
