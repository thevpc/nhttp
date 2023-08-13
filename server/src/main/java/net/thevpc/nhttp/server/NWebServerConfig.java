package net.thevpc.nhttp.server;

import net.thevpc.nhttp.server.api.NWebConfig;
import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nhttp.server.api.NHttpServer;
import net.thevpc.nhttp.server.api.NWebServerOptions;

class NWebServerConfig implements NWebConfig {
    private NHttpServer webServer;

    public NWebServerConfig(NHttpServer webServer) {
        this.webServer = webServer;
    }

    @Override
    public NWebLogger getLogger() {
        return webServer.getLogger();
    }

    @Override
    public NHttpServer getServer() {
        return webServer;
    }

    @Override
    public NWebServerOptions getOptions() {
        return webServer.getOptions();
    }
}
