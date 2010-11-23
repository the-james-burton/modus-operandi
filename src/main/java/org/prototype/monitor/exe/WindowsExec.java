package org.prototype.monitor.exe;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Deprecated
public class WindowsExec {

    private static final Log logger = LogFactory.getLog(WindowsExec.class);

    public static synchronized void execInDir(String command, File dir) {
        try {
            Runtime rt = Runtime.getRuntime();
            
            logger.info("executing: " + command + " in directory: " + dir.getAbsolutePath());
            Process proc = rt.exec(command, null, dir);
            proc.getOutputStream().close();
            logger.info("executed: " + command + " in directory: " + dir.getAbsolutePath());
        } catch (Throwable t) {
            logger.error("Could not execute command: " + command + " in " + dir.getAbsolutePath(), t);
        }
    }

    public static synchronized void exec(String command) {
        try {
            Runtime rt = Runtime.getRuntime();
            
            logger.info("executing: " + command);
            Process proc = rt.exec(command);
            proc.getOutputStream().close();
            logger.info("executed : " + command);
        } catch (Throwable t) {
            logger.error("Could not execute command: " + command, t);
        }
    }

    public static synchronized String execAndWait(String command) {
        String output = "";

        try {
            Runtime rt = Runtime.getRuntime();
            
            logger.info("executing: " + command);
            Process proc = rt.exec(command);
            proc.getOutputStream().close();

            // any error message?
            StreamGobbler error = new StreamGobbler(proc.getErrorStream());

            // any output?
            StreamGobbler input = new StreamGobbler(proc.getInputStream());

            // kick them off
            error.start();
            input.start();

            // any error???
            int exitVal = proc.waitFor();
            logger.info("executed : " + command + " with exit status of " + exitVal);
            output = input.getOutput() + error.getOutput();
            
        } catch (Throwable t) {
            logger.error("Could not execute command: " + command, t);
        }
        return output;
    }


}
