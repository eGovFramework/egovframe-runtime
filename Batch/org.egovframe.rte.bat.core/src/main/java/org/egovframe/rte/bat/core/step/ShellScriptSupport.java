/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.bat.core.step;

import org.springframework.util.ReflectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class ShellScriptSupport {

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String OSEncoding = System.getProperty("file.encoding");

    /** 셸 메타문자 패턴 - 명령어 삽입 방지용 (; | & ` $ ( ) \ < > newline 등) */
    private static final Pattern SHELL_METACHAR = Pattern.compile("[;&|`$()\\\\<>\\n\\r]");

    public static int shellCmd(String command, String encoding) throws Exception {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be null or empty");
        }
        command = command.trim();
        if (SHELL_METACHAR.matcher(command).find()) {
            throw new SecurityException(
                "command contains invalid shell metacharacters; possible command injection attempt");
        }
        Runtime runtime = Runtime.getRuntime();
        Process process;

        if (isWindows()) {
            process = runtime.exec(new String[]{"cmd", "/c", command});
        } else {
            process = runtime.exec(command);
        }

        // 2026.02.28 KISA 보안취약점 조치
        try (InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, encoding);
            BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
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
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

}
