package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.jobs.RetrySample;
import org.egovframe.rte.bat.sample.domain.trade.GeneratingTradeItemReader;
import org.egovframe.rte.bat.sample.example.support.EgovRetrySampleItemWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Data를 처리하다 에러시 Retry 시도 횟수 설정만큼 재시도 하는 과정을 테스트
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
@ContextConfiguration(classes = RetrySample.class)
public class EgovRetrySampleFunctionalTests {

    @Autowired
    private GeneratingTradeItemReader itemGenerator;

    @Autowired
    private EgovRetrySampleItemWriter<?> itemProcessor;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    /**
     * 배치작업 테스트
     */
    @Test
    public void testLaunchJob() throws Exception {
        jobLauncherTestUtils.launchJob();
        assertEquals(itemGenerator.getLimit() + 2, itemProcessor.getCounter());
    }

}
