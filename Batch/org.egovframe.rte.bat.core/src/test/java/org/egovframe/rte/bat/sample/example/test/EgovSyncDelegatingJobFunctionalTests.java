package org.egovframe.rte.bat.sample.example.test;

import org.egovframe.rte.bat.config.SyncJobLauncherConfig;
import org.egovframe.rte.bat.config.jobs.DelegatingJob;
import org.egovframe.rte.bat.sample.domain.person.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JobLauncher 설정을 통한 동기/비동기 처리기능을 테스트
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.07.31  배치실행개발팀     최초 생성
 *  </pre>
 * @since 2012. 07.31
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DelegatingJob.class, SyncJobLauncherConfig.class})
public class EgovSyncDelegatingJobFunctionalTests {

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

        assertTrue(personService.getReturnedCount() > 0);
        assertEquals(personService.getReturnedCount(), personService.getReceivedCount());
        assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

}
