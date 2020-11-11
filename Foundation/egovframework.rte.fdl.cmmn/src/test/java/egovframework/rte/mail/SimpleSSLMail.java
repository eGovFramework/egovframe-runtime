/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * EgovMail 구현체 (샘플용)
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  Judd Cho        최초 생성
 *
 * </pre>
 */
public class SimpleSSLMail extends EgovMail {


	public void send(String subject, String content) throws Exception {
		send(subject, content, "text/plain");
	}


	public void send(String subject, String content, String contentType) throws Exception {
		Properties props = new Properties();

		props.put("mail.transport.protocol", "smtps");
		props.put("mail.smtps.host", getHost());
		props.put("mail.smtps.auth", "true");

		Session mailSession = Session.getDefaultInstance(props);
		mailSession.setDebug(true);
		Transport transport = mailSession.getTransport();

		MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress("www.egovframe.org", "webmaster", "euc-kr"));
		message.setSubject(subject);
		message.setContent(content, contentType+";charset=utf-8");// "text/plain"

		for (String receiver : getReceivers())
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));

		transport.connect(getHost(), getPort(), getUsername(), getPassword());
		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

}