package com.modusoperandi.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Utility to load properties files from a given directory for processes
 * identified by the environment they running on  (such as UAT/BAU etc.)
 * </p>
 * <p>This utility can be set up to keep its information up-to-date by
 * running at configurable intervals.
 * </p>
 * 
 * <p>This class was written to be thread-safe.</p>
 * 
 * @author Silvio Molinari
 */
public class PropertiesLoader extends TimerTask {

    /**
     * Logger object for this class.
     */
    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * The input directory to load properties files from and monitor for
     * changes.
     */
    private File inputDirectory;

    /**
     * The default file extension for the properties files.
     */
    private String fileExtension = "txt";

    /**
     * Local cache containing all the information from the properties
     * directory.
     */
    Map<String, Entry> properties = new HashMap<String, Entry>();

    /**
     * Object used to synchronise thread-sensitive code.
     */
    private Object lockFlag = new Object();

    /**
     * Method used by the scheduling framework to run the utility in
     * background threads.
     */
    @Override
    public void run() {
        try {
            refresh();
        } catch (IOException ioe) {
            logger.error("Could not refresh information", ioe);
        }
    }

    /**
     * Loads files from directory, and keeps information up-to-date.
     * @throws IOException
     */
    private void refresh() throws IOException {
        if (inputDirectory.isDirectory() && inputDirectory.canRead()) {
            logger.debug("Scanning info directory for changes: started.");
            HashMap<String, Entry> tmpMap = new HashMap<String, Entry>();
            File[] files = inputDirectory.listFiles();

            String fileName = null;
            Entry entry = null;
            for (File file : files) {
                fileName = file.getName();
                entry = properties.get(fileName);

                if (entry != null && file.lastModified() <= entry.lastModified) {
                    // entry is still good
                    tmpMap.put(fileName, entry);
                } else {
                    InputStream is = null;
                    try {
                        if (entry == null) {
                            logger.debug("Loading data for " + fileName);
                        } else {
                            logger.info("Refreshing entry for " + fileName);
                        }
                        Properties p = new Properties();
                        is = new FileInputStream(file);
                        p.load(is);
                        Entry e = new Entry();
                        e.lastModified = file.lastModified();
                        e.props = p;
                        tmpMap.put(fileName, e);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
            synchronized (lockFlag) {
                properties = tmpMap;
            }
            logger.debug("Scanning info directory for changes: complete.");
        } else {
            logger.error("The input directory " + inputDirectory + " cannot be read");
        }
    }

    /**
     * Tells you if any data was loaded for the given parameters.
     * 
     * @param environment Which environment to look for eg. <i>BAU</i>.
     * @param server Which server instance to look for eg. <i>gui-ird</i>.
     * @return true if data was in the cache.
     */
    public boolean hasProperties(String environment, String server) {
        String key = makeKey(environment, server);
        synchronized (lockFlag) {
            return properties.containsKey(key);
        }
    }

    /**
     * <p>
     * Retrieves a map containing all the data for the given server/environment combination, or an
     * empty map if no data could be found.
     * </p>
     * 
     * @param environment Which environment to look for eg. <i>BAU</i>.
     * @param server Which server instance to look for eg. <i>gui-ird</i>.
     * @param forceRefresh Whether to force a refresh, or get the data from the cache.
     * @return A map containing the relevant data.
     * @throws IOException If the data cannot be refreshed.
     */
    public Map<String, String> getProperties(String environment, String server, boolean forceRefresh) throws IOException {
        return getProperties(makeKey(environment, server), forceRefresh);
    }


    /**
    * <p>
    * Retrieves a map containing all the data for the given server/environment combination, or an
    * empty map if no data could be found. The data is retrieved from the cache.
    * </p>
    * 
    * @param environment Which environment to look for eg. <i>BAU</i>.
    * @param server Which server instance to look for eg. <i>gui-ird</i>.
    * @return A map containing the relevant data.
    */
    public Map<String, String> getProperties(String environment, String server) {
        Map<String, String> returnProps = null;
        try {
            returnProps = getProperties(makeKey(environment, server), false);
        } catch (IOException ioe) {
            //This will never happen because a refresh is never requested in this method.
            logger.error("Unexpected IOException whilst retrieving cached data", ioe);
        }
        return returnProps;
    }

    /**
     * Abstracts away the way in which the server/environment combination is used to retrieve the
     * data.
     * 
     * @param environment Which environment to look for eg. <i>BAU</i>.
     * @param server Which server instance to look for eg. <i>gui-ird</i>.
     * @return
     */
    private String makeKey(String environment, String server) {
        return new StringBuilder(environment)
            .append('-')
            .append(server)
            .append('.')
            .append(fileExtension)
            .toString();
    }

    /**
     * Gets a copy of the properties for the given key.
     * 
     * @param key
     * @return
     */
    private Map<String, String> getProperties(String key, boolean forceRefresh) throws IOException {
        Map<String, String> returnProps = null;

        synchronized (lockFlag) {
            if (forceRefresh) {
                refresh();
            }
            if (properties.containsKey(key)) {
                Properties cachedProps = properties.get(key).props;
                Iterator<Object> iter = cachedProps.keySet().iterator();
                String propertyName = null;
                returnProps = new HashMap<String, String>();
                while (iter.hasNext()) {
                    propertyName = (String) iter.next();
                    returnProps.put(propertyName, cachedProps
                            .getProperty(propertyName));
                }
            }
        }

        return returnProps;
    }

    /**
     * Setter method to customise the file extension.
     * @param fileExtension
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Count of how many server/environment combinations are in the cache.
     * @return
     */
    public int count() {
        synchronized (lockFlag) {
            return properties.size();
        }
    }

    /**
     * Validating setter for the directory containing the input data.
     * 
     * @param propertiesFileDirectory
     */
    public void setInputDirectory(String propertiesFileDirectory) {
        File inputDir = new File(propertiesFileDirectory);
        if (!inputDir.isDirectory()) {
            logger.error(propertiesFileDirectory + " is not a valid directory");
        }

        this.inputDirectory = inputDir;
    }

    /**
     * Utility class used to associate a properties file with the last modified for the original
     * input file. This allows the process to decide if a given file should be reloaded.
     * @author Silvio Molinari
     */
    private class Entry {
        long lastModified;
        Properties props;
    }
}
