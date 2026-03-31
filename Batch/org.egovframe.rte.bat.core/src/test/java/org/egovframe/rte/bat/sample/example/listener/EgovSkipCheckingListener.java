package org.egovframe.rte.bat.sample.example.listener;

import org.egovframe.rte.bat.sample.domain.trade.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInWrite;

/**
 * Skip 된 데이터를 처리하는 Listener 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * 	개정이력(Modification Information)
 *
 * 	수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012.06.27
 */
public class EgovSkipCheckingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSkipCheckingListener.class);

    private static int processSkips;

    /**
     * ProcessSkips Getter
     */
    public static int getProcessSkips() {
        return processSkips;
    }

    /**
     * ProcessSkips 리셋
     */
    public static void resetProcessSkips() {
        processSkips = 0;
    }

    /**
     * 잡수행이 Fail이고, Skip이 일어났을 경우, ExitStatus 상태를 COMPLETED WITH SKIPS 으로 변경
     */
    @AfterStep
    public ExitStatus checkForSkips(StepExecution stepExecution) {
        if (!stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode()) && stepExecution.getSkipCount() > 0) {
            return new ExitStatus("COMPLETED WITH SKIPS");
        } else {
            return null;
        }
    }

    /**
     * Write 하다 Skip 이 발생할 경우 수행
     */
    @OnSkipInWrite
    public void skipWrite(Trade trade, Throwable t) {
        LOGGER.debug("Skipped writing " + trade);
    }

    /**
     * Process 하다 Skip 이 발생할 경우 수행
     */
    @OnSkipInProcess
    public void skipProcess(Trade trade, Throwable t) {
        LOGGER.debug("Skipped processing " + trade);
        processSkips++;
    }

    /**
     * Step 수행 전에 stepName을 Context에 put 함
     */
    @BeforeStep
    public void saveStepName(StepExecution stepExecution) {
        stepExecution.getExecutionContext().put("stepName", stepExecution.getStepName());
    }

}
