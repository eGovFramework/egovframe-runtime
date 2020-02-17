package egovframework.rte.bat.core.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

public class TaskletShellStep implements Tasklet, InitializingBean {

	// 실행시 사용하는 Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskletShellStep.class);
	
	private String shellScript;
	private String encoding;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Assert.notNull(directory, "directory must be set");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {

		LOGGER.debug("TaskletDeleteStep execute START ===");

		LOGGER.debug(">>>>> get Shell Script Description() = "+shellScript);
		LOGGER.debug(">>>>> get Shell Script Result Encoding = "+encoding);
		if ( shellScript == null || shellScript.trim().equals("")) {
			throw new UnexpectedJobExecutionException(
					"Shell Script is Empty!");
		}
		
		//String[] arrCmdLine = shellScript.split(System.getProperty("line.separator"));
		//String.split("[\\r\\n]+") // 공백라인 허용하지 않음
		//String lines[] = string.split("\\r?\\n");
		String[] arrCmdLine = shellScript.split("[\\r?\\n]+");
		
		LOGGER.debug("isMac() = "+ShellScriptSupport.isMac());
		LOGGER.debug("isWindows() = "+ShellScriptSupport.isWindows());
		LOGGER.debug("isUnix() = "+ShellScriptSupport.isUnix());
		LOGGER.debug("isSolaris() = "+ShellScriptSupport.isSolaris());
		LOGGER.debug("getOSEncoding() = "+ShellScriptSupport.getOSEncoding());
		LOGGER.debug("arrCmdLine.length = "+arrCmdLine.length);
		int resultShellScript = 0;
		
		if ( arrCmdLine.length == 0 ) { // single line
			LOGGER.debug(">>> Single Line");
			resultShellScript = ShellScriptSupport.shellCmd(shellScript,encoding);
			if ( resultShellScript > 0 ) throw new UnexpectedJobExecutionException(
					"Error Executing shell script~!");
		} else { // multiline
			LOGGER.debug(">>> Multi Line");
			for(int i=0; i<arrCmdLine.length; i++) {
				LOGGER.debug(">>> shell = "+arrCmdLine[i]);
				resultShellScript = ShellScriptSupport.shellCmd(arrCmdLine[i],encoding);
				if ( resultShellScript > 0 ) throw new UnexpectedJobExecutionException(
						"Error Executing shell script~!");
			}
		}

		return RepeatStatus.FINISHED;
	}

	public String getShellScript() {
		LOGGER.debug("shellScript = " + shellScript);
		return shellScript;
	}

	public void setShellScript(String shellScript) {
		LOGGER.debug(">>>>> shellScript = " + shellScript);
		this.shellScript = shellScript;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
