package org.prototype.monitor.exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.prototype.monitor.AbstractProcessMonitorService;
import org.prototype.monitor.ProcessMonitorServiceException;
import org.prototype.web.Process;
import org.prototype.web.Window;
import org.springframework.web.context.ServletContextAware;

@Deprecated
public class Services extends AbstractProcessMonitorService implements ServletContextAware {
    private static final String         outputFileName     = "cprocess.tab";
    private static String               cprocess;
    private String                      killCommandPattern;
    private String                      killCommand;
    private String                      psCommandPattern;
    private String                      psCommand;
    private static volatile boolean     updating           = false;
    private String                      executablesLocation;

    private static Object               processes_lock     = new Object();

    public Services() throws Exception {
        super();
    }

    
    protected void killProcessSpecificImpl(Process process, Window window) throws ProcessMonitorServiceException {
        String command = getKillCommand() + process.getPid();
        WindowsExec.exec(command);
        process.stopped();
    }

    protected void startProcessSpecificImpl(Process process) throws ProcessMonitorServiceException {
        String startCommand = process.getStartCommand();
        if (startCommand.indexOf(".bat") > -1) {
            startCommand = "cmd /c start " + startCommand;
        }
        if (process.getStartCommandParameters() != null) {
            startCommand += " " + process.getStartCommandParameters(); 
        }
        WindowsExec.execInDir(startCommand, process.getDir());
    }

    protected void refreshSpecificImpl() {
        if (updating) {
            return;
        }
        synchronized (processes_lock) {
            File outputFile = null;
            try {
                updating = true;
                outputFile = getOuptputFilePath();
                // clearProcessList();
                // logger.info("refreshing process list");
                // String command = "cmd /c tasklist /FO CSV /V /NH";
                String command = getPsCommand() + " " + outputFile.getAbsolutePath();
                /*String output = */ WindowsExec.execAndWait(command);
                BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                StringBuffer sb = new StringBuffer();
                Map<String, String> lines = new HashMap<String, String>();
                List<Integer> pids = new ArrayList<Integer>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!"".equals(line)) {
                        sb.append(line + "\n");
                        lines.put(Process.parseWindowTitle(line), line);
                        pids.add(Process.parsePID(line));
                    }
                }
                Map<String, Window> windoz = new HashMap<String, Window>();
                for (Process process : getProcesses()) {
                    if (lines.containsKey(process.getWindowTitle())) {
                        process.running(lines.get(process.getWindowTitle()));
                    }
                    if (process.isRunning()) {
                        Window w = new Window(process.getPid(), process.getWindowTitle());
                        windoz.put(w.getName(), w);
                    }
                }
                setWindows(windoz);
                cprocess = sb.toString();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputFile.exists()) {
                    outputFile.delete();
                }
            }
            updating = false;
            getLogger().info("process list refreshed");
        }
    }

    public String getCprocess() {
        return cprocess;
    }

    /**
     * Sets the killCommand allowing for the possibility that {@link #executablesLocation}
     * hasn't been set yet.
     * 
     * @param killCommand
     */
    public void setKillCommand(String killCommand) {
        killCommandPattern = killCommand;
        if (executablesLocation != null) {
            this.killCommand = String.format(killCommandPattern, executablesLocation);
        }
    }

    /**
     * Gets kill command allowing for the possibility that the setter couldn't work it out at
     * initialisation time.
     * 
     * @return The kill command.
     */
    public String getKillCommand() {
        if (killCommand != null) {
            return killCommand;
        }
        synchronized (processes_lock) {
            if (killCommand == null) {
                killCommand = String.format(killCommandPattern, executablesLocation);
            }
        }
        return killCommand;
    }

    public void setPsCommand(String psCommand) {
        psCommandPattern = psCommand;
        if (executablesLocation != null) {
            this.psCommand = String.format(psCommandPattern, executablesLocation);
        }
    }

    public String getPsCommand() {
        if (psCommand != null) {
            return psCommand;
        }
        synchronized (processes_lock) {
            if (psCommand == null) {
                psCommand = String.format(psCommandPattern, executablesLocation);
            }
        }
        return psCommand;
    }
    
    public File getOuptputFilePath() throws IOException {
        File file = File.createTempFile("tmp", outputFileName);
        return file;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        //this.servletContext = servletContext;
        //Doesn't work when war file isn't unpacked for execution
        //this.executablesLocation = this.servletContext.getRealPath("/exe/");
    }

    public void setExecutablesLocation(String executablesLocation) {
        this.executablesLocation = executablesLocation;
    }
}
