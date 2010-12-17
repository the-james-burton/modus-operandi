package com.modusoperandi.monitor;

public class ProcessMonitorServiceException extends RuntimeException {
    private static final long serialVersionUID = -2385095769760099811L;

    public ProcessMonitorServiceException(String message) {
        super(message);
    }

    public ProcessMonitorServiceException(String message, Throwable t) {
        super(message, t);
    }
}
