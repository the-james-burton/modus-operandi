package org.prototype.web;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Process {

    private String processName;
    private int pid;
    private String priority;
    private String productName;
    private String version;
    private String description;
    private String company;
    private String windowTitle;
    private int fileSize;
    private Date fileCreatedDate;
    private Date fileModifiedDate;
    private String filename;
    private String baseAddress;
    private Date createdOn;
    private int visibleWindows;
    private int hiddenWindows;
    private String userName;
    private int memUsage;
    private int memUsagePeak;
    private int pageFaults;
    private int pagefileUsage;
    private int pagefilePeakUsage;
    private String fileAttributes;
    private String startCommand;
    private String startCommandParameters;
    private String workingDirectory;
    private File dir;
    private ProcessLog log;
    private Map<String, String> properties;
    private boolean startAll = false;
    private boolean stopAll = false;
    private String infoKey;
    private volatile ProcessState state = ProcessState.STOPPED;

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // ----------------------------------
    public Process() {
    }

    public Process(String windowsTitle, String startCommand) {
        this.windowTitle = windowsTitle;
        this.startCommand = startCommand;
    }

    /**
     * This method will update this process with the given process
     * 
     * @param line
     * @return
     */
    public void running(String line) {
        try {
            String[] fields = line.split("\t");
            this.processName = fields[0];
            this.pid = Integer.parseInt(fields[1]);
            this.priority = fields[2];
            this.productName = fields[3];
            this.version = fields[4];
            this.description = fields[5];
            this.company = fields[6];
            // this.windowTitle = fields[7];
            this.fileSize = Integer.parseInt(fields[8].replaceAll("K", "").replaceAll(",", "").trim());
            this.fileCreatedDate = dateFormat.parse(fields[9]);
            this.fileModifiedDate = dateFormat.parse(fields[10]);
            this.filename = fields[11];
            this.baseAddress = fields[12];
            this.createdOn = dateFormat.parse(fields[13]);
            this.visibleWindows = Integer.parseInt(fields[14]);
            this.hiddenWindows = Integer.parseInt(fields[15]);
            this.userName = fields[6];
            this.memUsage = Integer.parseInt(fields[17].replaceAll("K", "").replaceAll(",", "").trim());
            this.memUsagePeak = Integer.parseInt(fields[18].replaceAll("K", "").replaceAll(",", "").trim());
            this.pageFaults = Integer.parseInt(fields[19].replaceAll("K", "").replaceAll(",", "").trim());
            this.pagefileUsage = Integer.parseInt(fields[20].replaceAll("K", "").replaceAll(",", "").trim());
            this.pagefilePeakUsage = Integer.parseInt(fields[21].replaceAll("K", "").replaceAll(",", "").trim());
            if (fields.length > 22) {
                this.fileAttributes = fields[22];
            }
            this.setState(ProcessState.RUNNING);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopped() {
        this.processName = null;
        this.pid = 0;
        this.priority = null;
        this.productName = null;
        this.version = null;
        this.description = null;
        this.company = null;
        // this.windowTitle = null;
        this.fileSize = 0;
        this.fileCreatedDate = null;
        this.fileModifiedDate = null;
        this.filename = null;
        this.baseAddress = null;
        this.createdOn = null;
        this.visibleWindows = 0;
        this.hiddenWindows = 0;
        this.userName = null;
        this.memUsage = 0;
        this.memUsagePeak = 0;
        this.pageFaults = 0;
        this.pagefileUsage = 0;
        this.pagefilePeakUsage = 0;
        this.fileAttributes = null;
        this.state = ProcessState.STOPPED;
    }

    public static String parseWindowTitle(String process) {
        String[] fields = process.split("\t");
        if (fields != null && fields.length > 7) {
            return fields[7];
        } else {
            return "0";
        }
    }

    public static Integer parsePID(String process) {
        String[] fields = process.split("\t");
        return isNumeric(fields[1]) ? Integer.parseInt(fields[1]) : 0;
        //return Integer.parseInt(fields[1]);
    }
    
    private static boolean isNumeric(String input) {
        if (input == null || (input=input.trim()).isEmpty()) {
            return false;
        }

        char[] chars = input.toCharArray();
        for (char c : chars) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public String getWindowTitle() {
        return windowTitle;
    }
    
    /**
     * WindowTitle is sometimes used as an HTML id, where spaces are not allowed.
     * @return
     */
    public String getEncodedWindowTitle() {
        return windowTitle.replace(' ', '_');
    }
    public static String decodedWindowTitle(String title) {
        return title.replace('_', ' ');
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public synchronized int getPid() {
        return pid;
    }

    public synchronized void setPid(int pid) {
        this.pid = pid;
    }

    public void setLog(ProcessLog log) {
        this.log = log;
    }

    public ProcessLog getLog() {
        return log;
    }

    // -------------------------------------------
    public String getProcessName() {
        return processName;
    }

    public String getPriority() {
        return priority;
    }

    public String getProductName() {
        return productName;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getCompany() {
        return company;
    }

    public int getFileSize() {
        return fileSize;
    }

    public Date getFileCreatedDate() {
        return fileCreatedDate;
    }

    public Date getFileModifiedDate() {
        return fileModifiedDate;
    }

    public String getFilename() {
        return filename;
    }

    public String getBaseAddress() {
        return baseAddress;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public int getVisibleWindows() {
        return visibleWindows;
    }

    public int getHiddenWindows() {
        return hiddenWindows;
    }

    public String getUserName() {
        return userName;
    }

    public int getMemUsage() {
        return memUsage;
    }

    public int getMemUsagePeak() {
        return memUsagePeak;
    }

    public int getPageFaults() {
        return pageFaults;
    }

    public int getPagefileUsage() {
        return pagefileUsage;
    }

    public int getPagefilePeakUsage() {
        return pagefilePeakUsage;
    }

    public String getFileAttributes() {
        return fileAttributes;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.dir = new File(workingDirectory);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public File getDir() {
        return dir;
    }

    public synchronized void setState(ProcessState processState) {
        this.state = processState;
    }
    
    public synchronized ProcessState getState() {
        return state;
    }

    public synchronized boolean isRunning() {
        return this.state == ProcessState.RUNNING;
    }

    public synchronized boolean isStarting() {
        return this.state == ProcessState.STARTING;
    }

    public synchronized boolean isStopping() {
        return this.state == ProcessState.STOPPING;
    }

    public boolean isStartAll() {
        return startAll;
    }

    public void setStartAll(boolean startAll) {
        this.startAll = startAll;
    }

    public boolean isStopAll() {
        return stopAll;
    }

    public void setStopAll(boolean stopAll) {
        this.stopAll = stopAll;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getInfoKey() {
        return infoKey;
    }

    public void setInfoKey(String infoKey) {
        this.infoKey = infoKey;
    }
    
    public boolean getHasInfo() {
        return properties != null && properties.size() > 0;
    }
    public String getStartCommandParameters() {
        return startCommandParameters;
    }

    public void setStartCommandParameters(String startCommandParameters) {
        this.startCommandParameters = startCommandParameters;
    }
    @Override
    public String toString() {
        
        return "Process[pid: " + getPid() + ", running: " + isRunning() + ", windowTitle: " +getWindowTitle() +"]";
    }
}
