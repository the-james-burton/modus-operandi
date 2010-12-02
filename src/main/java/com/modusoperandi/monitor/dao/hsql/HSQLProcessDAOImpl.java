package com.modusoperandi.monitor.dao.hsql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.modusoperandi.model.Process;
import com.modusoperandi.monitor.dao.ProcessDAO;

@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class HSQLProcessDAOImpl extends HibernateDaoSupport implements ProcessDAO {
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Process> getAll() {
        List<Process> results = getHibernateTemplate().find("from Process");
        return results == null ? new ArrayList<Process>() : results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Process getProcess(String windowTitle) {
        List<Process> result = getHibernateTemplate().findByNamedParam("select p from Process p where p.windowTitle=:windowTitle", "windowTitle", windowTitle);
        return result.iterator().hasNext() ? result.iterator().next() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getWindowNames() {
        List<String> results = getHibernateTemplate().find("select p.windowTitle from Process p");
        return results == null ? new ArrayList<String>() : results;
    }

    @Transactional(readOnly = false)
    @Override
    public void insert(Process process) {
        getHibernateTemplate().save(process);
    }

    @Transactional(readOnly = false)
    @Override
    public void update(Process process) {
        getHibernateTemplate().saveOrUpdate(process);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteAll() {
        getHibernateTemplate().deleteAll(getAll());
    }

    @Transactional(readOnly = false)
    @Override
    public void delete(Process process) {
        getHibernateTemplate().delete(process);
    }

    @Transactional(readOnly = false)
    @Override
    public void insertAll(Collection<Process> processes) {
        getHibernateTemplate().saveOrUpdateAll(processes);
    }
}
