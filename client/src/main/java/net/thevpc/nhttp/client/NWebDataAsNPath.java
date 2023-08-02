package net.thevpc.nhttp.client;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NWebDataAsNPath implements NWebData {
    NPath data;

    public NWebDataAsNPath(NPath data) {
        NAssert.requireNonNull(data, "data");
        this.data = data;
    }

    @Override
    public long length() {
        return data.getContentLength();
    }

    @Override
    public InputStream stream() {
        return data.getInputStream();
    }

    @Override
    public byte[] bytes() {
        return data.readBytes();
    }
}
