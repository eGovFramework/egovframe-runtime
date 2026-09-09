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
package org.egovframe.rte.fdl.filehandling.upload;

/**
 * 업로드 정책 위반으로 파일이 거부되었음을 알리는 예외.
 *
 * <p><b>NOTE:</b> 거부 <b>사유</b>({@link EgovUploadPolicy.Reason})를 담는다. 호출부가
 * 사유별로 다른 응답을 낼 수 있도록 하기 위해서다 — 크기 초과는 413, 확장자 불허는 400 처럼
 * 구분이 필요한 경우가 있다.</p>
 *
 * <p>메시지에는 <b>파일명을 넣지 않는다.</b> 사용자가 지어 보낸 값이 그대로 응답·로그에
 * 실리면 그 자체가 주입 경로가 되기 때문이다. 파일명이 필요하면 {@link #getFileName()} 으로
 * 꺼내 쓰되, 화면에 낼 때는 출력 인코딩을 거친다
 * ({@code fdl.string} 의 {@code EgovOutputEncoder}).</p>
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
 * @since 5.1
 */
public class EgovUploadRejectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final transient EgovUploadPolicy.Reason reason;

	private final String fileName;

	/**
	 * @param reason   거부 사유
	 * @param fileName 거부된 파일명(로그·응답 조립용, {@code null} 허용)
	 */
	public EgovUploadRejectedException(EgovUploadPolicy.Reason reason, String fileName) {
		super("Upload rejected: " + reason.name() + " - " + reason.getDescription());
		this.reason = reason;
		this.fileName = fileName;
	}

	/**
	 * 거부 사유를 반환한다.
	 *
	 * @return 사유
	 */
	public EgovUploadPolicy.Reason getReason() {
		return reason;
	}

	/**
	 * 거부된 파일명을 반환한다. <b>화면에 낼 때는 출력 인코딩을 거칠 것.</b>
	 *
	 * @return 파일명({@code null} 일 수 있다)
	 */
	public String getFileName() {
		return fileName;
	}
}
