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
package egovframework.brte.sample.example.event;

import java.util.Properties;

import egovframework.rte.bat.core.event.EgovEventNoticeTrigger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * 이벤트 알림을 위한 전자우편을 발송하는 클래스
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
 * </pre>
 */

public class EgovEmailEventNoticeTrigger extends EgovEventNoticeTrigger {

	protected Log logger = LogFactory.getLog(this.getClass());

	// 전자우편 수신자 셋팅(배열형태로 입력)
	String[] emailList = { "****@*****" };
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
			String emailMsgTxt = "============ Notice ============" + "\nJobName : " + jobExecution.getJobInstance().getJobName() + "\nExitStatus : "
					+ jobExecution.getExitStatus().getExitCode();

			postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);

		} catch (Exception e) { // 전자우편 전송 과정에서 에러발생
			logger.info(e);
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
			String emailMsgTxt = "============ Notice ============" + "\nJobName : " + stepExecution.getJobExecution().getJobInstance().getJobName() // 내용
					+ "\nStepName : " + stepExecution.getStepName() + "\nExitStatus : " + stepExecution.getExitStatus().getExitCode();

			postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);

		} catch (Exception e) { // 전자우편 전송 과정에서 에러발생
			logger.info(e);
		}
	}

	/**
	 *  Exception 을 넘겨주는 invoke
	 */
	@Override
	public void invoke(Exception inError) {

		try {

			// 전자우편 제목
			String emailSubjectTxt = "배치 실행 결과 보고서";

			// 전자우편 내용
			String emailMsgTxt = "============ Notice ============\n" + "에러메세지 : " + inError.getMessage();

			postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);

		} catch (Exception e) { // 전자우편 전송 과정에서 에러발생
			logger.info(e);
		}
	}

	/**
	 * 전자우편 전송에서 사용한 메일서버에 대한 정보 설정
	 * SMTPAuthenticator 메서드에 송신자의 계정정보 등록 필요
	 */
	@SuppressWarnings("restriction")
	private void postMail(String recipients[], String subject, String message, String from) throws MessagingException {

		boolean debug = false;

		java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		String SMTP_HOST_NAME = "gmail-smtp.l.google.com";

		// Properties 설정
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.auth", "true");
		Authenticator auth = new SMTPAuthenticator();
		Session session = Session.getDefaultInstance(props, auth);
		session.setDebug(debug);

		// message 생성
		Message msg = new MimeMessage(session);

		// 보내는 주소 등록
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		InternetAddress[] addressTo = new InternetAddress[recipients.length];

		// 받는 주소 등록
		for (int i = 0; i < recipients.length; i++) {
			addressTo[i] = new InternetAddress(recipients[i]);
		}

		msg.setRecipients(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);

	}

	/**
	 * 전자우편 송신을 위해 postMail 에서 이용하는 메일서버에 관한 계정 등록
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {

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
