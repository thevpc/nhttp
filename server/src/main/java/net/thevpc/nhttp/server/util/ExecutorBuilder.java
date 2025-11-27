package net.thevpc.nhttp.server.util;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorBuilder {
    private static Logger LOG = Logger.getLogger(ExecutorBuilder.class.getName());
    public static final int DEFAULT_MIN_CONNECTIONS = 1024;
    public static final int DEFAULT_MAX_CONNECTIONS = 6 * 1024;
    public static final int DEFAULT_QUEUE_SIZE = 10;
    public static final int DEFAULT_IDLE_TIME = 10 * 60;
    private Integer minConnections;
    private Integer maxConnections;
    private Integer queueSize;
    private Integer idlTimeSeconds;
    private String name;

    public ExecutorBuilder loadConfig(String prefix, Map<String, String> props) {
        this.name = prefix;
        if (props == null) {
            return this;
        }
        this.setMinConnections(_get(prefix, "minConnections", props).asInt().orNull());
        this.setMaxConnections(_get(prefix, "maxConnections", props).asInt().orNull());
        this.setQueueSize(_get(prefix, "queueSize", props).asInt().orNull());
        this.setIdlTimeSeconds(_get(prefix, "idleTimeSeconds", props).asInt().orNull());
        return this;
    }

    public String getName() {
        return name;
    }

    public ExecutorBuilder setName(String name) {
        this.name = name;
        return this;
    }

    private NLiteral _get(String prefix, String name, Map<String, String> props) {
        return NLiteral.of(props.get(_id(prefix, name)));
    }

    private String _id(String prefix, String name) {
        if (NBlankable.isBlank(prefix)) {
            return name;
        }
        return prefix.trim() + "." + name;
    }

    public ExecutorBuilder validateOptions() {
        if (
                (minConnections == null || minConnections <= 0)
                        && (maxConnections == null || maxConnections < 0)
        ) {
            minConnections = DEFAULT_MIN_CONNECTIONS;
            maxConnections = DEFAULT_MAX_CONNECTIONS;
        } else if ((minConnections == null || minConnections <= 0)) {
            minConnections = DEFAULT_MIN_CONNECTIONS;
        } else if ((maxConnections == null || maxConnections <= 0)) {
            maxConnections = DEFAULT_MAX_CONNECTIONS;
        } else if (maxConnections < minConnections) {
            throw new IllegalArgumentException(NMsg.ofC("invalid connections bounds %s..%s", minConnections, maxConnections).toString());
        }
        if (queueSize == null || queueSize <= 0) {
            queueSize = DEFAULT_QUEUE_SIZE;
        }
        if (idlTimeSeconds == null || idlTimeSeconds <= 0) {
            idlTimeSeconds = DEFAULT_IDLE_TIME;
        }
        if (queueSize == null || queueSize <= 0) {
            queueSize = DEFAULT_QUEUE_SIZE;
        }
        return this;
    }

    public ExecutorService build() {
        validateOptions();
        if(false) {
            //System.out.printf("ThreadPoolExecutor minConnections=%s maxConnections=%s idlTimeSeconds=%s queueSize=%s%n", minConnections, maxConnections, idlTimeSeconds, queueSize);
            ThreadPoolExecutor te = new ThreadPoolExecutor(
                    minConnections, // core size
                    maxConnections, // max size
                    idlTimeSeconds, // idle timeout
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(queueSize),
                    new NamedThreadFactory("nhttp-" + name),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            String rn = r.toString();
                            LOG.log(Level.SEVERE, "{0} REJECTED THREAD {1}", new Object[]{name, rn});
                        }
                    }
            );
            if (false) {
                monitorThreadPool(te);
            }
            return te;
        }else{
            //return Executors.newWorkStealingPool();
//            return Executors.newCachedThreadPool();
            return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    10L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(),
                    new NamedThreadFactory(name),
                    new RejectedExecutionHandler() {
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            String rn = r.toString();
                            LOG.log(Level.SEVERE, "{0} REJECTED THREAD {1}", new Object[]{name, rn});
                        }
                    }
                    );
        }
        //return Executors.newCachedThreadPool();
    }

    private void monitorThreadPool(ThreadPoolExecutor te) {
        Thread t = new Thread(name+"-ThreadPoolMonitor") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //System.out.println("STATE ::: PoolSize=" + te);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public Integer getMinConnections() {
        return minConnections;
    }

    public ExecutorBuilder setMinConnections(Integer minConnections) {
        this.minConnections = minConnections;
        return this;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public ExecutorBuilder setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public ExecutorBuilder setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public Integer getIdlTimeSeconds() {
        return idlTimeSeconds;
    }

    public ExecutorBuilder setIdlTimeSeconds(Integer idlTimeSeconds) {
        this.idlTimeSeconds = idlTimeSeconds;
        return this;
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(DEFAULT_MIN_CONNECTIONS);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(DEFAULT_MIN_CONNECTIONS);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix0) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = namePrefix0 + "-" + poolNumber.getAndIncrement() + "-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
