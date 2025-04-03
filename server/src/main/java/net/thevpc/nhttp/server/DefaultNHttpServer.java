package net.thevpc.nhttp.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.model.DefaultNWebContext;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

public class DefaultNHttpServer implements NHttpServer {
    private HttpServer server = null;
    private NWebServerOptions options;
    private NWebServerOptions effectiveOptions;
    private NLog log;
    private ExecutorService executor;
    private File pidFile;
    private Long pid = null;
    private File logFile;
    private long logFileMaxSize;
    private String storeCredentials;
    private NMsg header;
    private String defaultLogFile;
    private String defaultPidFile;
    private String serverName;
    private Bootstrapper bootstrapper;

    private Map<String, NWebContext> containers = new HashMap<>();

    private NWebLogger fileLogger;
    private NWebLogger userLogger;
    private boolean validLoggerFile = false;
    private boolean validPidFile = false;

    private NWebLogger safeLogger = new NWebLogger() {
        boolean headerWritten = false;

        private void writeHeader() {
            if (!headerWritten) {
                NMsg msg = getHeader();
                if (userLogger != null) {
                    userLogger.out(msg);
                } else {
                    fileLogger().out(msg);
                }
                headerWritten = true;
            }
        }

        @Override
        public void out(NMsg msg) {
            writeHeader();
            if (userLogger != null) {
                userLogger.out(msg);
                return;
            }
            fileLogger().out(msg);
        }

        @Override
        public void err(NMsg msg) {
            writeHeader();
            if (userLogger != null) {
                userLogger.err(msg);
                return;
            }
            fileLogger().err(msg);
        }
    };

    public DefaultNHttpServer() {
        this.log = NLog.of(DefaultNHttpServer.class);
    }

    public NHttpServer setOptions(NWebServerOptions options) {
        this.options = options;
        return this;
    }

    public NHttpServer setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    @Override
    public Bootstrapper getBootstrapper() {
        return bootstrapper;
    }

    @Override
    public NHttpServer setBootstrapper(Bootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
        return this;
    }

