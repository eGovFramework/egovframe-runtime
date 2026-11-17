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

/**
 * optional 파서 라이브러리 존재 확인 — 없으면 좌표를 담은 {@link EgovExtractionException}(침묵 폴백 금지).
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
final class EgovOptionalLibraries {

	private EgovOptionalLibraries() {
	}

	/**
	 * @param feature 기능 이름(메시지용, 예: "HWP 텍스트 추출")
	 * @param probeClass 라이브러리의 대표 클래스 FQN
	 * @param coordinates Maven 좌표(예: kr.dogfoot:hwplib)
	 */
	static void require(String feature, String probeClass, String coordinates) {
		try {
			Class.forName(probeClass, false, EgovOptionalLibraries.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError e) {
			throw EgovExtractionException.missingLibrary(feature, coordinates, e);
		}
	}

	/** 존재 여부(예외 없이). */
	static boolean present(String probeClass) {
		try {
			Class.forName(probeClass, false, EgovOptionalLibraries.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
	}
}
