package org.prototype.monitor;

import static org.prototype.web.ProcessState.RUNNING;
import static org.prototype.web.ProcessState.STARTING;
import static org.prototype.web.ProcessState.STOPPING;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prototype.monitor.jnative.ProcessMonitorServiceJNativeImpl;
import org.prototype.web.Process;
import org.prototype.web.ProcessState;
import org.prototype.web.PropertiesLoader;
import org.prototype.web.Window;

public abstract class AbstractProcessMonitorService extends TimerTask implements
        ProcessMonitorService {

    /**
     * Logger for this class.
     */
    private static final Log logger = LogFactory.getLog(ProcessMonitorServiceJNativeImpl.class);

    /**
     * Time to wait for a process to start up.
     */
    private static final long DEFAULT_STARTUP_DETECTION_WAIT_TIME = 30000L;

    /**
     * Time to sleep in between checks.
     */
    private static final long DEFAULT_STARTUP_DETECTION_INTERVAL = 500L;

    /**
     * The list of processes to be monitored
     */
    private Map<String, Process> processes = new TreeMap<String, Process>();

    /**
     * The environment this process is running under (UAT, BAU etc)
     */
    private String environment;

    /**
     * How often the refresh mechanism should kick in.
     */
    private int refreshRatio;

    /**
     * The properties loader used to get properties.
     */
    private PropertiesLoader propertiesLoader;

    /**
     * Information retrieved by the window enumeration process.
     */
    private Map<String, Window> windows;

    /**
     * Count.
     */
    int count;

    /**
     * Configurable time to wait for a process detection at start time.
     */
    private long startUpDetectionWaitTime = DEFAULT_STARTUP_DETECTION_WAIT_TIME;

    /**
     * Sleep time during checks.
     */
    private long startUpDetectionInterval = DEFAULT_STARTUP_DETECTION_INTERVAL;

    /**
     * This machine's name.
     */
    private final String machine;
    
    public AbstractProcessMonitorService() {
        super();
        try {
            machine = InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            throw new ProcessMonitorServiceException("Failed to initialise object.", e);
        }
    }

    @Override
    public final void killProcess(int pid) throws ProcessMonitorServiceException {
        boolean found = false;
        for (Window window : windows.values()) {
            if (window.getPid() == pid) {
                Process process = processes.get(window.getName());
                ProcessState preState = process.getState();
                if (process != null && !process.isStopping()) {
                    found = true;
                    process.setState(STOPPING);
                    try {
                        killProcessSpecificImpl(process, window);
                    } catch (ProcessMonitorServiceException e) {
                        process.setState(preState);
                        throw e;
                    }
                }
            }

            if (!found) {
                logger.error("Ignoring request to stop "
                        + pid
                        + " because it's either already stopping or not there");
            }
        }
    }

    protected abstract void killProcessSpecificImpl(Process process, Window window) throws ProcessMonitorServiceException;

    @Override
    public final void refresh() throws ProcessMonitorServiceException {
        refreshSpecificImpl();
        Window window = null;
        for (Process process : processes.values()) {
            window = windows.get(process.getWindowTitle());
            if (window == null && !process.isStarting()) {
                process.stopped();
            } else if (window != null && !process.isStopping()) {
                process.setPid(window.getPid());
                process.setState(RUNNING);
            } else {
                logger.debug("Refreshing window " + window + ", processState " + process.getState());
            }
        }
    }

    protected abstract void refreshSpecificImpl() throws ProcessMonitorServiceException;

    @Override
    public final synchronized void startAllProcesses() throws ProcessMonitorServiceException {
        for (Process process : processes.values()) {
            if (process == null || process.isStarting() || process.isRunning()) {
                continue;
            }
            startProcess(process);
        }
    }

    public final synchronized void startProcess(Process process)
            throws ProcessMonitorServiceException {
        logger.info("Starting : " + process);

        if (process == null || process.isStarting() || process.isRunning()) {
            logger.info("Ignoring request for start for " + process.getWindowTitle());
            return;
        }
        // flag this
        process.setState(STARTING);

        // perform the check in the background as this could take a while!
        Thread startUpCheckerThread = new Thread(new StartUpThread(process),
                "StartUpChecker[" + process.getWindowTitle() + "]");
        startUpCheckerThread.start();
    }

    protected abstract void startProcessSpecificImpl(Process process) throws ProcessMonitorServiceException;

    @Override
    public final synchronized void startProcess(String windowTitle) throws ProcessMonitorServiceException {
        startProcess(processes.get(windowTitle));
    }

    @Override
    public final synchronized void stopAllProcesses() {
        for (Window window : windows.values()) {
            killProcess(window.getPid());
        }
    }

    @Override
    public List<Process> getProcesses() {
        return new ArrayList<Process>(processes.values());
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
                //we could be in testing mode try wild card match
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
    public String getEnvironment() {
        return environment;
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
        return this.processes.keySet();
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

    @Override
    public void run() {
        for (Process process : processes.values()) {
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
        private Process process;

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
                    process.setProperties(propertiesLoader.getProperties(
                            environment, process.getInfoKey(), true));
                } catch (IOException ioe) {
                    logger.error("Could not load properties.", ioe);
                }
                while (process.getPid() == 0
                        && System.currentTimeMillis() - startTime < startUpDetectionWaitTime) {
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
                    process.stopped();
                }
            } catch (ProcessMonitorServiceException e) {
                process.stopped();
                throw e;
            }
        }
    }
}
