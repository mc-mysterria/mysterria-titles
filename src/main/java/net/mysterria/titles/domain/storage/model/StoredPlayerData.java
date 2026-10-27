package net.mysterria.titles.domain.storage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoredPlayerData {

    private String uuid;
    private String activeTitle;
    private Set<String> unlocked;
    private Map<String, Integer> progress;
    private long lastModified;
}
