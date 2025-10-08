package net.thevpc.nhttp.server.impl;

import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nhttp.server.api.NWebUserBuilder;
import net.thevpc.nhttp.server.api.NWebUser;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class NhttpServerConfigManager implements NBlankable {
    private NPath configFile;
    private Instant lastLoaded;
    private final Map<String, NWebUser> users = new HashMap<>();
    private NWebLogger logger;

    public NhttpServerConfigManager(NPath configFile, NWebLogger logger) {
        this.configFile = configFile;
        this.logger = logger;
    }

    @Override
    public boolean isBlank() {
        synchronized (users) {
            if (!users.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, NWebUser> users() {
        tryReload();
        return users;
    }

    public void tryReload() {
        Instant otherInstant = configFile.lastModifiedInstant();
        if (lastLoaded == null || otherInstant == null || lastLoaded.isBefore(otherInstant)) {
            reload();
        }
    }

    public void addUser(NWebUser user) {
        if (user != null) {
            NAssert.requireNonBlank(user.getUserId(), "userId");
            NAssert.requireNonBlank(user.getUserName(), "userName");
            synchronized (users) {
                NWebUser ou = users.get(user.getUserName());
                if (ou != null) {
                    throw new NIllegalArgumentException(NMsg.ofC("user already registered %s", user.getUserName()));
                }
                users.put(user.getUserName(), user);
                users.put(user.getUserId(), user);
            }
        }
    }

    public void save() {
        logger.info(NMsg.ofC("saving config to %s", configFile.toAbsolute().toString()));
        List<NWebUser> usersCopy;
        synchronized (users) {
            usersCopy = new ArrayList<>(this.users.values());
        }
        NObjectElement r = NElement.ofObject(
                NElement.ofObjectBuilder("users")
                        .addComments(NElement.ofSingleLineComments(
                                "list of users",
                                "in the following form",
                                "<userName>(userId:<userName>,password:<password>,...)"
                        ))
                        .addAll(
                                usersCopy.stream()
                                        .map(x -> NElement.ofUpletBuilder(x.getUserName())
                                                .doWith(z -> {
                                                    if (!NBlankable.isBlank(x.getUserId()) && !x.getUserId().equals(x.getUserName())) {
                                                        z.add("userId", x.getUserId());
                                                    }
                                                })
                                                .doWith(z -> {
                                                            if (x.getPassword() != null) {
                                                                z.add("password", x.getPassword());
                                                            }
                                                        }
                                                )
                                                .build()).collect(Collectors.toList())
                        ).build()
        );
        NElementWriter.ofTson().write(r,configFile.mkParentDirs());
    }

    public void reload() {
        synchronized (users) {
            if (configFile.isRegularFile()) {
                logger.info(NMsg.ofC("reloading config from %s", configFile.toAbsolute().toString()));
                users.clear();
                NElement object = NElementParser.ofTson().parse(configFile);
                if (object != null) {
                    if (object.isObject() || object.isArray()) {
                        for (NElement child : object.asListContainer().get().children()) {
                            parseOneConfItem(child);
                        }
                    } else {
                        parseOneConfItem(object);
                    }
                }
                lastLoaded = configFile.lastModifiedInstant();
            } else {
                lastLoaded=null;
                logger.info(NMsg.ofC("resetting config (config file not found %s)", configFile.toAbsolute().toString()));
            }
        }
    }

    private void parseOneConfItem(NElement child) {
        NOptional<NPairElement> np = child.toNamedPair();
        if (np.isPresent()) {
            switch (np.get().name().orElse("")) {
                case "users": {
                    for (NElement nElement : np.get().toListContainer().orElseOf(() -> np.get().wrapIntoArray()).get().children()) {
                        NWebUser c = parseUser(nElement).get();
                        addUser(c);
                    }
                    break;
                }
                default: {
                    throw new NIllegalArgumentException(NMsg.ofC("unexpected config %s", np.get().name().orNull()));
                }
            }
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("unexpected %s", child.type().id()));
        }
    }

    private NOptional<NWebUser> parseUser(NElement value) {
        NWebUserBuilder wu = new NWebUserBuilder();
        List<String> names = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        List<Number> numbers = new ArrayList<>();
        NOptional<NPairElement> np = value.toNamedPair();
        if (np.isPresent()) {
            wu.setUserName(np.get().name().orNull());
            value = np.get().value();
        }
        NElement finalValue = value;
        for (NElement o : value.asListContainer()
                .orElseOf(() -> finalValue.wrapIntoArray()).get()
                .children()
        ) {
            if (o.isName()) {
                names.add(o.asStringValue().get());
            } else if (o.isString()) {
                strings.add(o.asStringValue().get());
            } else if (o.isOrdinalNumber()) {
                numbers.add(o.asNumberValue().get());
            } else if (o.isNamedPair()) {
                switch (o.asNamed().get().name().orElse("")) {
                    case "userId": {
                        wu.setUserId(o.asPair().get().value().asStringValue().get());
                        break;
                    }
                    case "userName": {
                        wu.setUserName(o.asPair().get().value().asStringValue().get());
                        break;
                    }
                    case "password": {
                        wu.setPassword(o.asPair().get().value().asStringValue().get());
                        break;
                    }
                }
            }
        }
        if (wu.getUserName() == null) {
            if (!names.isEmpty()) {
                wu.setUserName(names.remove(0));
            }
        }
        if (wu.getUserId() == null) {
            if (!numbers.isEmpty()) {
                wu.setUserId(numbers.remove(0).toString());
            }
        }
        if (wu.getUserId() == null) {
            if (!names.isEmpty()) {
                wu.setUserId(names.get(0));
            }
        }
        if (wu.getUserId() == null) {
            if (!strings.isEmpty()) {
                wu.setUserId(strings.get(0));
            }
        }
        if (wu.getUserId() == null && wu.getUserName() != null) {
            wu.setUserId(wu.getUserName());
        }
        if (wu.getUserName() == null && wu.getUserId() != null) {
            wu.setUserName(wu.getUserId());
        }
        if (!NBlankable.isBlank(wu.getUserName()) && !NBlankable.isBlank(wu.getUserId())) {
            return NOptional.of(wu.build());
        }
        return NOptional.ofNamedEmpty("user");
    }

}
