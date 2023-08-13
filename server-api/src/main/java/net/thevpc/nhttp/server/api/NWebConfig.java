package net.thevpc.nhttp.server.api;


public interface NWebConfig {
    NWebLogger getLogger();

    NHttpServer getServer();

    NWebServerOptions getOptions();
}
