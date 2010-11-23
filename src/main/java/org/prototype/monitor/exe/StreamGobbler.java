package org.prototype.monitor.exe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Deprecated
public class StreamGobbler extends Thread {

    private InputStream is;

    private StringBuffer output = new StringBuffer();

    StreamGobbler(InputStream is) {
        this.is = is;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
            }
            br.close();
            isr.close();
            is.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getOutput() {
        return output.toString();
    }
    
}
