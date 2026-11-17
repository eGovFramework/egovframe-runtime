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
 * 거부 사유({@link EgovUploadPolicy.Reason})를 담으며, 메시지에는 파일명을 넣지 않는다(주입 방지).
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
 */
public class EgovUploadRejectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final EgovUploadPolicy.Reason reason;

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
	 * 거부된 파일명을 반환한다. 화면 출력 시 인코딩을 거친다.
	 *
	 * @return 파일명({@code null} 일 수 있다)
	 */
	public String getFileName() {
		return fileName;
	}
}
