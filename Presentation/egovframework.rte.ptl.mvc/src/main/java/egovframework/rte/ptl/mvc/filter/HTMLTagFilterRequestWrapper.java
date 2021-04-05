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
package egovframework.rte.ptl.mvc.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HTMLTagFilterRequestWrapper.java
 *
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  함철            최초 생성
 *
 * </pre>
 */
public class HTMLTagFilterRequestWrapper extends HttpServletRequestWrapper {

	public HTMLTagFilterRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);

		if (values == null) {
			return null;
		}

		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				values[i] = this.doFilter(values[i]);
			}
		}

		return values;
	}

	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);

		if (value == null) {
			return null;
		}

		value = this.doFilter(value);

		return value;
	}

	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paramMap = super.getParameterMap();
		Map<String, String[]> newFilteredParamMap = new HashMap<String, String[]>();
		Set<Entry<String, String[]>> set = paramMap.entrySet();

		for (Entry<String, String[]> entry : set) {
			String[] valueObj = entry.getValue();
			String[] filteredValue = new String[valueObj.length];

			for (int index = 0; index < valueObj.length; ++index) {
				filteredValue[index] = this.doFilter(String.valueOf(valueObj[index]));
			}

			newFilteredParamMap.put(entry.getKey(), filteredValue);
		}

		return newFilteredParamMap;
	}

	private String doFilter(String value) {
		if (value == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&apos;");
					break;
				default:
					sb.append(c);
					break;
			}
		}

		value = sb.toString();

		return value;
	}

}