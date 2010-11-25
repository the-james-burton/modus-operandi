package org.prototype.monitor.dao.readonly;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prototype.monitor.dao.ProcessDAO;
import org.prototype.web.Process;

public class StaticProcessDAOImpl implements ProcessDAO {
    private static final Log           logger    = LogFactory.getLog(StaticProcessDAOImpl.class);
    private final Map<String, Process> processes = new TreeMap<String, Process>();
    private String                     environment;

    @Override
    public Collection<Process> getAll() {
        return processes.values();
    }

    @Override
    public Process getProcess(String windowTitle) {
        return processes.get(windowTitle);
    }

    @Override
    public Collection<String> getWindowNames() {
        return new ArrayList<String>(processes.keySet());
    }

    public void setProcesses(List<Process> proc) {
        for (Process process : proc) {
            processes.put(process.getWindowTitle(), process);
        }
    }

    public void setEnvironmentConfig(Map<String, String> environmentConfig) {
        String hostname = "test";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName().toUpperCase();
        } catch (UnknownHostException uhe) {
            logger.error("Could not determine the server's hostname, and could not set the processes list.", uhe);
        }
        if (environmentConfig.containsKey(hostname)) {
            this.environment = environmentConfig.get(hostname);
        } else {
            this.environment = "UNKNOWN";
        }
    }

    public void setProcessesConfig(Map<String, List<Process>> processes) {
        String hostname = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName().toUpperCase();
        } catch (UnknownHostException uhe) {
            logger.error("Could not determine the server's hostname, and could not set the processes list.", uhe);
        }
        if (processes.containsKey(hostname)) {
            List<Process> processList = processes.get(hostname);
            if (processList != null && processList.size() > 0) {
                setProcesses(processList);
            } else {
                logger.warn("No processes for " + hostname + " could be found. Please verify your configuration");
            }
        } else {
            // we could be in testing mode try wild card match
            Iterator<String> keyIterator = processes.keySet().iterator();
            String key = null;
            while (keyIterator.hasNext()) {
                key = keyIterator.next();
                if (hostname.matches(key)) {
                    setProcesses(processes.get(key));
                }
            }
        }
    }

    @Override
    public String getEnvironmentName() {
        return environment;
    }

    @Override
    public void delete(Process process) {
        throw new UnsupportedOperationException("ProcessDAO.delete(Process) not allowed on read-only DAO implementation");
    }

    @Override
    public void insert(Process process) {
        throw new UnsupportedOperationException("ProcessDAO.insert(Process) not allowed on read-only DAO implementation");
    }

    @Override
    public void update(Process process) {
        throw new UnsupportedOperationException("ProcessDAO.update(Process) not allowed on read-only DAO implementation");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("ProcessDAO.deleteAll() not allowed on read-only DAO implementation");
    }

    @Override
    public void insertAll(Collection<Process> processes) {
        throw new UnsupportedOperationException("ProcessDAO.insertAll() not allowed on read-only DAO implementation");
    }
}
