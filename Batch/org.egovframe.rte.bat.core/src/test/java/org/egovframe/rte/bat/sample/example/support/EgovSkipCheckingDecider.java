package org.egovframe.rte.bat.sample.example.support;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * Skip 이 일어날 경우 Exit Status 를 변경하는 클래스
 *
 * @see <pre>
 * == 개정이력(Modification Information) ==
 * 수정일               수정자               수정내용
 * -------    --------     ---------------------------
 * 2012.07.30  배치실행개발팀         최초 생성
 * </pre>
 * @since 2012. 07.30
 */
public class EgovSkipCheckingDecider implements JobExecutionDecider {

    /**
     * skip된 step 상태에 따라 FlowExecution의 Status를 바꾼다.
     */
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (!stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode()) && stepExecution.getSkipCount() > 0) {
            return new FlowExecutionStatus("COMPLETED WITH SKIPS");
        } else {
            return new FlowExecutionStatus(ExitStatus.COMPLETED.getExitCode());
        }
    }

}
