package com.modusoperandi.monitor;

import static com.modusoperandi.model.ProcessState.RUNNING;
import static com.modusoperandi.model.ProcessState.STARTING;
import static com.modusoperandi.model.ProcessState.STOPPING;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.modusoperandi.model.ConfigEntry;
import com.modusoperandi.model.ConfigKey;
import com.modusoperandi.model.Process;
import com.modusoperandi.model.ProcessLog;
import com.modusoperandi.model.ProcessState;
import com.modusoperandi.model.Window;
import com.modusoperandi.monitor.dao.ConfigEntryDAO;
import com.modusoperandi.monitor.dao.ProcessDAO;
import com.modusoperandi.monitor.jnative.ProcessMonitorServiceJNativeImpl;

public abstract class AbstractProcessMonitorService extends TimerTask implements ProcessMonitorService {
    /**
     * Logger for this class.
     */
    private static final Log     logger                              = LogFactory.getLog(ProcessMonitorServiceJNativeImpl.class);
    /**
     * Time to wait for a process to start up.
     */
    private static final long    DEFAULT_STARTUP_DETECTION_WAIT_TIME = 30000L;
    /**
     * Time to wait for a process to shut down.
     */
    private static final long    DEFAULT_SHUT_DOWN_WAIT_TIME         = 2000L;
    /**
     * Time to sleep in between checks.
     */
    private static final long    DEFAULT_STARTUP_DETECTION_INTERVAL  = 500L;
    /**
     * The DAO providing the list of processes to be monitored.
     */
    private ProcessDAO           processDAO;
    /**
     * The DAO providing access to properties.
     */
    private ConfigEntryDAO       configDAO;
    /**
     * The in-memory representation of the processes being monitored.
     */
    private Map<String, Process> processes                           = new TreeMap<String, Process>();
    /**
     * How often the refresh mechanism should kick in.
     */
    private int                  refreshRatio;
    /**
     * The properties loader used to get properties.
     */
    private PropertiesLoader     propertiesLoader;
    /**
     * Information retrieved by the window enumeration process.
     */
    private Map<String, Window>  windows;
    /**
     * Count.
     */
    int                          count;
    /**
     * Configurable time to wait for a process detection at start time.
     */
    private long                 startUpDetectionWaitTime            = DEFAULT_STARTUP_DETECTION_WAIT_TIME;
    /**
     * Configurable time to wait for a process to shut down gracefully.
     */
    private long                 shutdownWaitTime                    = DEFAULT_SHUT_DOWN_WAIT_TIME;
    /**
     * Sleep time during checks.
     */
    private long                 startUpDetectionInterval            = DEFAULT_STARTUP_DETECTION_INTERVAL;
    /**
     * This machine's name.
     */
    private final String         machine;

    public AbstractProcessMonitorService() {
        super();
        try {
            machine = InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            throw new ProcessMonitorServiceException("Failed to initialise object.", e);
        }
    }

    @Override
    public synchronized final void killProcess(int pid) throws ProcessMonitorServiceException {
        boolean found = false;
        boolean killRequested = false;
        logger.info(String.format("Trying to stop pid %d", pid));
        for (Window window : windows.values()) {
            if (window.getPid() == pid) {
                Process process = getProcess(window.getName());
                found = process != null;
                if (found && !process.isStopping()) {
                    ProcessState preState = process.getState();
                    process.setState(STOPPING);
                    try {
                        killRequested = true;
                        killProcessSpecificImpl(process, window);
                    } catch (ProcessMonitorServiceException e) {
                        process.setState(preState);
                        throw e;
                    }
                }
            }
        }
        if (!found) {
            logger.error("Ignoring request to stop " + pid + " because it cannot be found.");
        } else if (!killRequested) {
            logger.error("Ignoring request to stop " + pid + " it's already stopping.");
        }
    }

    protected abstract void killProcessSpecificImpl(Process process, Window window) throws ProcessMonitorServiceException;

    @Override
    public final synchronized void refresh() throws ProcessMonitorServiceException {
        setProcesses(processDAO.getAll());
        refreshSpecificImpl();
        Window window = null;
        for (Process process : getProcesses()) {
            window = windows.get(process.getWindowTitle());
            if (window == null && !process.isStarting()) {
                process.setStopped();
            } else if (window != null && !process.isStopping()) {
                process.setPid(window.getPid());
                process.setState(RUNNING);
            } else {
                logger.debug("Refreshing window " + window + ", processState " + process.getState());
            }
        }
    }

