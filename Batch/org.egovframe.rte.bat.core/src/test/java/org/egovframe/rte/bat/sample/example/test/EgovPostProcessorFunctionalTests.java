package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.jobs.PostProcessorJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.test.EgovAbstractIoSampleTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 작업 실행시 후행 처리 설정 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.28  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 06.28
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PostProcessorJob.class)
public class EgovPostProcessorFunctionalTests extends EgovAbstractIoSampleTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /**
     * 배치작업 테스트
     */
    @Override
    @Test
    public void testUpdateCredit() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(getUniqueJobParameters());
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    /**
     * 배치결과를 다시 읽을 때  reader 설정하는 메소드
     */
    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "src/test/resources/META-INF/spring/delimited.csv")
                .addString("outputFile", "./target/test-outputs/delimitedOutput.csv")
                .toJobParameters();
    }

}
