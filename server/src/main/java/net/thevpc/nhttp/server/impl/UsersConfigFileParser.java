package net.thevpc.nhttp.server.impl;

import net.thevpc.nhttp.server.api.DefaultNWebUser;
import net.thevpc.nhttp.server.api.NWebUser;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.elem.NUpletElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersConfigFileParser {
    private NPath userFile;
    private Instant lastLoaded;
    private Map<String, NWebUser> users = new HashMap<>();

    public UsersConfigFileParser(NPath userFile) {
        this.userFile = userFile;
    }

    public Map<String, NWebUser> users() {
        tryReload();
        return users;
    }

    public void tryReload() {
        Instant otherInstant = userFile.lastAccessInstant();
        if (lastLoaded == null || otherInstant == null || lastLoaded.isBefore(otherInstant)) {
            reload();
        }
    }

    public void reload() {
        users.clear();
        if (userFile.isRegularFile()) {
            NElement object = NElements.of().tson().parse(userFile);
            if (object != null) {
                if (object.isObject() || object.isArray()) {
                    for (NElement child : object.asListContainer().get().children()) {
                        parseOneConfItem(child);
                    }
                } else {
                    parseOneConfItem(object);
                }
            }
            lastLoaded = userFile.lastModifiedInstant();
            if (lastLoaded == null) {
                lastLoaded = Instant.now();
            }
        }
    }

    private void parseOneConfItem(NElement child) {
        if (child.isNamedUplet()) {
            NUpletElement u = child.asUplet().orNull();
            if (u != null) {
                switch (u.name()) {
                    case "user": {
                        String fullName = null;
                        String username = null;
                        String password = null;
                        List<NElement> children = u.children();
                        for (NElement nElement : children) {
                            if (nElement.isAnyString()) {
                                if (username == null) {
                                    username = nElement.asString().orNull();
                                } else if (password == null) {
                                    password = nElement.asString().orNull();
                                } else if (fullName == null) {
                                    fullName = nElement.asString().orNull();
                                }
                            } else if (nElement.isNamedPair()) {
                                NPairElement pair = nElement.asPair().get();
                                String key = pair.key().asString().get();
                                switch (NStringUtils.trim(key)) {
                                    case "username": {
                                        username = pair.value().asString().orNull();
                                        break;
                                    }
                                    case "password": {
                                        password = pair.value().asString().orNull();
                                        break;
                                    }
                                    case "fullName": {
                                        fullName = pair.value().asString().orNull();
                                        break;
                                    }
                                }
                            }
                        }
                        DefaultNWebUser w = new DefaultNWebUser(username, fullName, password);
                        users.put(w.getUserId(), w);
                    }
                }
            }
        }
    }

}
