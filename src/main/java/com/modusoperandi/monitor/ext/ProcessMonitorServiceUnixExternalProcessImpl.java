package com.modusoperandi.monitor.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.modusoperandi.model.Process;
import com.modusoperandi.model.Window;
import com.modusoperandi.monitor.AbstractProcessMonitorService;
import com.modusoperandi.monitor.ProcessMonitorServiceException;

public class ProcessMonitorServiceUnixExternalProcessImpl extends AbstractProcessMonitorService {
    /**
     * Logger for this class.
     */
    private static final Log    logger                     = LogFactory.getLog(ProcessMonitorServiceUnixExternalProcessImpl.class);
    private static final String DEFAULT_KILL_EXEC          = "/bin/kill";
    private static final String DEFAULT_TERM_PARAM_PATTERN = "-SIGTERM %d";
    private final String        killExecutable             = DEFAULT_KILL_EXEC;
    private final String        termParamPattern           = DEFAULT_TERM_PARAM_PATTERN;

    @Override
    protected void killProcessSpecificImpl(Process process, Window window) throws ProcessMonitorServiceException {
        logger.info("Killing process " + process.getId());
        try {
            /* java.lang.Process terminator = */new ProcessBuilder(killExecutable, String.format(termParamPattern, process.getPid())).start();
        } catch (Exception e) {
            throw new ProcessMonitorServiceException(e.getMessage(), e);
        }
    }

    @Override
    protected void refreshSpecificImpl() throws ProcessMonitorServiceException {
        logger.info("Refreshing...");
        BufferedReader br = null;
        try {
            java.lang.Process process = new ProcessBuilder("/bin/ps", "-ef").start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            throw new ProcessMonitorServiceException(e.getMessage(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    protected void startProcessSpecificImpl(Process process) throws ProcessMonitorServiceException {
        logger.info("Starting process " + process.getWindowTitle());
    }
}
