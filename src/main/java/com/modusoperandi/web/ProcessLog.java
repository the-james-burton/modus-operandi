package com.modusoperandi.web;

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
import java.util.TimerTask;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.modusoperandi.monitor.ProcessMonitorServiceException;

@javax.persistence.Entity
@Table(name = "ProcessLog")
public class ProcessLog extends TimerTask {
    private static final DateFormat dateFormat              = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Log               logger                  = LogFactory.getLog(this.getClass());
    private final int               DEFAULT_LOG_SIZE        = 5000;
    private final int               DEFAULT_NUMBER_OF_LINES = 20;
    private Long                    id;
    private String                  pathfilename;
    private int                     bytes                   = DEFAULT_LOG_SIZE;
    private int                     lines                   = DEFAULT_NUMBER_OF_LINES;
    private long                    pointer                 = 0L;
    private Queue<String>           text                    = new LinkedList<String>();
    private StringBuilder           tail;
    private File                    logFile;
    private String                  lastModified            = "";
    private final Object            lock                    = new Object();

    @Id
    @GeneratedValue
    @Column(name = "Id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "FileName", length = 300)
    public String getPathfilename() {
        return pathfilename;
    }

    public void setPathfilename(String pathfilename) {
        this.pathfilename = pathfilename;
        logFile = new File(pathfilename);
    }

    @Column(name = "Bytes")
    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    @Column(name = "Lines")
    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    @Transient
    public boolean isFull() {
        return (tail == null ? false : tail.length() > 0);
    }

    @Transient
    public String getLastModified() {
        return lastModified;
    }

    @Transient
    public String getTail() {
        return tail.toString().trim();
    }

    public List<Line> findLineRange(int firstLine, int lastLine) {
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
                    if (bis != null)
                        bis.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
            int toIndex = lastLine > data.size() ? data.size() : lastLine;
            data = data.subList(firstLine, toIndex);
        }
        return data;
    }

    public List<Line> findLastLines(int lastLines) {
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
                    // garbage collect unused lines
                    if (data.size() > lastLines) {
                        data.removeFirst();
                    }
                }
            } catch (IOException e) {
                throw new ProcessMonitorServiceException("Could not get log content", e);
            } finally {
                try {
                    if (bis != null)
                        bis.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
            // TODO: next two lines unnecessary? Check!!
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
                    if (bis != null)
                        bis.close();
                } catch (IOException ioe) {
                    // ignore
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

    public static class Line {
        private final int    lineNumber;
        private final String line;

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
