package org.egovframe.rte.bat.sample.example.event;

import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import org.egovframe.rte.bat.core.event.EgovEventNoticeTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * 이벤트 알림을 위한 전자우편을 발송하는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 * 2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 * @since 2012.06.27
 */
public class EgovEmailEventNoticeTrigger extends EgovEventNoticeTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovEmailEventNoticeTrigger.class);

    // 전자우편 수신자 셋팅(배열형태로 입력)
    String[] emailList = {"****@*****"};

    // 전자우편 송신자 셋팅
    String emailFromAddress = "****@*****";

    /**
     * JobExecution 을 넘겨주는 invoke
     */
    @Override
    public void invoke(JobExecution jobExecution) {
        try {
            // 전자우편 제목
            String emailSubjectTxt = jobExecution.getJobInstance().getJobName() + " 의 실행 결과 보고서";

            // 전자우편 내용
            String emailMsgTxt = "============ Notice ============"
                    + "\nJobName : "
                    + jobExecution.getJobInstance().getJobName()
                    + "\nExitStatus : "
                    + jobExecution.getExitStatus().getExitCode();

            postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
        } catch (MessagingException e) { // 전자우편 전송 과정에서 에러발생
            LOGGER.debug("[{}] EgovEmailEventNoticeTrigger invoke() : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * StepExecution 을 넘겨주는 invoke
     */
    @Override
    public void invoke(StepExecution stepExecution) {
        try {
            // 전자우편 제목
            String emailSubjectTxt = stepExecution.getStepName() + " 의 실행 결과 보고서";

            // 전자우편 내용
            String emailMsgTxt = "============ Notice ============" + "\nJobName : "
                    + stepExecution.getJobExecution().getJobInstance().getJobName() // 내용
                    + "\nStepName : "
                    + stepExecution.getStepName()
                    + "\nExitStatus : "
                    + stepExecution.getExitStatus().getExitCode();

            postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
        } catch (MessagingException e) { // 전자우편 전송 과정에서 에러발생
            LOGGER.debug("[{}] EgovEmailEventNoticeTrigger invoke() : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * Exception 을 넘겨주는 invoke
     */
    @Override
    public void invoke(Exception inError) {
        try {
            // 전자우편 제목
            String emailSubjectTxt = "배치 실행 결과 보고서";

            // 전자우편 내용
            String emailMsgTxt = "============ Notice ============\n" + "에러메세지 : " + inError.getMessage();

            postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
        } catch (MessagingException e) { // 전자우편 전송 과정에서 에러발생
            LOGGER.debug("[{}] EgovEmailEventNoticeTrigger invoke() : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 전자우편 전송에서 사용한 메일서버에 대한 정보 설정
     * SMTPAuthenticator 메서드에 송신자의 계정정보 등록 필요
     */
    @SuppressWarnings("restriction")
    private void postMail(String[] recipients, String subject, String message, String from) throws MessagingException {
        // 테스트 환경에서는 실제 메일을 보내지 않고 로그만 출력
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() Start ");
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() From: {}", from);
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() To: {}", String.join(", ", recipients));
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() Subject: {}", subject);
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() Message: {}", message);
        LOGGER.debug("### EgovEmailEventNoticeTrigger postMail() End ");
    }

    /**
     * 전자우편 송신을 위해 postMail 에서 이용하는 메일서버에 관한 계정 등록
     */
    private class SMTPAuthenticator extends jakarta.mail.Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            // gmail계정
            String username = "****@****";
            // 패스워드
            String password = "********";
            return new PasswordAuthentication(username, password);
        }
    }

}