    private synchronized void setProcesses(Collection<Process> newProcesses) {
        Map<String, Process> refreshed = new TreeMap<String, Process>();
        for (Process process : newProcesses) {
            String key = process.getWindowTitle();
            if (processes.containsKey(key)) {
                // Keep old reference but change any serialisable values as they might have changed
                Process oldP = processes.get(key);
                oldP.setId(process.getId());
                oldP.setInfoKey(process.getInfoKey());
                oldP.setStartCommand(process.getStartCommand());
                oldP.setStartCommandParameters(process.getStartCommandParameters());
                oldP.setWorkingDirectory(process.getWorkingDirectory());
                // same for the log
                ProcessLog oldLog = oldP.getLog();
                ProcessLog newLog = process.getLog();
                if (oldLog == null && newLog != null) {
                    oldLog = newLog;
                } else if (oldLog != null && newLog == null) {
                    oldLog = newLog;
                } else if (oldLog != null && newLog != null) {
                    oldLog.setId(newLog.getId());
                    oldLog.setPathfilename(newLog.getPathfilename());
                    oldLog.setBytes(newLog.getBytes());
                    oldLog.setLines(newLog.getLines());
                }
                oldP.setLog(oldLog);
                refreshed.put(key, oldP);
            } else {
                refreshed.put(key, process);
            }
        }
        processes = refreshed;
    }

    protected abstract void refreshSpecificImpl() throws ProcessMonitorServiceException;

    @Override
    public final synchronized void startAllProcesses() throws ProcessMonitorServiceException {
        for (Process process : getProcesses()) {
            if (process == null || process.isStarting() || process.isRunning()) {
                continue;
            }
            startProcess(process);
        }
    }

    public final synchronized void startProcess(Process process) throws ProcessMonitorServiceException {
        if (process == null) {
            logger.info("Ignoring start request for null process");
        } else if (process.isStarting() || process.isRunning()) {
            logger.info("Ignoring request for start for " + process.getWindowTitle());
        } else {
            logger.info("Starting : " + process);
            // flag this
            process.setState(STARTING);
            // perform the check in the background as this could take a while!
            Thread startUpCheckerThread = new Thread(new StartUpThread(process), "StartUpChecker[" + process.getWindowTitle() + "]");
            startUpCheckerThread.start();
        }
    }

    protected abstract void startProcessSpecificImpl(Process process) throws ProcessMonitorServiceException;

    public synchronized void removeAllProcesses() {
        processes.clear();
        processDAO.deleteAll();
    }

    public synchronized void addAllProcesses(Collection<Process> processes) {
        processDAO.insertAll(processes);
    }

    @Override
    public void removeAllConfigEntries() {
        configDAO.deleteAll();
    }

    @Override
    public void addAllConfigEntries(Collection<ConfigEntry> configEntries) {
        configDAO.insertAll(configEntries);
    }

    @Override
    public final synchronized void startProcess(String windowTitle) throws ProcessMonitorServiceException {
        startProcess(getProcess(windowTitle));
    }

    @Override
    public final synchronized void stopAllProcesses() {
        logger.info(String.format("Stopping all processes: %d", windows.size()));
        for (Window window : windows.values()) {
            killProcess(window.getPid());
        }
    }

    @Override
    public synchronized List<Process> getProcesses() {
        return processes != null ? new ArrayList<Process>(processes.values()) : new ArrayList<Process>();
    }

    public synchronized void setProcessDAO(ProcessDAO processDAO) {
        this.processDAO = processDAO;
    }

    public void setConfigDAO(ConfigEntryDAO configDAO) {
        this.configDAO = configDAO;
    }

    @Override
    public String getEnvironment() {
        ConfigEntry envName = configDAO.getConfigEntry(ConfigKey.ENVIRONMENT_NAME);
        return envName == null || envName.getValue() == null ? "UNKNOWN" : envName.getValue();
    }

    public void setRefreshRatio(int refreshRatio) {
        this.refreshRatio = refreshRatio;
    }

    public void setPropertiesLoader(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    protected synchronized void setWindows(Map<String, Window> windows) {
        this.windows = windows;
    }

    protected synchronized Collection<String> getWindowNames() {
        return processes.keySet();
    }

    @Override
    public String getMachine() {
        return machine;
    }

    @Override
    public Process getProcess(String windowTitle) {
        return processes.get(windowTitle);
    }

    protected Log getLogger() {
        return logger;
    }

    public long getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public void setShutdownWaitTime(long shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
    }

    @Override
    public void run() {
        for (Process process : getProcesses()) {
            if (process.getLog() != null) {
                process.getLog().refreshTail();
            }
        }
        // periodically refresh the process list...
        if (count++ % refreshRatio == 0) {
            refresh();
        }
    }

    private class StartUpThread implements Runnable {
        private final Process process;

        private StartUpThread(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                // Some processes take time to load properly and be detected by
                // #refresh()
                long startTime = System.currentTimeMillis();
                startProcessSpecificImpl(process);
                try {
                    process.setProperties(propertiesLoader.getProperties(getEnvironment(), process.getInfoKey(), true));
                } catch (IOException ioe) {
                    logger.error("Could not load properties.", ioe);
                }
                while (process.getPid() == 0 && System.currentTimeMillis() - startTime < startUpDetectionWaitTime) {
                    try {
                        Thread.sleep(startUpDetectionInterval);
                    } catch (Exception e) {
                        // ignore
                    }
                    refresh();
                }
                if (process.getPid() > 0) {
                    process.setState(RUNNING);
                } else {
                    // ain't gonna happen??
                    process.setStopped();
                }
            } catch (ProcessMonitorServiceException e) {
                process.setStopped();
                throw e;
            }
        }
    }
}
