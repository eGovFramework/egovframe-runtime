package org.egovframe.rte.fdl.security.config.internal;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SessionInformationExpiredStrategy를 처리하는 factory bean 클래스
 * 
 *<p>Desc.: MaximumSessionsFactoryBean Deprecated에 대한 대응</p>
 *
 * @author 윤창원
 * @since 2019.12.02
 * @version 3.9
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2019.12.02	윤창원				SessionInformationExpiredStrategy를 처리하는 factory bean 클래스 생성
 * </pre>
 */
public class EgovSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

	private final String expiredUrl;
	
	public EgovSessionInformationExpiredStrategy(String expiredUrl) {
		this.expiredUrl = expiredUrl;
	}
	
	@Override
	public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
		HttpServletRequest request = event.getRequest();
		HttpServletResponse response = event.getResponse();
		response.sendRedirect(request.getContextPath() + expiredUrl);
	}

}