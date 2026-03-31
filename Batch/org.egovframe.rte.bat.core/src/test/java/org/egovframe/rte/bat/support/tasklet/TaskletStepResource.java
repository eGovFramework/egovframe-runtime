package org.egovframe.rte.bat.support.tasklet;

import jakarta.annotation.Resource;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

public class TaskletStepResource implements Tasklet, InitializingBean {

    @Resource(name = "egovResourceVariable")
    private EgovResourceVariable egovResourceVariable;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        egovResourceVariable.setVariable("VariableTEST", "VariableTEST12345");
        return RepeatStatus.FINISHED;
    }

}
