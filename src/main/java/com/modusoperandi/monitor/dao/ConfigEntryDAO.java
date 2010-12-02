package com.modusoperandi.monitor.dao;

import java.util.Collection;

import com.modusoperandi.web.ConfigEntry;
import com.modusoperandi.web.ConfigKey;
import com.modusoperandi.web.Process;

public interface ConfigEntryDAO {
    ConfigEntry getConfigEntry(ConfigKey configKey);

    Collection<Process> getAll();

    void insert(ConfigEntry configEntry);

    void update(ConfigEntry configEntry);

    void delete(ConfigEntry configEntry);

    void deleteAll();

    void insertAll(Collection<ConfigEntry> configEntries);
}
