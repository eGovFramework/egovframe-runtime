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
package org.egovframe.rte.ai.core.response;

import java.util.regex.Pattern;

/**
 * 모델 출력 정리 — 추론 모델의 사고 태그(<code>&lt;think&gt;…&lt;/think&gt;</code>) 제거와 JSON 객체 추출.
 *
 * <p>Qwen3·DeepSeek-R1 계열은 답 앞에 사고 과정을 태그로 내보내 구조화 출력 파싱을 깨뜨린다. 어댑터의 구조화 출력 변환기가
 * 파싱 전에 이 정리를 거친다.</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 EgovResponseCleanerUtil 차용)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovModelOutputs {

	private static final Pattern THINK_BLOCK =
			Pattern.compile("<think>.*?</think>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private EgovModelOutputs() {
	}

	/**
	 * 사고 태그 블록(대소문자 무관·여러 줄)을 제거하고 앞뒤 공백을 정리한다. null·빈 입력은 그대로.
	 */
	public static String stripReasoning(String output) {
		if (output == null || output.isBlank()) {
			return output;
		}
		return THINK_BLOCK.matcher(output).replaceAll("").trim();
	}

	/**
	 * 사고 태그를 제거한 뒤 첫 <code>{</code> 부터 마지막 <code>}</code> 까지를 돌려준다. JSON 객체가 없으면 정리된 원문.
	 */
	public static String extractJsonObject(String output) {
		String cleaned = stripReasoning(output);
		if (cleaned == null || cleaned.isEmpty()) {
			return cleaned;
		}
		int start = cleaned.indexOf('{');
		int end = cleaned.lastIndexOf('}');
		if (start >= 0 && end > start) {
			return cleaned.substring(start, end + 1);
		}
		return cleaned;
	}
}
