package net.thevpc.nhttp.client;

import net.thevpc.nuts.util.NAssert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NWebDataAsBytes implements NWebData {
    byte[] data;

    public NWebDataAsBytes(byte[] data) {
        NAssert.requireNonNull(data,"data");
        this.data = data;
    }

    @Override
    public long length() {
        return data.length;
    }

    @Override
    public InputStream stream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public byte[] bytes() {
        return data;
    }
}
