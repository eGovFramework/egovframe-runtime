package egovframework.rte.bat.core.launch.support;

import java.util.ArrayList;

import org.junit.Test;

/**
 * EgovSchedulerRunner Test 클래스
 * @author 한성곤
 * @since 2013.04.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *  2013.04.01	한성곤	최초 생성 (loop 처리에 대한 테스트)
 *
 * </pre>
 */
public class EgovSchedulerRunnerTest {

	/**
	 * 반복되지 않는 경우 처리
	 *
	 * (30초 대기 후 ApplicationContext close 처리되며, 50초 초과 시 timeout 오류 발생)
	 */
	// CHECKSTYLE:OFF
	@Test(timeout = 50000)
	// CHECKSTYLE:ON
	public void testNoRepeatStart() {
		// CHECKSTYLE:OFF
		EgovSchedulerRunner egovSchedulerRunner = new EgovSchedulerRunner("/META-INF/spring/launch/context-scheduler.xml", "/META-INF/spring/launch/context-job.xml",
				new ArrayList<String>(), 30000);
		// CHECKSTYLE:ON
		egovSchedulerRunner.start();
	}

	/**
	 * 반복처리되는 경우
	 *
	 * (40초 대기 후 EgovSchedulerRunner 실행 thread를 interrupt함으로써 loop 종료 후 AplicationContext close 처리하고, 50초 초과 시 timeout 오류 발생)
	 */
	// CHECKSTYLE:OFF
	@Test(timeout = 50000)
	// CHECKSTYLE:ON
	public void testContinueStart() {
		ThreadRunner runner = new ThreadRunner();

		runner.start();
		// CHECKSTYLE:OFF
		try {

			Thread.sleep(40000);

		} catch (InterruptedException ie) {

		}
		// CHECKSTYLE:ON
		runner.interrupt();
	}

}

class ThreadRunner extends Thread {
	private EgovSchedulerRunner egovSchedulerRunner;

	// CHECKSTYLE:OFF
	@Override
	public void run() {
		egovSchedulerRunner = new EgovSchedulerRunner("/META-INF/spring/launch/context-scheduler.xml", "/META-INF/spring/launch/context-job.xml", new ArrayList<String>(), -30000);

		egovSchedulerRunner.start();
	}
	// CHECKSTYLE:ON
}