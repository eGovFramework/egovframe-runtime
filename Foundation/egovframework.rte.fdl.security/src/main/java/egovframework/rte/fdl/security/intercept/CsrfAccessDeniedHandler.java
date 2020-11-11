package egovframework.rte.fdl.security.intercept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CSRF Access Denied 처리
 *
 * <p>Desc.: CSRF 사용 시 token 이 없거나(InvalidCsrfTokenException) 잘못된 경우(MissingCsrfTokenException) Access Denied URL 로 넘김</p>
 *
 * @author Egovframework Center
 * @since 2020.05.27
 * @version 3.10
 * @see <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일		수정자					수정내용
 * ---------------------------------------------------------------------------------
 * 2020.05.27	Egovframework Center	최초 생성
 *
 * </pre>
 */
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfAccessDeniedHandler.class);

    private final String csrfAccessDeniedUrl;

    public CsrfAccessDeniedHandler(String csrfAccessDeniedUrl) {
        this.csrfAccessDeniedUrl = csrfAccessDeniedUrl;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (accessDeniedException instanceof InvalidCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(csrfAccessDeniedUrl);
            dispatcher.forward(request, response);
        }

        if (accessDeniedException instanceof MissingCsrfTokenException) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(csrfAccessDeniedUrl);
            dispatcher.forward(request, response);
        }
    }

}
