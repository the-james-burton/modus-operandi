package com.modusoperandi.monitor.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.win32.W32APIOptions;

/**
 * This interface can be used to extend the functionality from the <code>com.sun.jna.platform</code> package, when such functionality has not been implemented
 * by the JNA contributors.
 * 
 * @author Silvio Molinari
 */
public interface Shell32 extends com.sun.jna.platform.win32.Shell32 {
    Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class, W32APIOptions.UNICODE_OPTIONS);

    HINSTANCE ShellExecute(int i, String lpVerb, String lpFile, String lpParameters, String lpDirectory, int nShow);
}
