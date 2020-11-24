package org.egovframe.rte.bat.core.step;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.util.ReflectionUtils;

public class ShellScriptSupport {

	private static String OS = System.getProperty("os.name").toLowerCase();
	private static String OSEncoding = System.getProperty("file.encoding");

	public static int shellCmd(String command, String encoding) throws Exception {
		command = command.trim();
        Runtime runtime = Runtime.getRuntime();
        Process process;

        if (isWindows()) {
            process = runtime.exec(new String[] {"cmd", "/c", command});
        } else {
            process = runtime.exec(command);
        }

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is,encoding);
        BufferedReader br = new BufferedReader(isr);

        String line;
        while((line = br.readLine()) != null) {
            System.out.println(line);
        }

        int exitValue = -1;
        try {
            exitValue = process.waitFor();
        } catch (InterruptedException e) {
            ReflectionUtils.handleReflectionException(e);
        }
        return exitValue;
	}

	// Get the OS encoding
    public static String getOSEncoding() {
        return OSEncoding;
    }

    public static String getShellResultEncoding() {
    	String encoding = "UTF-8";
    	if (isWindows()) {
    		encoding = "MS949"; // DOS Command CharacterSet = MS949
    	}
        return encoding;
    }

	// Check the OS type
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

}
