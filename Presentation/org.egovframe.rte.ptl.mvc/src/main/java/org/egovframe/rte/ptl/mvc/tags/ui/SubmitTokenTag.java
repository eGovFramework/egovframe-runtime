/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ptl.mvc.tags.ui;

import java.io.IOException;

import org.egovframe.rte.ptl.mvc.token.EgovSubmitTokens;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * 중복 제출 방지 토큰을 hidden input 으로 출력하는 JSP 태그.
 *
 * <pre>
 * &lt;%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui" %&gt;
 * &lt;form action="/orders" method="post"&gt;
 *     &lt;ui:submitToken/&gt;                        &lt;!-- 기본 키 --&gt;
 *     &lt;ui:submitToken tokenKey="orderForm"/&gt;    &lt;!-- 폼별 키 --&gt;
 * &lt;/form&gt;
 * </pre>
 *
 * <p>토큰 발급·검증 로직은 {@link EgovSubmitTokens} 가 갖고 있으며 이 태그는 출력만 담당한다.
 * <b>JSP 가 아닌 뷰</b>(Thymeleaf·SPA 등)는 태그 없이 컨트롤러에서
 * {@code EgovSubmitTokens.issue(request)} 로 토큰을 얻어 모델·응답에 담으면 된다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성 (공통컴포넌트 DoubleSubmitTag 가 발급 로직까지
 *                            품고 있어 JSP 없이는 쓸 수 없던 구조를, 출력 전용 얇은 태그로 분리)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @see EgovSubmitTokens
 */
public class SubmitTokenTag extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String tokenKey = EgovSubmitTokens.DEFAULT_TOKEN_KEY;

	/**
	 * 토큰 키를 반환한다.
	 *
	 * @return 토큰 키
	 */
	public String getTokenKey() {
		return tokenKey;
	}

	/**
	 * 토큰 키를 지정한다. 한 세션에서 여러 폼을 동시에 열 때 폼마다 다른 키를 주면
	 * 서로 간섭하지 않는다.
	 *
	 * @param tokenKey 토큰 키
	 */
	public void setTokenKey(String tokenKey) {
		this.tokenKey = tokenKey;
	}

	/**
	 * 토큰을 발급해 hidden input 으로 출력한다.
	 *
	 * @return {@link #SKIP_BODY}
	 * @throws JspException 출력 중 IO 오류가 발생한 경우
	 */
	@Override
	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		String token = EgovSubmitTokens.issue(request, tokenKey);

		String html = "<input type=\"hidden\" name=\"" + EgovSubmitTokens.PARAMETER_NAME
				+ "\" value=\"" + token + "\"/>";
		try {
			pageContext.getOut().print(html);
		} catch (IOException e) {
			throw new JspTagException("제출 토큰 출력 중 오류가 발생했습니다.", e);
		}
		return SKIP_BODY;
	}

}
