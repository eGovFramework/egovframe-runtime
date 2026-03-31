package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.jobs.PartitionFileJob;
import org.egovframe.rte.bat.sample.domain.trade.CustomerCredit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 다수파일을 파티셔닝으로 처리하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 * @since 2012. 07.30
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PartitionFileJob.class)
public class EgovPartitionFileFunctionalTests implements ApplicationContextAware {

    @Autowired
    private ItemReader<CustomerCredit> inputReader;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private ApplicationContext applicationContext;


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 배치작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        assertTrue(applicationContext.containsBeanDefinition("outputTestReader"), "outputTestReader");

        //Job 의 output 자료들을 얻음
        open(inputReader);
        List<CustomerCredit> inputs = new ArrayList<CustomerCredit>(getCredits(inputReader));
        close(inputReader);

        //Job 수행
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        //Job 의 output 자료들을 얻음
        @SuppressWarnings("unchecked")
        ItemReader<CustomerCredit> outputReader = (ItemReader<CustomerCredit>) applicationContext.getBean("outputTestReader");

        open(outputReader);
        List<CustomerCredit> outputs = new ArrayList<CustomerCredit>(getCredits(outputReader));
        close(outputReader);

        //input과 output 의 자료중 credit 의 값 비교
        assertEquals(inputs.size(), outputs.size());

        // 파티션 작업에서는 순서가 보장되지 않으므로 전체 credit 합계를 비교
        int inputCreditSum = 0;
        int outputCreditSum = 0;

        for (CustomerCredit input : inputs) {
            inputCreditSum += input.getCredit().intValue();
        }

        for (CustomerCredit output : outputs) {
            outputCreditSum += output.getCredit().intValue();
        }

        assertEquals(inputCreditSum, outputCreditSum);
    }

    /**
     * 배치작업의 결과값을 List로 만드는 메소드
     *
     * @param reader
     * @return List<CustomerCredit>
     * @throws Exception
     */
    private List<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
        CustomerCredit credit;
        List<CustomerCredit> result = new ArrayList<CustomerCredit>();
        while ((credit = reader.read()) != null) {
            result.add(credit);
        }
        return result;
    }

    /**
     * reader를 open하는 메소드
     *
     * @param reader
     */
    private void open(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).open(new ExecutionContext());
        }
    }

    /**
     * reader를 close하는 메소드
     *
     * @param reader
     */
    private void close(ItemReader<?> reader) {
        if (reader instanceof ItemStream) {
            ((ItemStream) reader).close();
        }
    }

}
