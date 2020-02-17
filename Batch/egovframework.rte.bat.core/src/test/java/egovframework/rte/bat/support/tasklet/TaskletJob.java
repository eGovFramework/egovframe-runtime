package egovframework.rte.bat.support.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class TaskletJob implements Tasklet, InitializingBean {

		private Resource directory;
	 
		private String jobVariable;
		
		private String stepVariable;
		
		private String resourceVariable;
		
		
		@Value("#{jobExecutionContext[JobVariableKey2]}")
		private String vJobVariable;
		
		private String vStepVariable;
		
		private String vResourceVariable;
		

		public String getJobVariable() {
			return jobVariable;
		}

		public void setJobVariable(String jobVariable) {
			this.jobVariable = jobVariable;
		}

		public String getStepVariable() {
			return stepVariable;
		}

		public void setStepVariable(String stepVariable) {
			this.stepVariable = stepVariable;
		}

		public String getResourceVariable() {
			return resourceVariable;
		}

		public void setResourceVariable(String resourceVariable) {
			this.resourceVariable = resourceVariable;
		}


	  @Override
	  public void afterPropertiesSet() throws Exception {

	  }

	  @Override
	  public RepeatStatus execute(StepContribution contribution,
	               ChunkContext chunkContext) throws Exception {
		  
		  	System.setProperty("system.jobVariable1", String.valueOf(chunkContext.getStepContext().getJobExecutionContext().get("JobVariableKey1")));

		return RepeatStatus.FINISHED;
	  }

	  public Resource getDirectory() {
		return directory;
	  }

	  public void setDirectory(Resource directory) {
		this.directory = directory;
	  }
	  
}
