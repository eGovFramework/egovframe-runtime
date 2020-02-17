/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.rte.bat.core.listener;

import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;

/**
 * EgovOutputFileListener
 * input File 과 같은 이름으로 output File 명 지정
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 * 
 *      <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 * 
 * </pre>
 */
public class EgovOutputFileListener {

	// outputKeyName
	private String outputKeyName = "outputFile";

	// inputKeyName
	private String inputKeyName = "fileName";

	// path
	private String path = "file:./target/output/";

	/**
	 * path 셋팅
	 * 
	 * @param path
	 */

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * outputKeyName 셋팅
	 * 
	 * @param outputKeyName
	 */

	public void setOutputKeyName(String outputKeyName) {
		this.outputKeyName = outputKeyName;
	}

	/**
	 * inputKeyName 셋팅
	 * 
	 * @param inputKeyName
	 */
	public void setInputKeyName(String inputKeyName) {
		this.inputKeyName = inputKeyName;
	}

	/**
	 * stepExecutionContext에 inputKeyName 을 이용하여 outputKeyName을 put 함
	 * 
	 * @param stepExecution
	 */
	@BeforeStep
	public void createOutputNameFromInput(StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getExecutionContext();
		String inputName = stepExecution.getStepName().replace(":", "-");
		if (executionContext.containsKey(inputKeyName)) {
			inputName = executionContext.getString(inputKeyName);
		}
		if (!executionContext.containsKey(outputKeyName)) {
			executionContext.putString(outputKeyName,
					path + FilenameUtils.getBaseName(inputName) + ".csv");
		}
	}

}
