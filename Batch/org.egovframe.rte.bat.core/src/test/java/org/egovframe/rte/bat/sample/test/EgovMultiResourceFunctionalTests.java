package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.MultiResourceIoJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 여러 파일로 배치작업을 수행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MultiResourceIoJob.class)
public class EgovMultiResourceFunctionalTests extends EgovAbstractIoSampleTests {

    /**
     * 배치결과를 다시 읽을 때 Reader 설정하는 메소드
     */
    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
        JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("input.file.path", "./target/test-outputs/multiResourceOutput.csv.*")
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        StepSynchronizationManager.close();
        StepSynchronizationManager.register(stepExecution);
    }

    /**
     * Job 파라미터를 설정하기 위한 메소드
     */
    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("input.file.path", "META-INF/spring/delimited*.csv")
                .addString("output.file.path", "./target/test-outputs/multiResourceOutput.csv")
                .toJobParameters();
    }

}
