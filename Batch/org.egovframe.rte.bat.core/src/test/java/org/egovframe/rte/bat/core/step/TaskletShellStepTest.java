package org.egovframe.rte.bat.core.step;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TaskletShellStep의 셸 스크립트 줄 분리 테스트
 */
public class TaskletShellStepTest {

    private TaskletShellStep step(String shellScript) {
        TaskletShellStep step = new TaskletShellStep();
        step.setShellScript(shellScript);
        step.setEncoding("UTF-8");
        return step;
    }

    @Test
    @DisplayName("물음표가 든 한 줄 명령은 한 번에 실행된다")
    public void executeSingleLineContainingQuestionMark() throws Exception {
        assertEquals(RepeatStatus.FINISHED, step("echo a?b").execute(null, null));
    }

    @Test
    @DisplayName("줄바꿈으로 구분된 여러 줄은 줄마다 실행된다")
    public void executeMultipleLines() throws Exception {
        assertEquals(RepeatStatus.FINISHED, step("echo first\necho second").execute(null, null));
    }

    @Test
    @DisplayName("스크립트가 개행으로 시작해도 빈 명령을 실행하지 않는다")
    public void executeScriptStartingWithNewline() throws Exception {
        assertEquals(RepeatStatus.FINISHED, step("\necho hello").execute(null, null));
    }

    @Test
    @DisplayName("공백만 있는 줄은 건너뛴다")
    public void executeScriptWithBlankLine() throws Exception {
        assertEquals(RepeatStatus.FINISHED, step("echo first\n   \necho second").execute(null, null));
    }
}
