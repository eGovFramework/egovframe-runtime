package egovframework.brte.sample.example.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;

import egovframework.rte.bat.core.listener.EgovStepPostProcessor;
import egovframework.brte.sample.example.event.EgovEmailEventNoticeTrigger;

/**
 * 이벤트 알림을 호출 하는 리스너 클래스 빈으로 등록된 
 * EgovSmsEventNoticeTrigger, EgovEmailEventNoticeTrigger 를 사용 현재 Sms 서비스는 별도 신청후 사용하는 시스템이므로 Sample 에서는
 * Email 예제만 제공 (Sms 관련은 주석처리 하였음)
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

public class EgovEventNoticeCallProcessor<T, S> extends EgovStepPostProcessor<T, S> {
	protected Log log = LogFactory.getLog(this.getClass());

	// @Autowired
	// EgovSmsEventNoticeTrigger smsEventNoticeTrigger;

	@Autowired
	EgovEmailEventNoticeTrigger egovEmailEventNoticeTrigger;

	/**
	 * Step 수행 완료 후, SMS 혹은 전자우편을 전송하기 위한 invoke를 수행
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		// SMS서비스이용
		// smsEventNoticeTrigger.invoke(stepExecution);

		// 전자우편서비스이용
		egovEmailEventNoticeTrigger.invoke(stepExecution);

		// 수행결과에 따른 상태값 변경
		return stepExecution.getExitStatus();

	}

}
