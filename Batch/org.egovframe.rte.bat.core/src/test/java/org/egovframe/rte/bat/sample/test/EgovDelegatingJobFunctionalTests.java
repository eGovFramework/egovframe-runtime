package org.egovframe.rte.bat.sample.test;

import org.egovframe.rte.bat.config.jobs.DelegatingJob;
import org.egovframe.rte.bat.sample.domain.person.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 기존에 작성된 클래스를 ItemReader나 ItemWriter의 역할로 배치작업을 수행하는 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see
 * @since 2012. 06.27
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DelegatingJob.class)
public class EgovDelegatingJobFunctionalTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovDelegatingJobFunctionalTests.class);

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PersonService personService;

    /**
     * 배치작업 테스트
     */
    @Test
    public void testLaunchJob() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertTrue(personService.getReturnedCount() > 0);
        assertEquals(personService.getReturnedCount(), personService.getReceivedCount());
        LOGGER.debug("### EgovDelegatingJobFunctionalTests testLaunchJob() readCount : {}", personService.getReturnedCount());
        LOGGER.debug("### EgovDelegatingJobFunctionalTests testLaunchJob() writeCount : {}", personService.getReceivedCount());
    }

}
