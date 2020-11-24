/*
 * Copyright 2006-2007 the original author or authors. 
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
package org.egovframe.brte.sample.example.event;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Controller;

import org.egovframe.rte.bat.core.event.EgovEventNoticeTrigger;

//import org.egovframe.brte.support.EgovEventNoticeTrigger;
//import org.egovframe.com.cop.sms.service.EgovSmsInfoService;
//import org.egovframe.com.cop.sms.service.Sms;

/**
 * 공통컴포넌트의 SMS 전송을 이용하려면 위의 주석내용을 import 해야하며,
 * 이 서비스는 별도의 M-Gov 신청을 통해 사용할 수 있다.
 * 
 * 이벤트 알림을 위한 SMS 메시지 등록메소드를 호출하는 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012.06.27
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 * 2012.06.27  배치실행개발팀     최초 생성
 *  </pre>
 */

@Controller
public class EgovSmsEventNoticeTrigger extends EgovEventNoticeTrigger {

	protected Log log = LogFactory.getLog(this.getClass());

	@Resource(name = "EgovSmsInfoService")
	//	protected EgovSmsInfoService smsInfoService;
	/**
	 * StepExecution을 넘겨주는 invoke
	 */
	public void invoke(StepExecution stepExecution) {
		log.info("SMS 서비스 등록");

		// SMS 전송을 위해 필요한 VO
		// Sms sms = new Sms();

		// stepExecution의 정보를 이용해 전송 내용 작성
		// sms.setTrnsmitCn("=== 알 림 ===" + "\nStepName : " + stepExecution.getStepName() + "\nExitStatus : "	+ stepExecution.getExitStatus().getExitCode());

		// sms.setTrnsmitTelno("0101234567");

		try {
			// SMS 등록 
			//전자정부 SMS 서비스(모바일 전자정부 M-Gov)는 별도의 M-Gov 신청을 통해 사용가능
			// smsInfoService.insertSmsInf(sms);

		} catch (Exception e) {
			// 전자우편 전송 과정에서 에러발생
			log.debug(e);
		}
	}
}
