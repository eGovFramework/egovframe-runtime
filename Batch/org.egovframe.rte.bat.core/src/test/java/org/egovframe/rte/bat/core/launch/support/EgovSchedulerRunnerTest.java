package org.egovframe.rte.bat.core.launch.support;

import org.egovframe.rte.bat.config.launch.LaunchScheduleJCConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * EgovSchedulerRunner Test 클래스
 *
 * @author 한성곤
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *  2013.04.01	한성곤	최초 생성 (loop 처리에 대한 테스트)
 *
 * </pre>
 * @since 2013.04.01
 */
public class EgovSchedulerRunnerTest {

    /**
     * 반복되지 않는 경우 처리
     * <p>
     * (2초 대기 후 ApplicationContext close 처리되며, 15초 초과 시 timeout 오류 발생)
     */
    @Test
    public void testNoRepeatStart() {
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            EgovSchedulerJCRunner egovSchedulerJCRunner = new EgovSchedulerJCRunner(
                    new Class<?>[]{LaunchScheduleJCConfig.class},
                    2000
            );
            egovSchedulerJCRunner.start();
        });
    }

    /**
     * 반복처리되는 경우
     * <p>
     * (3초 대기 후 EgovSchedulerRunner 실행 thread를 interrupt함으로써
     * loop 종료 후 ApplicationContext close 처리하고, 15초 초과 시 timeout 오류 발생)
     */
    @Test
    public void testContinueStart() {
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            ThreadRunner runner = new ThreadRunner();
            runner.start();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            runner.interrupt();
        });
    }

}

class ThreadRunner extends Thread {
    @Override
    public void run() {
        EgovSchedulerJCRunner egovSchedulerJCRunner = new EgovSchedulerJCRunner(
                new Class<?>[]{LaunchScheduleJCConfig.class},
                -2000
        );
        egovSchedulerJCRunner.start();
    }
}
