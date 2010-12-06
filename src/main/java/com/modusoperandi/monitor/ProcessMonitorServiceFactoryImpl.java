package com.modusoperandi.monitor;

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
    private BeanFactory beanFactory;

    @Override
    public ProcessMonitorService getService() {
        ProcessMonitorService processMonitorService = null;
        if (Platform.isWindows()) {
            processMonitorService = beanFactory.getBean("win32ServicesImpl", ProcessMonitorService.class);
        } else if (Platform.isLinux()) {
            throw new RuntimeException("Unimplemented linux version");
        }
        return processMonitorService;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
