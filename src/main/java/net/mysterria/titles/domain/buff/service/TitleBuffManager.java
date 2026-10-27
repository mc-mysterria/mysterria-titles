package net.mysterria.titles.domain.buff.service;

import lombok.RequiredArgsConstructor;
import net.mysterria.titles.domain.buff.type.TitleBuff;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TitleBuffManager {

    private final Logger logger;
    private final Map<String, TitleBuff> buffs = new LinkedHashMap<>();

    public void register(TitleBuff buff) {
        if (buffs.containsKey(buff.id())) {
            throw new IllegalArgumentException("Duplicate buff id: " + buff.id());
        }
        buffs.put(buff.id(), buff);
    }

    public void registerAndEnable(TitleBuff buff) {
        register(buff);
        buff.register();
    }

    public void validateAgainstRegistry(TitleRegistry registry) {
        for (Title title : registry.all()) {
            String buffId = title.getBonus().getType();
            if (!buffs.containsKey(buffId)) {
                logger.warning("Title '" + title.getId() + "' references unknown buff id '" + buffId + "'; its bonus will be inert.");
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
