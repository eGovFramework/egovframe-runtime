package org.egovframe.rte.bat.core.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class DummyTasklet implements Tasklet, InitializingBean {

		
	  @Override
	  public void afterPropertiesSet() throws Exception {
		//Assert.notNull(directory, "directory must be set");
	  }

	  @Override
	  public RepeatStatus execute(StepContribution contribution,
	               ChunkContext chunkContext) throws Exception {
		  
			System.out.println("TaskletStep execute START ===");
			System.out.println("TaskletStep execute START ===");
			System.out.println("TaskletStep execute START ===");
			System.out.println("TaskletStep execute START ===");
			System.out.println("TaskletStep execute START ===");
			System.out.println("TaskletStep execute START ===");
			

			//System.out.println("@Value jobVariable2>>>"+vJobVariable2);

		return RepeatStatus.FINISHED;
	  }


	  
}
