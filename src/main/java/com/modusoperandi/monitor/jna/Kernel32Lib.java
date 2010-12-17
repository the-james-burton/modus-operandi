package com.modusoperandi.monitor.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;

/**
 * This interface can be used to extend the functionality from the <code>com.sun.jna.platform</code> package, when such functionality has not been implemented
 * by the JNA contributors.
 * 
 * @author Silvio Molinari
 */
public interface Kernel32Lib extends com.sun.jna.platform.win32.Kernel32 {
    Kernel32Lib KERNEL32 = (Kernel32Lib) Native.loadLibrary("kernel32", Kernel32Lib.class, W32APIOptions.UNICODE_OPTIONS);

    boolean TerminateProcess(HANDLE hObject, int exitCode);
}
