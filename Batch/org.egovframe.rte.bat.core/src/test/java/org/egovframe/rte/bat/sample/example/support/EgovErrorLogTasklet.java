package org.egovframe.rte.bat.sample.example.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Data 처리하다 Error 발생 시 Skip 에 관한 정보를 DB로 처리하는 클래스
 *
 * @author 배치실행개발팀
 * @see <pre>
 * == 개정이력(Modification Information) ==
 * 수정일               수정자               수정내용
 * ------      --------     ---------------------------
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 * @since 2012. 07.30
 */
public class EgovErrorLogTasklet implements Tasklet, StepExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovErrorLogTasklet.class);

    private JdbcTemplate jdbcTemplate;
    private String jobName;
    private StepExecution stepExecution;
    private String stepName;

    /**
     * Error_Log 테이블에 Skip Error 에 대한 정보를 저장
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (this.stepName == null) {
            this.stepName = "step1"; // 기본값으로 설정
        }
        int skipCount = getSkipCount();
        this.jdbcTemplate.update("insert into ERROR_LOG values (?, ?, '" + skipCount + " records were skipped!')", jobName, stepName);
        return RepeatStatus.FINISHED;
    }

    /**
     * skipCount 정보를 Execution 에서 가져옴
     */
    private int getSkipCount() {
        if (stepExecution == null || stepName == null) {
            return 0;
        }
        for (StepExecution execution : stepExecution.getJobExecution().getStepExecutions()) {
            if (execution.getStepName().equals(stepName)) {
                return (int) execution.getSkipCount();
            }
        }
        return 0;
    }

    /**
     * DataSource 셋팅
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Step 수행전에 처리되는 로직, 해당 stepName 을 JobExecution의 Context에서 제거함
     */
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        this.jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        if (this.jobName != null) {
            this.jobName = this.jobName.trim();
        }
        this.stepName = (String) stepExecution.getJobExecution().getExecutionContext().get("stepName");
        if (this.stepName == null) {
            this.stepName = "step1"; // 기본값으로 step1 설정
        }
        stepExecution.getJobExecution().getExecutionContext().remove("stepName");
    }

    /**
     * Step 단계이후 수행
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

}
