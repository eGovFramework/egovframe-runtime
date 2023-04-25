package org.egovframe.brte.sample.testcase.test;

import org.egovframe.brte.sample.common.domain.trade.CustomerCredit;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/org/egovframe/batch/jobs/xmlJob.xml")
public class EgovXMLFunctionalTests extends EgovAbstractIoSampleTests {

	@Override
	protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
		JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters()).addString("inputFile", "file:./target/test-outputs/output.xml").toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepSynchronizationManager.close();
		StepSynchronizationManager.register(stepExecution);

	}

	@Override
	protected JobParameters getUniqueJobParameters() {

		return new JobParametersBuilder(super.getUniqueJobParameters()).addString("inputFile", "/org/egovframe/data/input/input.xml").toJobParameters();
	}

}
