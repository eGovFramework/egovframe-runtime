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
package org.egovframe.rte.ptl.mvc.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

/**
 * HTMLTagFilterRequestWrapper.java
 * 
 * @author 실행환경 개발팀 함철
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.05.30	함철				최초 생성
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
				values[i] = getSafeParamData(values[i]);
			} else {
				values[i] = null;
			}
		}
		return values;
	}

	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);
		if (value == null) {
			return null;
		}
		value = getSafeParamData(value);
		return value;
	}

	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> valueMap = super.getParameterMap();
		String[] values;
		for (String key : valueMap.keySet()) {
			values = valueMap.get(key);
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					values[i] = getSafeParamData(values[i]);
				} else {
					values[i] = null;
				}
			}
		}
		return valueMap;
	}

	public String getSafeParamData(String value) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '<':
					stringBuilder.append("&lt;");
					break;
				case '>':
					stringBuilder.append("&gt;");
					break;
				case '&':
					stringBuilder.append("&amp;");
					break;
				case '"':
					stringBuilder.append("&quot;");
					break;
				case '\'':
					stringBuilder.append("&apos;");
					break;
				default:
					stringBuilder.append(c);
					break;
			}
		}
		value = stringBuilder.toString();
		return value;
	}

}
