package net.thevpc.nhttp.server.util;

//import com.google.gson.GsonBuilder;

import com.google.gson.GsonBuilder;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;

import java.io.BufferedWriter;
import java.io.Reader;

public class JsonUtils {
    private static final boolean useNuts = false;

    public static String toJson(Object object, NSession session) {
        if (useNuts) {
            return NElements.of(session).json().setNtf(false).setValue(object).format().filteredText();
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type, NSession session) {
        if (useNuts) {
            T r = NElements.of(session).json().setNtf(false).parse(json, type);
            return r;
        }
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static <T> T fromJson(Reader json, Class<T> type, NSession session) {
        if (useNuts) {
            T r = NElements.of(session).json().setNtf(false).parse(json, type);
            return r;
        }
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static void toJson(Object object, BufferedWriter r, NSession session) {
        if (useNuts) {
            NElements.of(session).json().setNtf(false).setValue(object).println(r);
        } else {
            new GsonBuilder().setPrettyPrinting().create().toJson(object, r);
        }
    }

    public static <T> T copy(T t, NSession session) {
        return (T) fromJson(toJson(t, session), t.getClass(), session);
    }
}
