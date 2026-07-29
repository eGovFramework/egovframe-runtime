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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p><b>보안 주의:</b> {@link #shellCmd(String, String)}는 셸 메타문자(<code>; | &amp; ` $ ( ) \ &lt; &gt;</code>
 * 및 개행)만 차단할 뿐, 공백으로 구분된 "단일 명령 + 인자" 자체의 실행은 막지 않는다. 즉 메타문자가
 * 전혀 없어도 <code>command</code> 문자열이 임의의 실행파일/인자로 채워지면 그대로 실행된다.
 * 사용자 요청 파라미터, 관리 UI 입력 등 <b>신뢰할 수 없는 값을 command(또는 그 일부)로 전달하지 말 것</b>.
 * 실행 가능한 프로그램을 제한해야 하는 배치 Job이라면 {@link #setAllowedExecutables(Set)}으로
 * 허용 목록을 설정하라(기본값은 null=제한 없음, 기존 동작과 동일).</p>
 */
public class ShellScriptSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellScriptSupport.class);

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String OSEncoding = System.getProperty("file.encoding");

    /** 셸 메타문자 패턴 - 명령어 삽입 방지용 (; | & ` $ ( ) \ < > newline 등) */
    private static final Pattern SHELL_METACHAR = Pattern.compile("[;&|`$()\\\\<>\\n\\r]");

    /**
     * 실행 허용 프로그램(실행파일명, 경로 제외) 화이트리스트.
     * null이면 제한 없음(기본값, 기존 동작과 동일 — 하위 호환).
     */
    private static volatile Set<String> allowedExecutables = null;

    /**
     * command로 신뢰할 수 없는 입력이 흘러들 가능성이 있는 배치 Job에서, 실행 가능한 프로그램을
     * 제한하려면 이 메서드로 허용 목록을 설정한다. 설정 시 {@link #shellCmd(String, String)}는
     * command의 첫 토큰(실행 파일)의 파일명이 이 목록에 없으면 {@link SecurityException}을 던진다.
     * null 또는 빈 집합을 넘기면 제한이 해제된다(기본값).
     *
     * @param executableNames 허용할 실행 파일명 집합 (예: "sh", "python3", "myBatchTool.exe")
     */
    public static void setAllowedExecutables(Set<String> executableNames) {
        allowedExecutables = (executableNames == null || executableNames.isEmpty())
                ? null
                : Collections.unmodifiableSet(new HashSet<>(executableNames));
    }

    /**
     * 화이트리스트가 설정된 경우에만 command의 첫 토큰(실행 파일)을 검사한다.
     * 화이트리스트 미설정 시(기본값) 아무 제한도 하지 않는다 — 기존 동작과 동일.
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
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

}
