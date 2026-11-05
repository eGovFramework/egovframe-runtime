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
package org.egovframe.rte.ai.core.prompt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 프롬프트 템플릿 레지스트리 — 키로 원문을 찾고 <code>{변수}</code> 플레이스홀더를 치환한다.
 *
 * <p>설정(예: Boot <code>egov.ai.prompt.templates.*</code>)에서 만든 맵으로 생성하며 이후 불변이다. <b>미등록 키는 예외</b>다 —
 * 참고 샘플은 빈 문자열을 돌려줘 빈 프롬프트가 조용히 모델에 전달될 수 있었다. 변수 값이 null 이면 빈 문자열로 치환하고,
 * 변수가 주어지지 않은 플레이스홀더는 그대로 둔다(호출 측이 단계적으로 채울 수 있게).</p>
 *
 * <pre>
 * EgovPromptTemplates templates = EgovPromptTemplates.of(Map.of("context-based", "다음 맥락으로 답하라: {context}"));
 * String prompt = templates.format("context-based", Map.of("context", contextText));
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-core — 샘플 템플릿 매니저의 미등록 키 침묵 교정)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovPromptTemplates {

	/** 플레이스홀더 형식 — 변수명은 영숫자·밑줄·점·하이픈 */
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z0-9_.\\-]+)\\}");

	private final Map<String, String> templates;

	private EgovPromptTemplates(Map<String, String> templates) {
		this.templates = Collections.unmodifiableMap(new LinkedHashMap<>(templates));
	}

	/**
	 * 키→템플릿 맵으로 만든다(복사하므로 이후 원본 맵 변경은 영향 없음).
	 *
	 * @param templates 키→템플릿(null 값 불가)
	 */
	public static EgovPromptTemplates of(Map<String, String> templates) {
		Objects.requireNonNull(templates, "templates must not be null");
		for (Map.Entry<String, String> entry : templates.entrySet()) {
			if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
				throw new IllegalArgumentException("템플릿 키·값은 비어 있을 수 없습니다: " + entry.getKey());
			}
		}
		return new EgovPromptTemplates(templates);
	}

	/** 빈 레지스트리. */
	public static EgovPromptTemplates empty() {
		return new EgovPromptTemplates(Map.of());
	}

	/**
	 * 템플릿 원문.
	 *
	 * @throws IllegalArgumentException 미등록 키
	 */
	public String get(String key) {
		String template = templates.get(key);
		if (template == null) {
			throw new IllegalArgumentException("등록되지 않은 프롬프트 템플릿 키입니다: " + key + " (등록된 키: " + keys() + ")");
		}
		return template;
	}

	/** 등록 여부. */
	public boolean contains(String key) {
		return templates.containsKey(key);
	}

	/**
	 * <code>{변수}</code> 플레이스홀더를 치환한 프롬프트.
	 *
	 * @param key 템플릿 키
	 * @param variables 변수명→값(null 값은 빈 문자열, 없는 변수의 플레이스홀더는 그대로)
	 * @throws IllegalArgumentException 미등록 키
	 */
	public String format(String key, Map<String, String> variables) {
		String template = get(key);
		if (variables == null || variables.isEmpty()) {
			return template;
		}
		// 원본 템플릿의 플레이스홀더만 한 번의 패스로 치환한다 — 값 안의 {다른변수} 는 재해석하지 않는다.
		// (순차 replace 는 사용자 입력에 든 {context} 가 다음 변수 치환에 걸려 다른 변수를 끌어왔다 — CWE-1336, 2026-09-07 보안 재점검)
		Matcher matcher = PLACEHOLDER.matcher(template);
		StringBuilder out = new StringBuilder(template.length() + 64);
		while (matcher.find()) {
			String name = matcher.group(1);
			String replacement = variables.containsKey(name)
					? (variables.get(name) == null ? "" : variables.get(name))
					: matcher.group();
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** 등록된 키 집합(불변, 등록 순서). */
	public Set<String> keys() {
		return templates.keySet();
	}

	/** 등록된 템플릿 수. */
	public int size() {
		return templates.size();
	}
}
