package net.mysterria.titles.domain.buff.service;

import net.mysterria.titles.domain.buff.model.TitleBuff;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public final class TitleBuffManager {

    private final Logger logger;
    private final Map<String, TitleBuff> buffs = new LinkedHashMap<>();

    public TitleBuffManager(Logger logger) {
        this.logger = logger;
    }

    public void register(TitleBuff buff) {
        if (buffs.containsKey(buff.id())) {
            throw new IllegalArgumentException("Duplicate buff id: " + buff.id());
        }
        buffs.put(buff.id(), buff);
    }

    public Optional<TitleBuff> get(String id) {
        return Optional.ofNullable(buffs.get(id));
    }

    public Set<String> registeredIds() {
        return Set.copyOf(buffs.keySet());
    }

    public void validateAgainstRegistry(TitleRegistry registry) {
        for (Title title : registry.all()) {
            String buffId = title.bonus().type();
            if (!buffs.containsKey(buffId)) {
                logger.warning("Title '" + title.id() + "' references unknown buff id '" + buffId + "'; its bonus will be inert.");
            }
        }
    }

    public void enableAll() {
        buffs.values().forEach(TitleBuff::register);
    }

    public void disableAll() {
        buffs.values().forEach(TitleBuff::unregister);
    }
}
