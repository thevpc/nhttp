package net.thevpc.nhttp.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.impl.NWebServerHttpContextImpl;
import net.thevpc.nhttp.server.model.DefaultNWebContainer;
import net.thevpc.nhttp.server.util.ExecutorBuilder;
import net.thevpc.nhttp.server.util.NWebAppLoggerDefault;
import net.thevpc.nhttp.server.util.OptionsValidator;
import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.util.NStringUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

public class DefaultNHttpServer implements NHttpServer {
    private HttpServer server = null;
    private NWebServerOptions options;
    private NWebServerOptions effectiveOptions;
    private NSession session;
    private NLog log;
    private ExecutorService executor;
    private File pidFile;
    private Long pid = null;
    private NWebServerRunner runner;
    private NWebLogger logger;
    private File logFile;
    private String storeCredentials;
    private NMsg header;
    private String defaultLogFile;
    private String defaultPidFile;
    private String serverName;

    public DefaultNHttpServer(String serverName, NWebServerRunner runner, NWebServerOptions options, NSession session) {
        this.serverName = serverName;
        this.session = session;
        this.options = options;
        this.runner = runner;
        this.log = NLog.of(DefaultNHttpServer.class, session);
    }

    private File normalizedFile(String str) {
        File file = new File(str);
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return file.getAbsoluteFile();
        }
    }

    public String getStoreCredentials() {
        return storeCredentials;
    }

    public DefaultNHttpServer setStoreCredentials(String storeCredentials) {
        this.storeCredentials = storeCredentials;
        return this;
    }

    private String getValidStorePass() {
        if (storeCredentials != null && storeCredentials.length() > 0) {
            return storeCredentials;
        }
        return "abcdef12";
    }

    public void genkeypair() {
        NPath storeJks = getStoreJks();
        List<NPlatformLocation> java = NPlatforms.of(session).findPlatforms(NPlatformFamily.JAVA).toList();
        NPath keyToolOk = null;
        for (NPlatformLocation j : java) {
            NVersion jVersion = NVersion.of(j.getVersion()).get();
            if (jVersion.compareTo("1.8") >= 0
                    && jVersion.compareTo("1.9") < 0
                    && "jdk".equals(j.getPackaging())
            ) {
                NPath keyTool = NPath.of(j.getPath(), session).resolve("bin/keytool");
                if (keyTool.isRegularFile()) {
                    keyToolOk = keyTool;
                    break;
                }
            }
        }

        String keytoolCmd = keyToolOk == null ? "keytool" : keyToolOk.toString();
        NExecCommand elist = NExecCommand.of(session)
                .addCommand(
                        keytoolCmd,
                        "-list",
                        "-keystore", storeJks.toString(),
                        "-storepass", getValidStorePass()
                        //"-keypass", "abcdef12"
                )
                .setExecutionType(NExecutionType.SYSTEM)
                .setSleepMillis(2000)
                .grabOutputString()
                .grabErrorString();
        String outputString = elist.getOutputString();
        String errorString = elist.getErrorString();
        int result = elist.getResult();
        if (result == 0) {
            //found
        } else {
            storeJks.mkParentDirs();
            NExecCommand.of(session)
                    .setExecutionType(NExecutionType.SYSTEM)
                    .addCommand(
                            keytoolCmd,
                            "-genkeypair",
                            "-keystore", storeJks.toString(),
                            "-keyalg", "RSA",
                            "-keysize", "2048",
                            "-validity", "10000",
                            "-alias", "selfsigned",
                            "-dname", "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown",
                            "-storepass", getValidStorePass(),
                            "-keypass", getValidStorePass()
                    ).setFailFast(true)
                    .run();
        }
    }

    private NPath getStoreJks() {
        return session.getAppVarFolder().resolve("app-store.jks");
    }

    private void compile() {
        if (this.effectiveOptions == null) {
            this.effectiveOptions = OptionsValidator.validateOptions(options);
            String logFile2 = effectiveOptions.getLogFile();
            if (NBlankable.isBlank(logFile2)) {
                logFile2 = getDefaultLogFile();
            }
            if (NBlankable.isBlank(logFile2)) {
                logFile2 = serverName + ".log";
            }
            if (NBlankable.isBlank(logFile2)) {
                logFile2 = "server.log";
            }
            this.logFile = normalizedFile(logFile2);
            this.effectiveOptions.setLogFile(this.logFile.getAbsolutePath());
            String pidFilePath = this.effectiveOptions.getPidFile();
            if (NBlankable.isBlank(pidFilePath)) {
                pidFilePath = getDefaultPidFile();
            }
            if (NBlankable.isBlank(pidFilePath)) {
                pidFilePath = serverName + ".pid";
            }
            if (NBlankable.isBlank(pidFilePath)) {
                pidFilePath = "server.pid";
            }
            String pname = null;
            try {
                pname = ManagementFactory.getRuntimeMXBean().getName();
            } catch (Exception e) {
                //
            }
            if (pname != null && pname.matches("[0-9]+@.*")) {
                pid = Long.parseLong(pname.substring(0, pname.indexOf('@')));
            }
            if (NBlankable.isBlank(pidFilePath)) {
                pidFile = normalizedFile(getDefaultPidFile());
            } else {
                pidFile = normalizedFile(pidFilePath);
            }
            this.effectiveOptions.setPidFile(pidFile.getAbsolutePath());
        }
    }

    public void start() {
        compile();
        prepareLogFile();
        preparePidFile();
        this.executor = new ExecutorBuilder()
                .setIdlTimeSeconds(effectiveOptions.getIdlTimeSeconds())
                .setQueueSize(effectiveOptions.getQueueSize())
                .setMaxConnexions(effectiveOptions.getMaxConnexions())
                .setMinConnexions(effectiveOptions.getMinConnexions())
                .build();
        runner.bootstrap(new NWebServerConfig(this));
        showStartupBanner();
        prepareAfterBanner();
        if (effectiveOptions.getSsl()) {
            createHttpsServer();
        } else {
            createHttpServer();
        }
        runner.createContext(new DefaultNWebContainer(options.getContextPath(), "NhttpServer"));
        server.setExecutor(executor); // creates a default executor
        server.start();
    }

    private void prepareAfterBanner() {
        NWebUserResolver userResolver = runner.userResolver();
        try {
            new NWebServerHttpContextImpl(null, null, userResolver, session, logger)
                    .runWithUnsafe(() -> {
                        runner.initializeConfig();
                    });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void createHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(effectiveOptions.getPort()), effectiveOptions.getBacklog());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void createHttpsServer() {
        genkeypair();
        try {
            //keytool - genkey - keystore my.keystore - keyalg RSA - keysize 2048 - validity 10000 - alias app - dname
            //"cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" - storepass abcdef12 - keypass abcdef12
            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(effectiveOptions.getPort()), effectiveOptions.getBacklog());
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            NPath storeJks = getStoreJks();
            char[] password = getValidStorePass().toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(storeJks.getInputStream(), password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        logger.err(NMsg.ofPlain("Failed to create HTTPS port"));
                    }
                }
            });
            server = httpsServer;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void showStartupBanner() {
        logger.out(NMsg.ofC("[%s] ##start## %s...",
                NBlankable.isBlank(serverName) ? "server" :
                        NStringUtils.trim(serverName)
                , Instant.now()));
        logger.out(NMsg.ofC("      port            %s", effectiveOptions.getPort()));
        logger.out(NMsg.ofC("      SSL/TLS Mode    %s", effectiveOptions.getSsl()));
        logger.out(NMsg.ofC("      connexions      %s-%s", effectiveOptions.getMinConnexions(), effectiveOptions.getMaxConnexions()));
        logger.out(NMsg.ofC("      idle time (sec) %s", effectiveOptions.getIdlTimeSeconds()));
        logger.out(NMsg.ofC("      queue size      %s", effectiveOptions.getQueueSize()));
        logger.out(NMsg.ofC("      java-version    %s", System.getProperty("java.version")));
        logger.out(NMsg.ofC("      java-home       %s", System.getProperty("java.home")));
        logger.out(NMsg.ofC("      user-name       %s", System.getProperty("user.name")));
        logger.out(NMsg.ofC("      user-dir        %s", System.getProperty("user.dir")));
        logger.out(NMsg.ofC("      log-file        %s", logFile));
        if (pidFile != null) {
            logger.out(NMsg.ofC("      pid             %s", pid));
            logger.out(NMsg.ofC("      pid-file        %s", pidFile));
        }
    }

    private void prepareLogFile() {
        logger = new NWebAppLoggerDefault(logFile, session);
        if (getHeader() != null) {
            logger.out(getHeader());
        }
    }


    private void preparePidFile() {
        if (pid != null) {
            if (pidFile.exists()) {
                if (pidFile != null) {
                    logger.out(NMsg.ofC("      pid             %s", pid));
                    logger.out(NMsg.ofC("      pid-file        %s", pidFile));
                }
                logger.out(NMsg.ofC("Server is %s.", NMsg.ofStyled("ALREADY RUNNING", NTextStyle.warn())));
                logger.out(NMsg.ofStyled("ABORT! (you may want to delete pid file)", NTextStyle.fail()));
                System.exit(1);
            }
            try {
                if (pidFile.getParentFile() != null) {
                    pidFile.getParentFile().mkdirs();
                }
                Files.write(pidFile.toPath(), (pid + "\n").getBytes(), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
            pidFile.deleteOnExit();
        }
    }

    public NWebServerOptions getOptions() {
        compile();
        return effectiveOptions.copy();
    }

    public NWebLogger getLogger() {
        return logger;
    }

    public NSession getSession() {
        return session;
    }

    public HttpServer getServer() {
        return server;
    }

    public NMsg getHeader() {
        return header;
    }

    public DefaultNHttpServer setHeader(NMsg header) {
        this.header = header;
        return this;
    }

    public String getDefaultLogFile() {
        return defaultLogFile;
    }

    public DefaultNHttpServer setDefaultLogFile(String defaultLogFile) {
        this.defaultLogFile = defaultLogFile;
        return this;
    }

    public String getDefaultPidFile() {
        return defaultPidFile;
    }

    public DefaultNHttpServer setDefaultPidFile(String defaultPidFile) {
        this.defaultPidFile = defaultPidFile;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        compile();
        return effectiveOptions.getPort();
    }
}