    public NHttpServer setLogger(NWebLogger logger) {
        this.userLogger = logger;
        return this;
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
        List<NPlatformLocation> java = NWorkspace.of().findPlatforms(NPlatformFamily.JAVA).toList();
        NPath keyToolOk = null;
        for (NPlatformLocation j : java) {
            NVersion jVersion = NVersion.of(j.getVersion());
            if (jVersion.compareTo("1.8") >= 0
                    && jVersion.compareTo("1.9") < 0
                    && "jdk".equals(j.getPackaging())
            ) {
                NPath keyTool = NPath.of(j.getPath()).resolve("bin/keytool");
                if (keyTool.isRegularFile()) {
                    keyToolOk = keyTool;
                    break;
                }
            }
        }

        String keytoolCmd = keyToolOk == null ? "keytool" : keyToolOk.toString();
        NExecCmd elist = NExecCmd.of()
                .addCommand(
                        keytoolCmd,
                        "-list",
                        "-keystore", storeJks.toString(),
                        "-storepass", getValidStorePass()
                        //"-keypass", "abcdef12"
                )
                .system()
                .setSleepMillis(2000)
                .grabAll();
        String outputString = elist.getGrabbedOutString();
        int result = elist.getResultCode();
        if (result == 0) {
            //found
        } else {
            storeJks.mkParentDirs();
            NExecCmd.of()
                    .system()
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
                    ).failFast()
                    .run();
        }
    }

    private NPath getStoreJks() {
        return NApp.of().getVarFolder().resolve("app-store.jks");
    }

    private NWebLogger fileLogger() {
        if (validLoggerFile) {
            return fileLogger;
        }
        NWebServerOptions _effectiveOptions = effectiveOptions();
        String logFile2 = _effectiveOptions.getLogFile();
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
        this.logFileMaxSize = _effectiveOptions.getLogFileMaxSize() == null ? -1 : _effectiveOptions.getLogFileMaxSize();
        if (this.fileLogger != null) {
            try {
                NWebAppLoggerDefault oldFileLogger = (NWebAppLoggerDefault) this.fileLogger;
                if (!Objects.equals(oldFileLogger.getBaseFile(), logFile) || !Objects.equals(oldFileLogger.getBaseMaxFileSize(), logFileMaxSize)) {
                    oldFileLogger.close();
                    this.fileLogger = new NWebAppLoggerDefault(logFile, logFileMaxSize);
                }
            } catch (Exception e) {
                //
            }
        } else {
            this.fileLogger = new NWebAppLoggerDefault(logFile, logFileMaxSize);
        }
        _effectiveOptions.setLogFile(this.logFile.getAbsolutePath());
        validLoggerFile = true;
        return this.fileLogger;
    }

    private NWebServerOptions effectiveOptions() {
        if (this.effectiveOptions == null) {
            this.effectiveOptions = OptionsValidator.validateOptions(options);
        }
        return this.effectiveOptions;
    }

    private File pidFile() {
        if (validPidFile) {
            return pidFile;
        }
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
        this.validLoggerFile = true;
        return pidFile;
    }

    @Override
    public NHttpServer start() {
        effectiveOptions();
        fileLogger();
        pidFile();
        prepareLogFile();
        preparePidFile();
        this.executor = new ExecutorBuilder()
                .setName("HTTPServer")
                .setIdlTimeSeconds(effectiveOptions.getIdlTimeSeconds())
                .setQueueSize(effectiveOptions.getQueueSize())
                .setMaxConnexions(effectiveOptions.getMaxConnexions())
                .setMinConnexions(effectiveOptions.getMinConnexions())
                .build();
        if (bootstrapper != null) {
            bootstrapper.bootstrap(new NWebServerConfig(this));
        }
        showStartupBanner();
        prepareAfterBanner();
        if (effectiveOptions.getTls()) {
            createHttpsServer();
        } else {
            createHttpServer();
        }
        String contextPath = NStringUtils.firstNonBlank(options.getContextPath(), "/");
        if (!containers.containsKey(contextPath)) {
            addContext(contextPath);
        }
        server.setExecutor(executor); // creates a default executor
        for (NWebContext value : containers.values()) {
            value.start();
        }
        server.start();
        return this;
    }

    public NWebContext addContext(String contextPath) {
        contextPath = NStringUtils.firstNonBlank(contextPath, "/");
        DefaultNWebContext c = new DefaultNWebContext(contextPath, "NhttpServer", this);
        containers.put(c.getContextPath(), c);
        return c;
    }

    private void prepareAfterBanner() {
        for (NWebContext container : containers.values()) {
            try (NWebCallContext c = container.createContext(null)) {
                c
                        .runWithUnsafe(() -> {
                                    c.initializeConfig();
                                }
                        );
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createHttpServer() {
        try {
            InetSocketAddress addr =
                    (NBlankable.isBlank(effectiveOptions.getHostName()) || "*".equals(NStringUtils.trim(effectiveOptions.getHostName())))
                            ? new InetSocketAddress(effectiveOptions.getPort()) :
                            new InetSocketAddress(effectiveOptions.getHostName(), effectiveOptions.getPort());
            server = HttpServer.create(
                    addr, effectiveOptions.getBacklog()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    @Override
    public void stop(int delay) {
        server.stop(delay);
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
                        userLogger.err(NMsg.ofPlain("Failed to create HTTPS port"));
                    }
                }
            });
            server = httpsServer;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void showStartupBanner() {
        String serverName = NStringUtils.firstNonBlank(this.serverName, "Server");
        getLogger().out(NMsg.ofC("[%s] %s %s...", serverName, NMsg.ofStyledSuccess("start"), Instant.now()));
        getLogger().out(NMsg.ofC("      port            %s", effectiveOptions.getPort()));
        getLogger().out(NMsg.ofC("      SSL/TLS Mode    %s", effectiveOptions.getTls()));
        userLogger.out(NMsg.ofC("      connexions      %s-%s", effectiveOptions.getMinConnexions(), effectiveOptions.getMaxConnexions()));
        userLogger.out(NMsg.ofC("      idle time (sec) %s", effectiveOptions.getIdlTimeSeconds()));
        userLogger.out(NMsg.ofC("      queue size      %s", effectiveOptions.getQueueSize()));
        userLogger.out(NMsg.ofC("      java-version    %s", System.getProperty("java.version")));
        userLogger.out(NMsg.ofC("      java-home       %s", System.getProperty("java.home")));
        userLogger.out(NMsg.ofC("      user-name       %s", System.getProperty("user.name")));
        userLogger.out(NMsg.ofC("      user-dir        %s", System.getProperty("user.dir")));
        userLogger.out(NMsg.ofC("      log-file        %s", logFile));
        if (pidFile != null) {
            userLogger.out(NMsg.ofC("      pid             %s", pid));
            userLogger.out(NMsg.ofC("      pid-file        %s", pidFile));
        }
    }

    private void prepareLogFile() {
        if (userLogger == null) {
            userLogger = new NWebAppLoggerDefault(logFile, logFileMaxSize);
        }
    }


    private void preparePidFile() {
        if (pid != null) {
            if (options.isIgnoreExistingPidFile()) {
                try {
                    if (pidFile.getParentFile() != null) {
                        pidFile.getParentFile().mkdirs();
                    }
                    if (pidFile.exists()) {
                        userLogger.out(NMsg.ofC("Un old pid file was %s. will be %s",
                                        NMsg.ofStyled("found", NTextStyle.warn()),
                                        NMsg.ofStyled("overridden", NTextStyle.warn())
                                )
                        );
                    }
                    Files.write(pidFile.toPath(), (pid + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                pidFile.deleteOnExit();
            } else {
                if (pidFile.exists()) {
                    if (pidFile != null) {
                        userLogger.out(NMsg.ofC("      pid             %s", pid));
                        userLogger.out(NMsg.ofC("      pid-file        %s", pidFile));
                    }
                    userLogger.out(NMsg.ofC("Server is %s.", NMsg.ofStyled("ALREADY RUNNING", NTextStyle.warn())));
                    userLogger.out(NMsg.ofStyled("ABORT! (you may want to delete pid file)", NTextStyle.fail()));
                    System.exit(1);
                }
                try {
                    if (pidFile.getParentFile() != null) {
                        pidFile.getParentFile().mkdirs();
                    }
                    Files.write(pidFile.toPath(), (pid + "\n").getBytes(), StandardOpenOption.CREATE_NEW);
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                pidFile.deleteOnExit();
            }
        }
    }

    public NWebServerOptions getOptions() {
        return effectiveOptions().copy();
    }

    public NWebLogger getLogger() {
        return safeLogger;
    }

    public HttpServer getServer() {
        return server;
    }

    @Override
    public NMsg getHeader() {
        return header;
    }

    @Override
    public NHttpServer setHeader(NMsg header) {
        this.header = header;
        return this;
    }

    @Override
    public String getDefaultLogFile() {
        return defaultLogFile;
    }

    @Override
    public NHttpServer setDefaultLogFile(String defaultLogFile) {
        this.defaultLogFile = defaultLogFile;
        return this;
    }

    @Override
    public String getDefaultPidFile() {
        return defaultPidFile;
    }

    @Override
    public DefaultNHttpServer setDefaultPidFile(String defaultPidFile) {
        this.defaultPidFile = defaultPidFile;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return effectiveOptions().getPort();
    }
}
