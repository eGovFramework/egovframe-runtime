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
package org.egovframe.rte.ai.springai.etl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.egovframe.rte.ai.core.audit.EgovAiAudit;
import org.egovframe.rte.ai.core.pii.EgovIndexPiiMasker;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;

/**
 * 색인용 PII 마스킹 변환기 — 기반 모듈 {@link EgovIndexPiiMasker} 위임.
 *
 * <p>치환이 일어난 문서는 같은 id 로 다시 만들고 {@code pii_masked=true} 를 붙인다. 감사는 <b>{@link #apply(List)} 호출당 1건</b>
 * — 유형별 건수를 합산해 {@code EGOV_AI_AUDIT} 채널에 {@code event=masking} 으로 남기며(문서당 1건이면 대량 색인에서 감사
 * 로그가 폭증), 합계가 0 이면 발신하지 않는다. 원문 값이나 치환 전 문자열은 로그·메타데이터 어디에도 남기지 않는다
 * (A-19 관례: 유형·건수만).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-springai — 어댑터 규범 3: 마스킹 감사는 변환기가 발신)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovPiiMaskingTransformer implements DocumentTransformer {

	private final EgovIndexPiiMasker masker;

	private EgovPiiMaskingTransformer(EgovIndexPiiMasker masker) {
		this.masker = Objects.requireNonNull(masker, "masker must not be null");
	}

	/**
	 * 마스커를 지정한 변환기.
	 *
	 * @param masker 마스커
	 * @return 변환기
	 */
	public static EgovPiiMaskingTransformer of(EgovIndexPiiMasker masker) {
		return new EgovPiiMaskingTransformer(masker);
	}

	/** 기본 마스커(주민번호 형식 검사·카드번호 Luhn·비밀 토큰). */
	public static EgovPiiMaskingTransformer defaults() {
		return of(EgovIndexPiiMasker.builder().build());
	}

	/** 위임 마스커. */
	public EgovIndexPiiMasker masker() {
		return masker;
	}

	@Override
	public List<Document> apply(List<Document> documents) {
		Objects.requireNonNull(documents, "documents must not be null");
		List<Document> out = new ArrayList<>(documents.size());
		Map<EgovIndexPiiMasker.Kind, Integer> totals = new EnumMap<>(EgovIndexPiiMasker.Kind.class);
		boolean anyMasked = false;
		for (Document document : documents) {
			String text = document.getText();
			if (text == null) {
				out.add(document);
				continue;
			}
			EgovIndexPiiMasker.Result result = masker.mask(text);
			if (!result.changed()) {
				out.add(document);
				continue;
			}
			anyMasked = true;
			for (Map.Entry<EgovIndexPiiMasker.Kind, Integer> entry : result.counts().entrySet()) {
				totals.merge(entry.getKey(), entry.getValue() == null ? 0 : entry.getValue(), Integer::sum);
			}
			out.add(EgovDocuments.withText(document, result.text(), Map.of(EgovSpringAiMetadata.PII_MASKED, Boolean.TRUE)));
		}
		if (anyMasked) {
			EgovAiAudit.masking(totals);
		}
		return out;
	}
}
