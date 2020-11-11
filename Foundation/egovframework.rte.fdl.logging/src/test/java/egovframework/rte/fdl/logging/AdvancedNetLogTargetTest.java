package egovframework.rte.fdl.logging;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import egovframework.rte.fdl.logging.util.LogFileUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-common.xml" })
public class AdvancedNetLogTargetTest {

	/** testSocketAppender() 테스트 케이스 실행 방법
	 *  1. 반드시 classpath에 있는 runSocketServer.bat 를 실행한다.
	 *  2. runSocketServer.bat가 두개 이상 실행되어 있는 경우, 에러를 발생시킨다.
	 *  3. classpath에 필요한 파일
	 *  ./lib/log4j-api-2.0-rc1.jar; ./lib/log4j-core-2.0-rc1.jar, ./remote/log4j2.xml, ./runSocketServer.bat, ./runSocketServer.sh
	 * */

	/**
	 *  아래는 SocketAppender를 테스트하는 메소드
	 *  logger: level=DEBUG, appender=Socket
	 * */
	@Test
	public void testSocketAppender() throws Exception {

		Logger logger = LogManager.getLogger("socketLogger"); 
		// 전송하는 쪽: resource아래 있는 log4j2의 socketLogger는 Socket Appender에 byte array 형태로 직렬화하여 로그 출력 (SerializedLayout)
		// 받는 쪽: remote아래 있는 log4j2의 socketLogger는 file과 console에 출력

		// 로그 출력
		logger.debug("SocketAppender test");

		// remote socket 으로 전송하는 시간만큼 기다림
		Thread.sleep(500);

		// remote socket 로그 확인
		// 테스트 편의를 위해 localhost 에 SimpleSocketServer 를 띄우고 remote/log4j2.xml 에서는
		// socketLogger 로 들어오는 로그를 ./remote/logs/remoteSample.log 에 기록되게 하였음
		File remoteFile = new File("./remote/logs/remoteSample.log");
		String lastLine = LogFileUtil.getLastLine(remoteFile);

		// 로그파일의 마지막 라인 확인
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
		assertTrue(lastLine.contains(sdf.format(new Date())));
		assertTrue(lastLine.endsWith("SocketAppender test"));
	}

	/** testSMTPAppender() 테스트 케이스 실행 방법
	 *  1. 반드시 로컬 pc에 원하는 메일서버를 설치하고, 새로운 계정을 생성한다. (James 메일 서버로 테스트 함) 
	 *  2. 설치한 서버와 user 정보에 따라 log4j2.xml의 SMTP Appender 설정 정보를 확인하고 변경하여야 한다.  (<Properties>로 설정됨)
	 *  3. 반드시 확인하고 변경해야할 정보는 수신자(smtp.to), 서버호스트(smtp.host), 포트(smtp.port) 이다.
	 *  4. 테스트 케이스 실행 전, 서버(run.bat)를 띄운다. 
	 *  5. SMTP를 통해 보낸 메일을 확인하기 위해 outlook으로 연결하여 생성한 계정으로 로그인한다.
	 *  6. 테스트 케이스 실행 후 받은 편지함을 확인한다.
	 * */
	
	/**
	 *  아래는 SMTPAppender를 테스트하는 메소드
	 *  logger: level=ERROR, appender=SMTP
	 * */
	@Test
	public void testSMTPAppender() throws Exception {
		
		System.setProperty("mail.smtp.starttls.enable", "true");
		System.setProperty("mail.smtps.auth", "true");

		Logger logger = LogManager.getLogger("mailLogger");
		// 로그 출력
		logger.error("SMTPAppender 테스트 test");
		// 받은 메일함 확인
	}
}
