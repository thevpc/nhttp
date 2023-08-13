package net.thevpc.nhttp.server.servlet;

import net.thevpc.nuts.util.NAssert;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

public class ServletOutputStreamImpl extends ServletOutputStream {
    private final OutputStream targetStream;
    private final Runnable onStart;
    private boolean started;

    public ServletOutputStreamImpl(OutputStream targetStream, Runnable onStart) {
        NAssert.requireNonNull(targetStream, "Target OutputStream must not be null");
        this.targetStream = targetStream;
        this.onStart = onStart;
    }

    public final OutputStream getTargetStream() {
        return this.targetStream;
    }

    private void start(){
        if(!started){
            started=true;
            if(onStart!=null) {
                onStart.run();
            }
        }
    }

    public void write(int b) throws IOException {
        start();
        this.targetStream.write(b);
    }

    public void flush() throws IOException {
        start();
        super.flush();
        this.targetStream.flush();
    }

    public void close() throws IOException {
        start();
        super.close();
        this.targetStream.close();
    }

    public boolean isReady() {
        return true;
    }

    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }


}
