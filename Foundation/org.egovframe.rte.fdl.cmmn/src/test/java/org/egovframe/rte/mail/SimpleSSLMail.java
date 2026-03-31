package org.egovframe.rte.mail;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * EgovMail 구현체 (샘플용)
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @version 1.0
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  Judd Cho        최초 생성
 *
 * </pre>
 * @since 2009.06.01
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
        message.setFrom(new InternetAddress("www.egovframe.go.kr", "webmaster", "euc-kr"));
        message.setSubject(subject);
        message.setContent(content, contentType + ";charset=utf-8");

        for (String receiver : getReceivers()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
        }

        transport.connect(getHost(), getPort(), getUsername(), getPassword());
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }

}
