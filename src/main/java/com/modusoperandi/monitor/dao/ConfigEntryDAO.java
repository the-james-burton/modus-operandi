package com.modusoperandi.monitor.dao;

import java.util.Collection;

import com.modusoperandi.model.ConfigEntry;
import com.modusoperandi.model.ConfigKey;
import com.modusoperandi.model.Process;

public interface ConfigEntryDAO {
    ConfigEntry getConfigEntry(ConfigKey configKey);

    Collection<Process> getAll();

    void insert(ConfigEntry configEntry);

    void update(ConfigEntry configEntry);

    void delete(ConfigEntry configEntry);

    void deleteAll();

    void insertAll(Collection<ConfigEntry> configEntries);
}
