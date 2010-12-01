package org.prototype.monitor.dao.hsql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.prototype.monitor.dao.ConfigEntryDAO;
import org.prototype.web.ConfigEntry;
import org.prototype.web.ConfigKey;
import org.prototype.web.Process;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class HSQLConfigEntryDAO extends HibernateDaoSupport implements ConfigEntryDAO {
    @SuppressWarnings("unchecked")
    @Override
    public ConfigEntry getConfigEntry(ConfigKey configKey) {
        List<ConfigEntry> result = getHibernateTemplate().findByNamedParam("select c from ConfigEntry c where c.key=:configKey", "configKey", configKey);
        return result.iterator().hasNext() ? result.iterator().next() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Process> getAll() {
        List<Process> results = getHibernateTemplate().find("from ConfigEntry");
        return results == null ? new ArrayList<Process>() : results;
    }

    @Transactional(readOnly = false)
    @Override
    public void insert(ConfigEntry configEntry) {
        getHibernateTemplate().save(configEntry);
    }

    @Transactional(readOnly = false)
    @Override
    public void update(ConfigEntry configEntry) {
        getHibernateTemplate().saveOrUpdate(configEntry);
    }

    @Transactional(readOnly = false)
    @Override
    public void delete(ConfigEntry configEntry) {
        getHibernateTemplate().delete(configEntry);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteAll() {
        getHibernateTemplate().deleteAll(getAll());
    }

    @Transactional(readOnly = false)
    @Override
    public void insertAll(Collection<ConfigEntry> configEntries) {
        getHibernateTemplate().saveOrUpdateAll(configEntries);
    }
}
