package org.prototype.monitor.jna;

import org.prototype.monitor.AbstractProcessMonitorService;
import org.prototype.monitor.ProcessMonitorServiceException;
import org.prototype.monitor.jna.WindowsEnumerationJnaCallbackImpl.JnaWindow;
import org.prototype.web.Process;
import org.prototype.web.ProcessState;
import org.prototype.web.Window;

import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.Shell32;
import com.sun.jna.examples.win32.User32;
import com.sun.jna.examples.win32.W32API;

public class ProcessMonitorServiceJnaImpl extends AbstractProcessMonitorService {

    /**
     * Windows API constant to specify that a process/window being started should be shown.
     */
    private static final int SW_SHOW = 5;
    private static final int WM_CLOSE = 0x0010;
    public static final W32API.DWORD PROCESS_TERMINATE = new W32API.DWORD(0x0001);

    @Override
    protected void killProcessSpecificImpl(Process process, Window window)
            throws ProcessMonitorServiceException {
        final User32 user32 = User32.INSTANCE;
        user32.PostMessage(
                ((JnaWindow)window).getHandle(),
                WM_CLOSE,
                new W32API.WPARAM(0),
                new W32API.LPARAM(0));
        Thread killerThread = new Thread(new KillerThread(process),
                "KillerThread[" + process.getWindowTitle() + "]");
        killerThread.start();
    }

    @Override
    protected void refreshSpecificImpl() throws ProcessMonitorServiceException {
        WindowsEnumerationJnaCallbackImpl callback = new WindowsEnumerationJnaCallbackImpl();
        User32.INSTANCE.EnumWindows(callback, null);
        setWindows(callback.getWindowDetails(getWindowNames()));
    }

    @Override
    protected void startProcessSpecificImpl(Process process)
            throws ProcessMonitorServiceException {
        Shell32 sh = Shell32.INSTANCE;
        W32API.HINSTANCE inst =  sh.ShellExecute(0,
                "open", 
                process.getStartCommand(),
                process.getStartCommandParameters(),
                process.getWorkingDirectory(),
                SW_SHOW);
        if (inst == null) {
            throw new ProcessMonitorServiceException("Failed to start process " + process);
        }
    }

    class KillerThread implements Runnable {
        private final Process process;
        private final long startTime;
        
        KillerThread(Process process) {
            this.process = process;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            try {
                for (;;) {
                    Thread.sleep(2000L);
                    refresh();
                    if (System.currentTimeMillis() - startTime > 30000L && process.getPid() != 0) {
                        getLogger().info("Process " + process.getPid() + 
                                " is not responding in a timely fashion. Forcing shutdown!!");
                        
                        Kernel32 k32 = Kernel32.INSTANCE;
                        W32API.HANDLE handle = k32.OpenProcess(PROCESS_TERMINATE, false, process.getPid());
                        
                        if (handle != null) {
                            getLogger().info("Terminating process " + process.getPid());
                            
                            if (k32.TerminateProcess(handle, 0)) {
                                process.stopped();
                            } else {
                                process.setState(ProcessState.RUNNING);
                                getLogger().warn(String.format(
                                        "Process PID %d could not be stopped. Kernel32.GetLastError(): %d", 
                                        process.getPid(),
                                        k32.GetLastError()));
                            }
                            
                            k32.CloseHandle(handle);
                        }
                        break;
                    }
                    if (process.getPid() == 0) {
                        getLogger().info("Process has exited within the granted time.");
                        process.stopped();
                        break;
                    }
                    
                }
            } catch (Exception e) {
                getLogger().error("Error sending killing pid " + process.getPid());
            }
        }
    }
}
