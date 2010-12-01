package org.prototype.monitor.dao;

import java.util.Collection;

import org.prototype.web.ConfigEntry;
import org.prototype.web.ConfigKey;
import org.prototype.web.Process;

public interface ConfigEntryDAO {
    ConfigEntry getConfigEntry(ConfigKey configKey);

    Collection<Process> getAll();

    void insert(ConfigEntry configEntry);

    void update(ConfigEntry configEntry);

    void delete(ConfigEntry configEntry);

    void deleteAll();

    void insertAll(Collection<ConfigEntry> configEntries);
}
