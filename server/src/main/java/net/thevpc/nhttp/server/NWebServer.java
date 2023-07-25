package net.thevpc.nhttp.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.thevpc.nhttp.server.model.NWebServerOptions;
import net.thevpc.nhttp.server.security.NWebUserResolver;
import net.thevpc.nhttp.server.util.NWebAppLoggerDefault;
import net.thevpc.nhttp.server.util.NWebLogger;
import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NLog;

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

public class NWebServer {
    private HttpServer server = null;
    private NWebServerOptions options;
    private NSession session;
    private NLog log;
    private Executor executor;
    private File pidFile;
    private Long pid = null;
    private NWebServerRunner runner;
    private NWebLogger logger;
    private String storeCredentials;
    private NMsg header;
    private String defaultLogFile;
    private String defaultPidFile;

    public NWebServer(NWebServerRunner runner, NWebServerOptions options, NSession session) {
        this.session = session;
        this.options = options;
        this.runner = runner;
        int port = options.getPort() == null ? -1 : options.getPort();
        if (port <= 0) {
            if (options.getSsl() == null || !options.getSsl()) {
                options.setSsl(false);
                port = 8080;
            } else {
                port = 8443;
            }
        } else {
            if (options.getSsl() == null) {
                if (port == 433
                        || (port >= 8400 && port <= 8499)
                        || (port >= 4000 && port <= 4999)
                ) {
                    options.setSsl(true);
                } else {
                    options.setSsl(false);
                }
            }
        }
        options.setPort(port);
        int backlog = options.getBacklog() == null ? -1 : options.getBacklog();
        if (backlog < 0) {
            backlog = 0;
        }
        options.setBacklog(backlog);
        this.log = NLog.of(NWebServer.class, session);
        if (
                (options.getMinConnexions() == null || options.getMinConnexions() <= 0)
                        && (options.getMaxConnexions() == null || options.getMaxConnexions() < 0)
        ) {
            options.setMinConnexions(1);
            options.setMaxConnexions(10);
        } else if ((options.getMinConnexions() == null || options.getMinConnexions() <= 0)) {
            options.setMinConnexions(1);
        } else if ((options.getMaxConnexions() == null || options.getMaxConnexions() <= 0)) {
            options.setMaxConnexions(10);
        } else if (options.getMaxConnexions() < options.getMinConnexions()) {
            throw new NIllegalArgumentException(this.session, NMsg.ofC("invalid connexions bounds %s..%s", options.getMinConnexions(), options.getMaxConnexions()));
        }
        if (options.getQueueSize() == null || options.getQueueSize() <= 0) {
            options.setQueueSize(5);
        }
        if (options.getIdlTimeSeconds() == null || options.getIdlTimeSeconds() <= 0) {
            options.setIdlTimeSeconds(10 * 60);
        }
        if (options.getQueueSize() == null || options.getQueueSize() <= 0) {
            options.setQueueSize(10);
        }
        executor = new ThreadPoolExecutor(
                options.getMinConnexions(), // core size
                options.getMaxConnexions(), // max size
                options.getIdlTimeSeconds(), // idle timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(options.getQueueSize())
        );
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

    public NWebServer setStoreCredentials(String storeCredentials) {
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
                    && "jdk" .equals(j.getPackaging())
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

    public void start() {
        //log.with().level(Level.INFO).verb(NLogVerb.START).log(NMsg.ofC("##start## server %s", port));
        String logFile2 = options.getLogFile();
        if(NBlankable.isBlank(logFile2)){
            logFile2= getDefaultLogFile();
        }
        if(NBlankable.isBlank(logFile2)){
            logFile2="server.log";
        }
        logger = new NWebAppLoggerDefault(new File(logFile2), session);
        if (getHeader() != null) {
            logger.out(getHeader());
        }
        runner.bootstrap(this);

        String pidFilePath = this.options.getPidFile();
        if(NBlankable.isBlank(pidFilePath)){
            pidFilePath=getDefaultPidFile();
        }
        if(NBlankable.isBlank(pidFilePath)){
            pidFilePath="server.pid";
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
        if (pid != null) {
            if (NBlankable.isBlank(pidFilePath)) {
                pidFile = normalizedFile(getDefaultPidFile());
            } else {
                pidFile = normalizedFile(pidFilePath);
            }
            if (pidFile.exists()) {
                logger.out(NMsg.ofC("      port            %s", options.getPort()));
                logger.out(NMsg.ofC("      SSL/TLS Mode    %s", options.getSsl()));
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

        logger.out(NMsg.ofC("[%s] ##start## server...", Instant.now()));
        logger.out(NMsg.ofC("      port            %s", options.getPort()));
        logger.out(NMsg.ofC("      SSL/TLS Mode    %s", options.getSsl()));
        logger.out(NMsg.ofC("      connexions      %s-%s", options.getMinConnexions(), options.getMaxConnexions()));
        logger.out(NMsg.ofC("      idle time (sec) %s", options.getIdlTimeSeconds()));
        logger.out(NMsg.ofC("      queue size      %s", options.getQueueSize()));
        logger.out(NMsg.ofC("      java-version    %s", System.getProperty("java.version")));
        logger.out(NMsg.ofC("      java-home       %s", System.getProperty("java.home")));
        logger.out(NMsg.ofC("      user-name       %s", System.getProperty("user.name")));
        logger.out(NMsg.ofC("      user-dir        %s", System.getProperty("user.dir")));
        if (pidFile != null) {
            logger.out(NMsg.ofC("      pid             %s", pid));
            logger.out(NMsg.ofC("      pid-file        %s", pidFile));
        }
        NWebUserResolver userResolver = runner.userResolver();
        try {
            new NWebServerHttpContext(null, null, userResolver, session, logger)
                    .runWithUnsafe(() -> {
                        runner.initializeConfig();
                    });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (options.getSsl()) {
            genkeypair();
            try {
                //keytool - genkey - keystore my.keystore - keyalg RSA - keysize 2048 - validity 10000 - alias app - dname
                //"cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" - storepass abcdef12 - keypass abcdef12
                // initialise the HTTPS server
                HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(options.getPort()), options.getBacklog());
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
        } else {
            try {
                server = HttpServer.create(new InetSocketAddress(options.getPort()), options.getBacklog());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        runner.createController();
        server.setExecutor(executor); // creates a default executor
        server.start();
    }

    public NWebServerOptions getOptions() {
        return options;
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

    public NWebServer setHeader(NMsg header) {
        this.header = header;
        return this;
    }

    public String getDefaultLogFile() {
        return defaultLogFile;
    }

    public NWebServer setDefaultLogFile(String defaultLogFile) {
        this.defaultLogFile = defaultLogFile;
        return this;
    }

    public String getDefaultPidFile() {
        return defaultPidFile;
    }

    public NWebServer setDefaultPidFile(String defaultPidFile) {
        this.defaultPidFile = defaultPidFile;
        return this;
    }
}
