package com.modusoperandi.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.sun.jna.Platform;

/**
 * Factory used by Spring to produce the correct service implementation based on the host operating system. <br/>
 * This class should not be referenced directly.
 * 
 * @author Silvio Molinari
 */
public class ProcessMonitorServiceFactoryImpl implements ProcessMonitorServiceFactory, BeanFactoryAware {
    /**
     * Logger for this class.
     */
    private static final Log logger = LogFactory.getLog(ProcessMonitorServiceFactoryImpl.class);
    private BeanFactory      beanFactory;

    @Override
    public ProcessMonitorService getService() {
        logger.info(String.format("Getting service for the %s operating system.", System.getProperty("os.name")));
        ProcessMonitorService processMonitorService = null;
        if (Platform.isWindows()) {
            processMonitorService = beanFactory.getBean("win32ServicesImpl", ProcessMonitorService.class);
        } else if (Platform.isLinux()) {
            processMonitorService = beanFactory.getBean("linuxServicesImpl", ProcessMonitorService.class);
        } else {
            throw new RuntimeException(String.format("Unimplemented version for OS: %s", System.getProperty("os.name")));
        }
        return processMonitorService;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
