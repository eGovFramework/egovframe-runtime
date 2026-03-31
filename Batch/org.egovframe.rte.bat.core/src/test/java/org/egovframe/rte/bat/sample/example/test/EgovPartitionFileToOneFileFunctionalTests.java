package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SimpleJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.PartitionFileToOneFileJob;
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
 * 다수의 파일을 파티셔닝을 이용해 하나의 파일로 처리하는 테스트
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
@ContextConfiguration(classes = {PartitionFileToOneFileJob.class, SimpleJobLauncherConfig.class})
public class EgovPartitionFileToOneFileFunctionalTests implements ApplicationContextAware {

    @Autowired
    private ItemReader<CustomerCredit> inputReader;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 배치작업 테스트
     */
    @Test
    public void testUpdateCredit() throws Exception {
        assertTrue(applicationContext.containsBeanDefinition("outputTestReader"), "Define a prototype bean called 'outputTestReader' to check the output");

        open(inputReader);
        List<CustomerCredit> inputs = new ArrayList<CustomerCredit>(getCredits(inputReader));
        close(inputReader);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        @SuppressWarnings("unchecked")
        ItemReader<CustomerCredit> outputReader = (ItemReader<CustomerCredit>) applicationContext.getBean("outputTestReader");
        open(outputReader);
        List<CustomerCredit> outputs = new ArrayList<CustomerCredit>(getCredits(outputReader));
        close(outputReader);

        // inputFile 과 outputFile 에 포함된 line 수 비교
        assertEquals(inputs.size(), outputs.size());

        // inputFile 과 outputFile 에 포함된 모든 credit 의 합을 비교
        int inputCredit = 0;
        int outputCredit = 0;

        //input 파일들에서 Credit 의 합
        inputs.iterator();
        for (int i = 0; i < inputs.size(); i++) {
            inputCredit += inputs.get(i).getCredit().intValue();
        }

        //output 파일들에서 Credit 의 합
        for (int j = 0; j < outputs.size(); j++) {
            outputCredit += outputs.get(j).getCredit().intValue();
        }

        assertEquals(inputCredit, outputCredit);
    }

    /**
     * 배치작업의 결과값을 List로 만드는 메소드
     */
    @SuppressWarnings("unused")
    private List<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
        CustomerCredit credit;
        List<CustomerCredit> result = new ArrayList<CustomerCredit>();
        while ((credit = reader.read()) != null) {
            int i = 0;
            i++;
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

}
