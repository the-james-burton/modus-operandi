package com.modusoperandi.monitor.jnative;

import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.misc.SHELLEXECUTEINFO;
import org.xvolks.jnative.misc.basicStructures.HANDLE;
import org.xvolks.jnative.misc.basicStructures.HWND;
import org.xvolks.jnative.misc.basicStructures.LPARAM;
import org.xvolks.jnative.misc.basicStructures.UINT;
import org.xvolks.jnative.misc.basicStructures.WPARAM;
import org.xvolks.jnative.util.Kernel32;
import org.xvolks.jnative.util.Shell32;
import org.xvolks.jnative.util.User32;
import org.xvolks.jnative.util.constants.winuser.WM;

import com.modusoperandi.model.Process;
import com.modusoperandi.model.Window;
import com.modusoperandi.monitor.AbstractProcessMonitorService;
import com.modusoperandi.monitor.ProcessMonitorServiceException;
import com.modusoperandi.monitor.jnative.WindowsEnumerationJNativeCallbackImpl.JNativeWindow;

/**
 * Implements the process services via a native proxy layer hooking directly into the User32, Shell32 and Kernel32.
 * 
 * @author Silvio Molinari
 */
public class ProcessMonitorServiceJNativeImpl extends AbstractProcessMonitorService {
    /**
     * Windows API constant to specify that a process/window being started should be shown.
     */
    private static final int SW_SHOW = 5;

    public ProcessMonitorServiceJNativeImpl() throws ProcessMonitorServiceException {
        super();
    }

    /**
     * Kills a process with the given id if this id is valid.
     * 
     * @param pid
     *            The process to kill
     */
    @Override
    public synchronized void killProcessSpecificImpl(Process process, Window window) throws ProcessMonitorServiceException {
        Thread stopperThread = new Thread(new StopperThread(window), "StopperThread[" + window.getName() + "]");
        Thread killerThread = new Thread(new KillerThread(process), "KillerThread[" + process.getWindowTitle() + "]");
        stopperThread.start();
        killerThread.start();
    }

    @Override
    public synchronized void refreshSpecificImpl() throws ProcessMonitorServiceException {
        try {
            WindowsEnumerationJNativeCallbackImpl callback = new WindowsEnumerationJNativeCallbackImpl();
            User32.EnumWindows(callback, 0);
            setWindows(callback.getWindowDetails(getWindowNames()));
        } catch (Exception e) {
            throw new ProcessMonitorServiceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void startProcessSpecificImpl(Process process) throws ProcessMonitorServiceException {
        try {
            SHELLEXECUTEINFO lpExecInfo = new SHELLEXECUTEINFO();
            lpExecInfo.lpVerb = "open";
            lpExecInfo.lpFile = process.getStartCommand();
            lpExecInfo.lpDirectory = process.getWorkingDirectory();
            if (process.getStartCommandParameters() != null) {
                lpExecInfo.lpParameters = process.getStartCommandParameters();
            }
            lpExecInfo.nShow = SW_SHOW;
            boolean worked = Shell32.ShellExecuteEx(lpExecInfo);
            if (worked == false) {
                throw new ProcessMonitorServiceException("Failed to start process " + process);
            }
        } catch (IllegalAccessException iae) {
            throw new ProcessMonitorServiceException(iae.getMessage(), iae);
        } catch (NativeException ne) {
            throw new ProcessMonitorServiceException(ne.getMessage(), ne);
        }
    }

    class StopperThread implements Runnable {
        private final Window window;

        StopperThread(Window window) {
            this.window = window;
        }

        @Override
        public void run() {
            try {
                getLogger().info("Sending WM_CLOSE to pid " + window.getPid());
                User32.SendMessage(new HWND((int) ((JNativeWindow) window).getKey()), new UINT(WM.WM_CLOSE.getValue()), new WPARAM(0), new LPARAM(0));
            } catch (Throwable e) {
                getLogger().error("Error sending WM_CLOSE to pid " + window.getPid());
            }
        }
    }

    class KillerThread implements Runnable {
        private final Process process;
        private final long    startTime;

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
                        getLogger().info("Process " + process.getPid() + " is not responding in a timely fashon. Forcing shutdown!!");
                        HANDLE llHandle = Kernel32.OpenProcess(Kernel32.PROCESS_TERMINATE, false, process.getPid());
                        if (llHandle != null) {
                            if (Kernel32.TerminateProcess(llHandle, 1)) {
                                process.setStopped();
                            }
                        }
                        break;
                    }
                    if (process.getPid() == 0) {
                        getLogger().info("Process has exited within the granted time.");
                        process.setStopped();
                        break;
                    }
                }
            } catch (Throwable e) {
                getLogger().error("Error sending killing pid " + process.getPid());
            }
        }
    }
}
