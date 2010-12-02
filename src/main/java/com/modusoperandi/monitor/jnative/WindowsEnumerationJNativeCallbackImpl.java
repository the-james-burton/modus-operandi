package com.modusoperandi.monitor.jnative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.misc.basicStructures.HWND;
import org.xvolks.jnative.util.Callback;
import org.xvolks.jnative.util.User32;

import com.modusoperandi.model.Window;

/**
 * 
 * @author Silvio Molinari
 */
public class WindowsEnumerationJNativeCallbackImpl implements Callback {
    
    private static final Log logger = LogFactory.getLog(WindowsEnumerationJNativeCallbackImpl.class);

    private final List<Long> windowHandles;

    public WindowsEnumerationJNativeCallbackImpl() {
        windowHandles = new ArrayList<Long>();
    }
    
    public synchronized Map<String, Window> getWindowDetails(Collection<String> windowNames) {
        Map<String, Window> windows = new HashMap<String, Window>();

        String windowName = null;
        for (Long key : windowHandles) {
            try {
                HWND windowHandle = new HWND(key.intValue());
                windowName = User32.GetWindowText(windowHandle);
                if (windowName != null && windowName.length() > 0 && windowNames.contains(windowName)) {
                    JNativeWindow window = new JNativeWindow(User32
                            .GetWindowThreadProcessId(windowHandle), windowName);
                    window.key = key;
                    windows.put(windowName, window);
                }
            } catch (Exception e) {
                logger.error("Failed to process window handle " + key);
            }
        }

        return windows;
    }

    /**
     * 
     */
    @Override
    public int callback(long[] values) {
        if (values == null) {
            logger.error("callback ret " + 3);
            return 3;
        }
        if (values.length == 2) {
            logger.debug("Handle " + values[0] + ", lParam " + values[1]);
            try {
                if (values[0] > 0) {
                    windowHandles.add(values[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("List size : " + windowHandles.size());
            return 1;
        } else {
            logger.error("Bad number of arguments! 2 expected but " + values.length + " found.");
            return 2;
        }
    }

    /**
     * Provides an address for the native code to call this object back.
     */
    @Override
    public int getCallbackAddress() throws NativeException {
        return JNative.createCallback(2, this);
    }

    public class JNativeWindow extends Window {
        private long key;
        public JNativeWindow(int pid, String name) {
            super(pid, name);
        }
        public long getKey() {
            return key;
        }
    }
}