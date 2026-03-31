package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCreditIncreaseProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IoSample 실행을 위한 기본 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
public abstract class EgovAbstractIoSampleTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ItemReader<CustomerCredit> reader;

    /**
     * 배치작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        open(reader);
        List<CustomerCredit> inputs = getCredits(reader);
        close(reader);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(getUniqueJobParameters());

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        pointReaderToOutput(reader);
        open(reader);
        List<CustomerCredit> outputs = getCredits(reader);
        close(reader);

        assertEquals(inputs.size(), outputs.size());
        int itemCount = inputs.size();
        assertTrue(itemCount > 0);

        for (int i = 0; i < itemCount; i++) {
            assertEquals(inputs.get(i).getCredit().add(CustomerCreditIncreaseProcessor.FIXED_AMOUNT).intValue(), outputs.get(i).getCredit().intValue());
        }
    }

    /**
     * 잡파라미터를 얻기 위한 메소드
     */
    protected JobParameters getUniqueJobParameters() {
        return jobLauncherTestUtils.getUniqueJobParameters();
    }

    /**
     * file-to-file 배치작업일 경우만 필요한 설정으로  배치결과를 다시 읽을 때  reader 설정하는 메소드
     */
    protected abstract void pointReaderToOutput(ItemReader<CustomerCredit> reader);

    /**
     * 배치작업의 결과값을 list로 만드는 메소드
     */
    private List<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
        CustomerCredit credit;
        List<CustomerCredit> result = new ArrayList<>();
        while ((credit = reader.read()) != null) {
            result.add(credit);
        }
        return result;
    }

    /**
     * reader를 open하는 메소드
     */
    private void open(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).open(new ExecutionContext());
        }
    }

    /**
     * reader를 close하는 메소드
     */
    private void close(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).close();
        }
    }

    /**
     * stepExecution를 얻는 메소드
     */
    protected StepExecution getStepExecution() {
        return MetaDataInstanceFactory.createStepExecution(getUniqueJobParameters());
    }

}
