package org.prototype.monitor.jna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.prototype.web.Window;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Silvio Molinari
 */
public class WindowsEnumerationJnaCallbackImpl implements WNDENUMPROC {
    private static final Log logger = LogFactory.getLog(WindowsEnumerationJnaCallbackImpl.class);
    private final List<HWND> windowHandles;

    public WindowsEnumerationJnaCallbackImpl() {
        windowHandles = new ArrayList<HWND>();
    }

    public synchronized Map<String, Window> getWindowDetails(Collection<String> windowNames) {
        Map<String, Window> windows = new HashMap<String, Window>();
        final User32 user32 = User32.INSTANCE;
        String windowName = null;
        for (HWND handle : windowHandles) {
            try {
                char[] nameBuffer = new char[200];
                user32.GetWindowText(handle, nameBuffer, nameBuffer.length);
                windowName = Native.toString(nameBuffer);
                if (windowName != null && windowName.length() > 0 && windowNames.contains(windowName)) {
                    IntByReference lpdwProcessId = new IntByReference();
                    user32.GetWindowThreadProcessId(handle, lpdwProcessId);
                    int pid = lpdwProcessId.getValue();
                    JnaWindow window = new JnaWindow(pid, windowName);
                    window.handle = handle;
                    windows.put(windowName, window);
                }
            } catch (Exception e) {
                logger.error("Failed to process window handle " + handle);
            }
        }
        return windows;
    }

    @Override
    public boolean callback(HWND hWnd, Pointer data) {
        windowHandles.add(hWnd);
        return true;
    }

    public class JnaWindow extends Window {
        private HWND handle;

        public JnaWindow(int pid, String name) {
            super(pid, name);
        }

        public HWND getHandle() {
            return handle;
        }
    }
}