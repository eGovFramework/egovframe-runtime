package org.egovframe.rte.fdl.filehandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovFiles 경로 봉쇄의 <b>공격 입력</b> 검증 — 기능 테스트({@link EgovFilesTest})와 별도로,
 * 경로 조작(CWE-22)에 실제로 쓰이는 표기 변형을 한 곳에 모아 회귀를 막는다.
 *
 * <p>판정 기준은 하나다: 기준 디렉터리 <b>밖으로 확정되는 경로가 하나라도 나오는가.</b>
 * 리터럴로 남아 기준 안에 놓이는 입력(URL 인코딩·전각 점)은 탈출이 아니므로 허용이 맞다.</p>
 */
public class EgovFilesSecurityTest {

    private static final String BS = String.valueOf((char) 92);

    @TempDir
    Path tempDir;

    private Path base() throws Exception {
        return Files.createDirectories(tempDir.resolve("base"));
    }

    // ------------------------------------------------------------ 모든 OS

    @Test
    public void 상위_이동_변형은_전부_차단된다() throws Exception {
        Path base = base();
        List<String> attacks = List.of(
                "../pwn", "../../pwn", "sub/../../pwn", "a/b/../../../pwn", "..");
        for (String attack : attacks) {
            assertThrows(IllegalArgumentException.class,
                    () -> EgovFiles.resolveSecurely(base, attack), "차단돼야 한다: " + attack);
            assertFalse(EgovFiles.tryResolveSecurely(base, attack).isPresent(), "판정형도 거부: " + attack);
        }
    }

    @Test
    public void 인코딩_변형은_해석하지_않고_리터럴_이름으로_기준_안에_둔다() throws Exception {
        Path base = base();
        // 퍼센트 인코딩·전각 점은 파일시스템에 그런 이름의 항목일 뿐이다 — 디코딩해 탈출로 바꾸지 않는다
        for (String literal : List.of("%2e%2e/pwn", "%2e%2e%2fpwn", "．．/pwn", "....//pwn")) {
            Path resolved = EgovFiles.resolveSecurely(base, literal);
            assertTrue(resolved.startsWith(base), "기준 안이어야 한다: " + literal + " -> " + resolved);
        }
    }

    @Test
    public void 널바이트와_빈값은_거부된다() throws Exception {
        Path base = base();
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "ok.txt" + (char) 0));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "ok" + (char) 0 + "/../pwn"));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, ""));
        assertFalse(EgovFiles.tryResolveSecurely(base, "ok.txt" + (char) 0).isPresent());
        assertFalse(EgovFiles.tryResolveSecurely(base, "").isPresent());
    }

    @Test
    public void POSIX_절대_경로는_기준_밖으로_차단된다() throws Exception {
        Path base = base();
        // Windows 에서는 루트 상대(현재 드라이브 루트)로 해석돼 역시 기준 밖이다
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "/etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, "//server/share/pwn"));
    }

    // ------------------------------------------------------------ Windows

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void 윈도우_드라이브_UNC_루트상대_경로는_차단된다() throws Exception {
        Path base = base();
        for (String attack : List.of(
                "C:" + BS + "Windows" + BS + "pwn",
                BS + "pwn",
                BS + BS + "server" + BS + "share" + BS + "pwn",
                ".." + BS + ".." + BS + "pwn")) {
            assertThrows(IllegalArgumentException.class,
                    () -> EgovFiles.resolveSecurely(base, attack), "차단돼야 한다: " + attack);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void 윈도우_드라이브_상대_경로는_기준_드라이브_기준으로_붙어_기준_안에_남는다() throws Exception {
        Path base = base();
        // "C:pwn" 은 드라이브 상대 표기 — Java 가 기준 경로의 드라이브에 이어 붙이므로 탈출이 아니다
        Path resolved = EgovFiles.resolveSecurely(base, base.getRoot().toString().substring(0, 2) + "pwn");
        assertTrue(resolved.startsWith(base), resolved.toString());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void 윈도우_ADS와_끝_공백은_경로로_해석되지_않아_거부된다() throws Exception {
        Path base = base();
        // 대체 데이터 스트림 구분자(:)와 끝 공백은 Windows 경로 파서가 InvalidPathException 으로 거부한다
        assertFalse(EgovFiles.tryResolveSecurely(base, "ok.txt:hidden").isPresent());
        assertFalse(EgovFiles.tryResolveSecurely(base, "ok.txt::$DATA").isPresent());
        assertFalse(EgovFiles.tryResolveSecurely(base, "ok.txt ").isPresent());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void 윈도우_예약_장치명은_거부된다() throws Exception {
        // 예약 장치명은 파일이 아니라 장치로 열린다 — NUL 은 내용이 사라지고 CON 은 콘솔에 붙어 블로킹될 수 있다.
        // 확장자가 붙어도(NUL.txt) 장치이며, 중간 구성요소로 와도 마찬가지다
        Path base = base();
        for (String name : List.of("NUL", "nul", "NUL.txt", "CON", "PRN.log", "AUX", "COM1", "com9.dat",
                "LPT1", "sub/NUL/x.txt")) {
            assertThrows(IllegalArgumentException.class, () -> EgovFiles.resolveSecurely(base, name), name);
            assertTrue(EgovFiles.tryResolveSecurely(base, name).isEmpty(), name);
        }
        // 예약명을 접두로 가질 뿐인 정상 이름은 통과한다
        for (String ok : List.of("console.txt", "nullable.txt", "com10.txt", "aux2.txt", "lpt0.txt", "prn_report.txt")) {
            assertDoesNotThrow(() -> EgovFiles.resolveSecurely(base, ok), ok);
        }
    }

    // ------------------------------------------------------------ 확장자 추출

    @Test
    public void 확장자_추출은_경로를_먼저_떼고_마지막_점만_본다() {
        assertTrue(EgovFiles.getExtension("../../etc/shell.jsp").equals("jsp"));
        assertTrue(EgovFiles.getExtension("dir" + BS + "shell.jsp").equals("jsp"));
        assertTrue(EgovFiles.getExtension("shell.jsp.").isEmpty(), "점으로 끝나면 확장자 없음");
        assertTrue(EgovFiles.getExtension("shell．jsp").isEmpty(), "전각 점은 구분자가 아니다");
        assertTrue(EgovFiles.getExtension(null).isEmpty());
        assertTrue(EgovFiles.getBaseName("../../etc/shell.jsp").equals("shell"));
    }
}
