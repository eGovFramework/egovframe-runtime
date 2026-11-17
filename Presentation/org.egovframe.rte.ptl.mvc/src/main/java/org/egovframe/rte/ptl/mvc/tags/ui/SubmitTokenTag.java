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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 중복 제출 방지 토큰을 hidden input 으로 출력하는 JSP 태그.
 * 토큰 발급은 {@link EgovSubmitTokens}가 담당하며, 이 태그는 출력만 한다.
 *
 * <pre>
 * &lt;ui:submitToken tokenKey="orderForm"/&gt;
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.01  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
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
	 * 토큰 키를 지정한다(폼별로 다른 키를 주면 서로 간섭하지 않는다).
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
