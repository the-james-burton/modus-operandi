package org.prototype.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prototype.monitor.ProcessMonitorServiceException;

public class ProcessLog extends TimerTask {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Log logger = LogFactory.getLog(this.getClass());
    private String pathfilename;
    private String popupName;
    private int bytes = 5000;
    private int lines = 20;
    private long pointer = 0;
    private Queue<String> text = new LinkedList<String>();
    private StringBuilder tail;
    private File logFile;

    private String lastModified = "";
    private Object lock = new Object();
    Random random = new Random();
    
    public List<Line> getLineRange(int firstLine, int lastLine) {
        if (firstLine > lastLine) {
            throw new ProcessMonitorServiceException("First line (" + firstLine + ") cannot be greater than last line (" + lastLine + ")");
        }
        lastLine++;
        List<Line> data = new ArrayList<Line>();
        if (logFile.exists()) {
            BufferedReader bis = null;
            try {
                // Here BufferedInputStream is added for fast reading.
                bis = new BufferedReader(new FileReader(logFile));
                String line = null;
                int count = 0;
                while ((line = bis.readLine()) != null && count < lastLine) {
                    data.add(new Line(count, line));
                    count++;
                }
            } catch (IOException e) {
                throw new ProcessMonitorServiceException("Could not get log content", e);
            } finally {
                try {
                    if (bis != null) bis.close();
                } catch (IOException ioe) {
                    //ignore
                }
            }
            int toIndex = lastLine > data.size() ? data.size() : lastLine;
            data = data.subList(firstLine, toIndex);
        }
        return data;
    }

    public List<Line> getLastLines(int lastLines) {
        if (lastLines <= 0) {
            throw new ProcessMonitorServiceException("Number of lines must be a positive integer (" + lastLines + ")");
        }
        List<Line> results = new ArrayList<Line>();
        LinkedList<Line> data = new LinkedList<Line>();
        if (logFile.exists()) {
            BufferedReader bis = null;
            try {
                // Here BufferedInputStream is added for fast reading.
                bis = new BufferedReader(new FileReader(logFile));
                String line = null;
                int count = 0;
                while ((line = bis.readLine()) != null) {
                    count++;
                    data.add(new Line(count, line));
                    //garbage collect unused lines
                    if (data.size() > lastLines) {
                        data.removeFirst();
                    }
                }
            } catch (IOException e) {
                throw new ProcessMonitorServiceException("Could not get log content", e);
            } finally {
                try {
                    if (bis != null) bis.close();
                } catch (IOException ioe) {
                    //ignore
                }
            }
            //TODO: next two lines unnecessary? Check!!
            int start = lastLines > data.size() ? data.size() - lastLines : 0;
            results = data.subList(start, data.size());
        }
        return results;
    }
    
    public List<Line> filterLines(String pattern) {
        if (pattern == null) {
            throw new ProcessMonitorServiceException("Pattern cannot be null");
        }
        List<Line> results = new ArrayList<Line>();
        LinkedList<Line> data = new LinkedList<Line>();
        if (logFile.exists()) {
            BufferedReader bis = null;
            try {
                // Here BufferedInputStream is added for fast reading.
                bis = new BufferedReader(new FileReader(logFile));
                String line = null;
                int count = 0;
                final String replacement = "<b>" + pattern + "</b>";
                while ((line = bis.readLine()) != null) {
                    count++;
                    if (line.contains(pattern)) {
                        line = line.replaceAll(pattern, replacement);
                        data.add(new Line(count, line));
                    }
                }
            } catch (IOException e) {
                throw new ProcessMonitorServiceException("Could not get log content", e);
            } finally {
                try {
                    if (bis != null) bis.close();
                } catch (IOException ioe) {
                    //ignore
                }
            }
            results = data;
        }
        return results;
    }
    
    public void refreshTail() {
        try {

            if (logFile.exists()) {

                // move pointer if we need to...
                if (pointer == 0) {
                    pointer = logFile.length() < bytes ? 0 : logFile.length() - bytes;
                }

                // if the log has got bigger, read in what we need...
                if (logFile.length() > pointer) {
                    RandomAccessFile log = new RandomAccessFile(logFile, "r");
                    log.seek(pointer);
                    long length = log.length();
                    synchronized (lock) {
                        while (pointer < length) {
                            read(log);
                        }
                    }
                    log.close();
                }

                // log file has shrunk... reread entire tail...
                if (logFile.length() < pointer) {
                    RandomAccessFile log = new RandomAccessFile(logFile, "r");
                    pointer = log.length() < bytes ? 0 : log.length() - bytes;
                    log.seek(pointer);
                    synchronized (lock) {
                        text = new LinkedList<String>();
                        read(log);
                    }
                    log.close();
                }

                // delete old lines if we need to...
                while (text.size() > lines) {
                    text.remove();
                }

                // prepare results...
                synchronized (lock) {
                    tail = new StringBuilder();
                    for (String line : text) {
                        tail.append(encodeHTML(line) + "\n");
                    }
                }

                // make a note of the last modified date...
                lastModified = dateFormat.format(new Date(logFile.lastModified()));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("refreshing log: " + pathfilename);
        refreshTail();
    }

    private void read(RandomAccessFile log) throws IOException {
        byte[] buffer = new byte[bytes];
        log.read(buffer);
        pointer = log.getFilePointer();
        BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer)));
        while (input.ready()) {
            String line = input.readLine();
            if (!"".equals(line.trim())) {
                text.add(line);
            }
        }
    }

    /**
     * used to encode the XMLUnit output to HTML
     * 
     * @param s
     * @return
     */
    private String encodeHTML(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>') {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public String getPathfilename() {
        return pathfilename;
    }

    public String getPopupName() {
        return popupName + random.nextInt(); 
    }

    public void setPathfilename(String pathfilename) {
        this.pathfilename = pathfilename;
        logFile = new File(pathfilename);
        this.popupName = logFile.getName().replace(":", "").replace(".", "").replace("-", "").replace("\\", "");
    }

    public int getBytes() {
        return bytes;
    }

    public int getLines() {
        return lines;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public boolean isFull() {
        return (tail == null ? false : tail.length() > 0);
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getTail() {
        return tail.toString().trim();
    }

    public String getName() {
        return logFile.getAbsolutePath();
    }
    
    public static class Line {
        private int lineNumber;
        private String line;
        private Line(int lineNumber, String line) {
            this.lineNumber = lineNumber;
            this.line = line;
        }
        public int getLineNumber() {
            return lineNumber;
        }
        public String getLine() {
            return line;
        }
    }
}
