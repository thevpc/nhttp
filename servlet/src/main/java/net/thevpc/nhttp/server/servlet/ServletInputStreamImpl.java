package net.thevpc.nhttp.server.servlet;

import net.thevpc.nuts.util.NAssert;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServletInputStreamImpl extends ServletInputStream {
    private final InputStream inputStream;
    private boolean finished = false;

    public ServletInputStreamImpl(InputStream inputStream) {
        NAssert.requireNonNull(inputStream, "inputStream");
        this.inputStream = inputStream;
    }

    public final InputStream getSourceStream() {
        return this.inputStream;
    }

    public int read() throws IOException {
        int data = this.inputStream.read();
        if (data == -1) {
            this.finished = true;
        }
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int e = super.read(b, off, len);
        if(e<=0 && len>0){
            this.finished = true;
        }
        return e;
    }

    @Override
    public long skip(long n) throws IOException {
        if(n>0) {
            long e = super.skip(n);
            if (e <= 0) {
                this.finished = true;
            }
            return e;
        }
        return 0;
    }

    public int available() throws IOException {
        return this.inputStream.available();
    }

    public void close() throws IOException {
        super.close();
        this.inputStream.close();
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isReady() {
        return true;
    }

    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
