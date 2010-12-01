package org.prototype.monitor;

import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.prototype.web.ConfigEntry;
import org.prototype.web.Process;

/**
 * Defines the operations required by the process monitor. It also specifies the security settings to be applied. The methods {@link #refresh()},
 * {@link #getEnvironment()}, {@link #getMachine()}, and {@link #getProcesses()} don't have any associated security roles because they need to be accessed by
 * unauthenticated users.
 * 
 * @author Silvio Molinari
 */
public interface ProcessMonitorService {
    @RolesAllowed({ UserRoles.ADMIN })
    void killProcess(int pid) throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.USER, UserRoles.ADMIN })
    Process getProcess(String windowTitle) throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.ADMIN })
    void startProcess(String windowTitle) throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.ADMIN })
    void startProcess(Process process) throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.ADMIN })
    void startAllProcesses() throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.ADMIN })
    void stopAllProcesses() throws ProcessMonitorServiceException;

    void refresh() throws ProcessMonitorServiceException;

    @RolesAllowed({ UserRoles.ADMIN })
    void removeAllProcesses();

    @RolesAllowed({ UserRoles.ADMIN })
    void addAllProcesses(Collection<Process> processes);

    @RolesAllowed({ UserRoles.ADMIN })
    void removeAllConfigEntries();

    @RolesAllowed({ UserRoles.ADMIN })
    void addAllConfigEntries(Collection<ConfigEntry> configEntries);

    String getEnvironment();

    String getMachine();

    List<Process> getProcesses();
}
