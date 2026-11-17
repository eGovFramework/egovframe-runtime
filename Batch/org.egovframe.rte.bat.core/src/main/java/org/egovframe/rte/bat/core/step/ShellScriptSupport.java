/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 셸 명령 실행 유틸 클래스.
 * <p><b>보안 주의:</b> {@link #shellCmd(String, String)}는 셸 메타문자(<code>; | &amp; ` $ ( ) \ &lt; &gt;</code>, 개행)만
 * 차단하므로 신뢰할 수 없는 값을 command로 전달하지 않는다. 실행 프로그램 제한은 {@link #setAllowedExecutables(Set)}로 설정한다.</p>
 */
public class ShellScriptSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellScriptSupport.class);

    private static String OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private static String OSEncoding = System.getProperty("file.encoding");

    /** 명령어 삽입 방지용 셸 메타문자 패턴 */
    private static final Pattern SHELL_METACHAR = Pattern.compile("[;&|`$()\\\\<>\\n\\r]");

    /**
     * 실행 허용 프로그램(경로 제외 파일명) 목록. null이면 제한 없음(기본값).
     */
    private static volatile Set<String> allowedExecutables = null;

    /**
     * 실행 허용 프로그램 목록을 설정한다. 설정 시 command의 첫 토큰 파일명이 목록에 없으면
     * {@link SecurityException}을 던지며, null 또는 빈 집합이면 제한을 해제한다.
     *
     * @param executableNames 허용할 실행 파일명 집합 (예: "sh", "python3", "myBatchTool.exe")
     */
    public static void setAllowedExecutables(Set<String> executableNames) {
        allowedExecutables = (executableNames == null || executableNames.isEmpty())
                ? null
                : Collections.unmodifiableSet(new HashSet<>(executableNames));
    }

    /**
     * 허용 목록이 설정된 경우에만 command의 첫 토큰(실행 파일)을 검사한다.
     */
    private static void assertAllowedExecutable(String command) {
        Set<String> allowed = allowedExecutables;
        if (allowed == null) {
            return;
        }
        int sp = command.indexOf(' ');
        String executablePart = (sp == -1) ? command : command.substring(0, sp);
        String executableName = Paths.get(executablePart).getFileName().toString();
        if (!allowed.contains(executableName)) {
            throw new SecurityException(
                "command executable '" + executableName + "' is not in the allowed executable list");
        }
    }

    public static int shellCmd(String command, String encoding) throws Exception {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be null or empty");
        }
        command = command.trim();
        if (SHELL_METACHAR.matcher(command).find()) {
            throw new SecurityException(
                "command contains invalid shell metacharacters; possible command injection attempt");
        }
        assertAllowedExecutable(command);
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
                LOGGER.info("{}", line);
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
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") >= 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

}
