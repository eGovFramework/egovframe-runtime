package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.XmlJob;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = XmlJob.class)
public class EgovXMLFunctionalTests extends EgovAbstractIoSampleTests {

    @Override
    protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
        JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "./target/test-outputs/output.csv")
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        StepSynchronizationManager.close();
        StepSynchronizationManager.register(stepExecution);
    }

    @Override
    protected JobParameters getUniqueJobParameters() {
        return new JobParametersBuilder(super.getUniqueJobParameters())
                .addString("inputFile", "META-INF/spring/input.csv")
                .toJobParameters();
    }

}
