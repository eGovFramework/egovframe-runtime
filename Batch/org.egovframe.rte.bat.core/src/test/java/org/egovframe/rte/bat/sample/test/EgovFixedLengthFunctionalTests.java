package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.FixedLengthIoJob;
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
 * FilxedLength 방식으로 데이터처리를 수행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FixedLengthIoJob.class)
public class EgovFixedLengthFunctionalTests extends EgovAbstractIoSampleTests {

    /**
     * 배치결과를 다시 읽을 때  reader 설정하는 메소드
     */
    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
        JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "./target/test-outputs/fixedLengthOutput.txt")
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        StepSynchronizationManager.close();
        StepSynchronizationManager.register(stepExecution);
    }

    /**
     * 잡파라미터를 설정하기 위한 메소드
     */
    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "META-INF/spring/fixedLength.txt")
                .addString("outputFile", "./target/test-outputs/fixedLengthOutput.txt")
                .toJobParameters();
    }

}
