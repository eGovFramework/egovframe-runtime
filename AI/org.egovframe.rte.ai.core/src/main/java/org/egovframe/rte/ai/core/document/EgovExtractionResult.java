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
package org.egovframe.rte.ai.core.document;

import java.nio.file.Path;
import java.util.List;

/**
 * 일괄 추출 결과 — 성공 문서 목록과 파일별 실패(사유·원인) 목록, 본문 없어 제외된 파일 목록.
 *
 * <p>부분 실패를 성공으로 보고하지 않는다: 호출 측은 {@link #hasFailures()} 를 보고 색인 완료 여부를 결정한다.</p>
 *
 * @param succeeded 성공 문서(파일 순서)
 * @param failed 실패 목록
 * @param empty 본문이 없어 제외된 파일
 * @author 실행환경 개발팀
 * @since 5.1
 */
public record EgovExtractionResult(List<EgovExtractedDocument> succeeded, List<Failure> failed, List<Path> empty) {

	/** 파일별 실패 — 파일·사유·원인. */
	public record Failure(Path file, String reason, Throwable cause) {
	}

	public EgovExtractionResult {
		succeeded = List.copyOf(succeeded);
		failed = List.copyOf(failed);
		empty = List.copyOf(empty);
	}

	/** 실패가 하나라도 있는가. */
	public boolean hasFailures() {
		return !failed.isEmpty();
	}
}
