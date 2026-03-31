package org.egovframe.rte.bat.core.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

public class DummyTasklet implements Tasklet, InitializingBean {

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("TaskletStep execute START ===");
        System.out.println("TaskletStep execute START ===");
        System.out.println("TaskletStep execute START ===");
        System.out.println("TaskletStep execute START ===");
        System.out.println("TaskletStep execute START ===");
        System.out.println("TaskletStep execute START ===");

        return RepeatStatus.FINISHED;
    }

}
