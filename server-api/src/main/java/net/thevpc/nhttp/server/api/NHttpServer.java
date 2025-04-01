package net.thevpc.nhttp.server.api;

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

    NHttpServer setOptions(NWebServerOptions options);

    NHttpServer setServerName(String serverName);

    Bootstrapper getBootstrapper();

    NHttpServer setBootstrapper(Bootstrapper bootstrapper);

    NHttpServer start();

    NWebServerOptions getOptions();

    void stop();

    void stop(int delay);

    NWebContext addContext(String contextPath);

    interface Bootstrapper {
        void bootstrap(NWebConfig appWebServer);
    }
}
