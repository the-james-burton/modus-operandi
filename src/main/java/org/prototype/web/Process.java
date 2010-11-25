package org.prototype.web;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@javax.persistence.Entity
@Table(name = "Process", uniqueConstraints = { @UniqueConstraint(columnNames = "WindowTitle"), @UniqueConstraint(columnNames = "InfoKey") })
public class Process {
    private Long                id;
    private String              windowTitle;
    private String              startCommand;
    private String              startCommandParameters;
    private String              workingDirectory;
    private ProcessLog          log;
    private String              infoKey;
    private ProcessState        state;
    private int                 pid;
    private Map<String, String> properties;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "Id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "WindowTitle", length = 300)
    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Column(name = "StartCommand", length = 1000)
    public String getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }

    @Column(name = "Parameters", length = 300)
    public String getStartCommandParameters() {
        return startCommandParameters;
    }

    public void setStartCommandParameters(String startCommandParameters) {
        this.startCommandParameters = startCommandParameters;
    }

    @Column(name = "WorkingDirectory", length = 1000)
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @ManyToOne(optional = true, cascade = CascadeType.ALL)
    public ProcessLog getLog() {
        return log;
    }

    public void setLog(ProcessLog log) {
        this.log = log;
    }

    @Column(name = "InfoKey", length = 30)
    public String getInfoKey() {
        return infoKey;
    }

    public void setInfoKey(String infoKey) {
        this.infoKey = infoKey;
    }

    @Transient
    public synchronized ProcessState getState() {
        return state;
    }

    public synchronized void setState(ProcessState state) {
        this.state = state;
    }

    @Transient
    public synchronized int getPid() {
        return pid;
    }

    public synchronized void setPid(int pid) {
        this.pid = pid;
    }

    @Transient
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public synchronized void setStopped() {
        this.pid = 0;
        this.state = ProcessState.STOPPED;
    }

    @Transient
    public synchronized boolean isRunning() {
        return this.state == ProcessState.RUNNING;
    }

    @Transient
    public synchronized boolean isStarting() {
        return this.state == ProcessState.STARTING;
    }

    @Transient
    public synchronized boolean isStopping() {
        return this.state == ProcessState.STOPPING;
    }
}
