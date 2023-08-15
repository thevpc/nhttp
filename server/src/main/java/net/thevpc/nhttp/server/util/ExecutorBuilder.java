package net.thevpc.nhttp.server.util;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorBuilder {
    private static Logger LOG = Logger.getLogger(ExecutorBuilder.class.getName());
    public static final int DEFAULT_MIN_CONNEXIONS = 1024;
    public static final int DEFAULT_MAX_CONNEXIONS = 6 * 1024;
    public static final int DEFAULT_QUEUE_SIZE = 1;
    public static final int DEFAULT_IDLE_TIME = 10 * 60;
    private Integer minConnexions;
    private Integer maxConnexions;
    private Integer queueSize;
    private Integer idlTimeSeconds;
    private String name;

    public ExecutorBuilder loadConfig(String prefix, Map<String, String> props) {
        this.name = prefix;
        if (props == null) {
            return this;
        }
        this.setMinConnexions(_get(prefix, "minConnexions", props).asInt().orNull());
        this.setMaxConnexions(_get(prefix, "maxConnexions", props).asInt().orNull());
        this.setQueueSize(_get(prefix, "queueSize", props).asInt().orNull());
        this.setIdlTimeSeconds(_get(prefix, "idleTimeSeconds", props).asInt().orNull());
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
                (minConnexions == null || minConnexions <= 0)
                        && (maxConnexions == null || maxConnexions < 0)
        ) {
            minConnexions = DEFAULT_MIN_CONNEXIONS;
            maxConnexions = DEFAULT_MAX_CONNEXIONS;
        } else if ((minConnexions == null || minConnexions <= 0)) {
            minConnexions = DEFAULT_MIN_CONNEXIONS;
        } else if ((maxConnexions == null || maxConnexions <= 0)) {
            maxConnexions = DEFAULT_MAX_CONNEXIONS;
        } else if (maxConnexions < minConnexions) {
            throw new IllegalArgumentException(NMsg.ofC("invalid connexions bounds %s..%s", minConnexions, maxConnexions).toString());
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
            //System.out.printf("ThreadPoolExecutor minConnexions=%s maxConnexions=%s idlTimeSeconds=%s queueSize=%s%n", minConnexions, maxConnexions, idlTimeSeconds, queueSize);
            ThreadPoolExecutor te = new ThreadPoolExecutor(
                    minConnexions, // core size
                    maxConnexions, // max size
                    idlTimeSeconds, // idle timeout
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(queueSize),
                    new NamedThreadFactory("hal-" + name),
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
            return Executors.newCachedThreadPool();
        }
        //return Executors.newCachedThreadPool();
    }

    private void monitorThreadPool(ThreadPoolExecutor te) {
        new Thread() {
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
        }.start();
    }

    public Integer getMinConnexions() {
        return minConnexions;
    }

    public ExecutorBuilder setMinConnexions(Integer minConnexions) {
        this.minConnexions = minConnexions;
        return this;
    }

    public Integer getMaxConnexions() {
        return maxConnexions;
    }

    public ExecutorBuilder setMaxConnexions(Integer maxConnexions) {
        this.maxConnexions = maxConnexions;
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
        private static final AtomicInteger poolNumber = new AtomicInteger(DEFAULT_MIN_CONNEXIONS);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(DEFAULT_MIN_CONNEXIONS);
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
