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

package egovframework.rte.bat.core.launch.support;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.util.Assert;

/**
 * EgovBatchRunner 클래스
 * @author 실행환경 개발팀 이도형
 * @since 2012.06.28
 * @version 1.0
 * @see 
 * <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.28  이도형     최초 생성
 *  2012.07.03  이도형     Job Parameter 변환 메소드 추가
 * </pre>
*/
public class EgovBatchRunner {
	
	//Batch Job의 시작, 정지, 재시작에 사용되는 JobOperator
	private JobOperator jobOperator;
	
	//JobExecution 조회 등에 사용되는 JobExplorer
	private JobExplorer jobExplorer;
	
	//가장 최근에 실행된 JobExecution, StepExecution 조회 등에 사용되는 JobRepository 
	private JobRepository jobRepository;
	
	/**
	 * EgovBatchRunner 생성자
	 * JobOperator, JobExplorer, JobRepository를 설정한다.
	 * 
	 * @param jobOperator
	 * @param jobExplorer
	 * @param jobRepository
	 */
	public EgovBatchRunner(JobOperator jobOperator, JobExplorer jobExplorer,
			JobRepository jobRepository) {
		
		Assert.notNull(jobOperator, "The JobOperator is mandatory");
		Assert.notNull(jobExplorer, "The JobExplorer is mandatory");
		Assert.notNull(jobRepository, "The JobRepository is mandatory");
		
		this.jobOperator = jobOperator;
		this.jobExplorer = jobExplorer;
		this.jobRepository = jobRepository;
	}
	
	/**
	 * JobOperator를 사용할 수 있도록 가져온다.
	 * 
	 * @return JobOperator
	 */
	public JobOperator getJobOperator() {
		return jobOperator;
	}
	
	/**
	 * JobExplorer를 사용할 수 있도록 가져온다.
	 * 
	 * @return JobExplorer
	 */
	public JobExplorer getJobExplorer() {
		return jobExplorer;
	}

	/**
	 * JobRepository를 사용할 수 있도록 가져온다.
	 * 
	 * @return JobRepository
	 */
	public JobRepository getJobRepository() {
		return jobRepository;
	}
	
	/**
	 * Batch Job을 시작한다.
	 * @param jobName : Job 이름  
	 * @param jobParameters: String 형태의 Job 파라미터들
	 * @return Long: JobExecution의 ID
	 * @throws NoSuchJobException
	 * @throws JobParametersInvalidException
	 * @throws JobInstanceAlreadyExistsException
	 */
	public Long start(String jobName, String jobParameters) throws NoSuchJobException, JobParametersInvalidException, JobInstanceAlreadyExistsException  {
		return jobOperator.start(jobName, jobParameters);
	}
	
	/**
	 * 정지, 종료되었거나 실패한 Batch Job을 재시작한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @return Long: JobExecution의 ID
	 * @throws JobInstanceAlreadyCompleteException
	 * @throws NoSuchJobExecutionException
	 * @throws NoSuchJobException
	 * @throws JobRestartException
	 * @throws JobParametersInvalidException
	 */
	public Long restart(Long jobExecutionId) 
			throws JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException, JobParametersInvalidException {
		return jobOperator.restart(jobExecutionId);
	}
	
	/**
	 * 실행 중인 Batch Job을 정지한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @throws NoSuchJobExecutionException
	 * @throws JobExecutionNotRunningException
	 */
	public void stop(Long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException  {
		jobOperator.stop(jobExecutionId);
	}
	
	/**
	 * 실행에 필요한  JobParameters String을 생성한다.
	 * @return JobParameters
	 */	
	public String createUniqueJobParameters() {
		Map<String, JobParameter> parameters = new HashMap<String, JobParameter>();
		parameters.put("timestamp", new JobParameter(new Date().getTime()));
		return convertJobParametersToString(new JobParameters(parameters));
	}
	
	/**
	 * 이미 생성되어 있는 JobParamters String에 JobParameter를 추가한다. 
	 * @param jobParameters
	 * @param parameterName : 추가할 JobParamter 이름 
	 * @param parameterValue : 추가할 JobParamter 값
	 * @return String 형태의 JobParamters
	 */
	public String addJobParameter(String jobParameters, String parameterName, String parameterValue) {
		String delim = "";
		
		if (!"".equals(jobParameters)) {
			delim = ",";  
		}
		
		return jobParameters.concat(delim).concat(parameterName).concat("=").concat(parameterValue); 
	}
	
	/**
	 * JobParamters를 String으로 변환한다.
	 * @param jobParamters
	 * @return String으로 변환된 JobParamters
	 */
	public String convertJobParametersToString(JobParameters jobParamters) {
		String convertedJobParameters = jobParamters.toString();
		
		if (convertedJobParameters.startsWith("{")) {
			convertedJobParameters = convertedJobParameters.substring(1);
		}
		
		if (convertedJobParameters.endsWith("}")) {
			convertedJobParameters = convertedJobParameters.substring(0, (convertedJobParameters.length() - 1));
		}
		
		return convertedJobParameters;
	}
	
	/**
	 * 여러건의 등록된 Job을 조회한다.
	 * @return Set String : Job 이름 목록
	 */
	public Set<String> getJobNames() {
		return jobOperator.getJobNames();
	}
	
	/**
	 * JobExecution ID로 JobExecution를 조회한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @return JobExecution
	 */
	public JobExecution getJobExecution(Long jobExecutionId) {
		return jobExplorer.getJobExecution(jobExecutionId);
	}
	
	/**
	 * JobExecution ID로 JobExecution를 조회한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @return  List JobExecution  : JobExecution 목록
	 */
	public List<JobExecution> getJobExecutions(Long jobExecutionId) {
		return jobExplorer.getJobExecutions(getJobInstance(jobExecutionId));
	}
	
	/**
	 * 마지막으로 수행된 JobExecution를 조회한다.
	 * @param jobName : Job 이름
	 * @param jobParameters : Job 파라미터들
	 * @return  List JobExecution : JobExecution 목록
	 */
	public JobExecution getLastJobExecution(String jobName, JobParameters jobParameters) {
		return jobRepository.getLastJobExecution(jobName, jobParameters);
	}
	
	/**
	 * 마지막으로 수행된 StepExecution를 조회한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @param stepName : Step 이름
	 * @return  List JobExecution : JobExecution 목록
	 */
	public StepExecution getLastStepExecution(Long jobExecutionId, String stepName) {
		return jobRepository.getLastStepExecution(getJobInstance(jobExecutionId), stepName);
	}
	
	/**
	 * JobExecution ID로 JobInstance를 조회한다.
	 * @param jobExecutionId : JobExecution의 ID
	 * @return JobInstance
	 */
	public JobInstance getJobInstance(Long jobExecutionId) {
		return getJobExecution(jobExecutionId).getJobInstance();
	}
	
}
