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
package org.egovframe.rte.ai.langchain4j.etl;

/**
 * 청크 수 한도에 걸려 본문 일부가 분할되지 않았을 때({@link EgovTruncationPolicy#FAIL}) 던지는 예외.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public class EgovChunkLimitException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String originalId;
	private final int maxChunks;

	/**
	 * @param originalId 문서 원본 id(메타데이터에 없으면 null)
	 * @param maxChunks 청크 수 한도
	 */
	public EgovChunkLimitException(String originalId, int maxChunks) {
		super("문서 " + (originalId == null ? "(original_id 없음)" : "'" + originalId + "'") + " 의 세그먼트 수가 한도 " + maxChunks
				+ " 에 도달해 본문 일부가 분할되지 않았습니다. maxChunks 를 올리거나, 잘린 채 계속하려면 "
				+ "onTruncation(EgovTruncationPolicy.MARK) 를 명시하세요");
		this.originalId = originalId;
		this.maxChunks = maxChunks;
	}

	/** 원본 id(없으면 null). */
	public String originalId() {
		return originalId;
	}

	/** 청크 수 한도. */
	public int maxChunks() {
		return maxChunks;
	}
}
