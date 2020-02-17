/*
 * Copyright 2006-2007 the original author or authors. *
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
package egovframework.brte.sample.example.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import egovframework.brte.sample.common.domain.trade.Trade;

/**
 * Skip 된 데이터를 처리하는 Listener 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012.06.27
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 */

public class EgovSkipCheckingListener {

	private static final Log logger = LogFactory.getLog(EgovSkipCheckingListener.class);

	// processSkips
	private static int processSkips;

	/**
	 * 잡수행이 Fail이고, Skip이 일어났을 경우, ExitStatus 상태를 COMPLETED WITH SKIPS 으로 변경
	 */
	@AfterStep
	public ExitStatus checkForSkips(StepExecution stepExecution) {
		if (!stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode()) && stepExecution.getSkipCount() > 0) {
			return new ExitStatus("COMPLETED WITH SKIPS");
		} else {
			return null;
		}
	}

	/**
	 * ProcessSkips Getter
	 */
	public static int getProcessSkips() {
		return processSkips;
	}

	/**
	 * ProcessSkips 리셋
	 */
	public static void resetProcessSkips() {
		processSkips = 0;
	}

	/**
	 * Write 하다 Skip 이 발생할 경우 수행
	 */

	@OnSkipInWrite
	public void skipWrite(Trade trade, Throwable t) {
		logger.debug("Skipped writing " + trade);
	}

	/**
	 * Process 하다 Skip 이 발생할 경우 수행
	 */
	@OnSkipInProcess
	public void skipProcess(Trade trade, Throwable t) {
		logger.debug("Skipped processing " + trade);
		processSkips++;
	}

	/**
	 * Step 수행 전에 stepName을 Context에 put 함
	 */
	@BeforeStep
	public void saveStepName(StepExecution stepExecution) {
		stepExecution.getExecutionContext().put("stepName", stepExecution.getStepName());
	}
}
