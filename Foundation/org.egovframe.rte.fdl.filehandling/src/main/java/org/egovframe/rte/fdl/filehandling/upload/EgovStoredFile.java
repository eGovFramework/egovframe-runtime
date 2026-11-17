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

import java.nio.file.Path;
import java.util.Objects;

/**
 * {@link EgovFileStore}의 저장 결과를 담는 불변 값 객체.
 * 루트 키와 저장명을 보관했다가 다운로드 시 {@link EgovFileStore#resolve(String, String)}로 경로를 재확정한다.
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 4.3.1
 */
public final class EgovStoredFile {

	private final String rootKey;
	private final String storedName;
	private final Path path;
	private final String originalName;
	private final long size;

	EgovStoredFile(String rootKey, String storedName, Path path, String originalName, long size) {
		this.rootKey = Objects.requireNonNull(rootKey, "rootKey must not be null");
		this.storedName = Objects.requireNonNull(storedName, "storedName must not be null");
		this.path = Objects.requireNonNull(path, "path must not be null");
		this.originalName = originalName;
		this.size = size;
	}

	/** 저장된 허용 루트의 키. */
	public String getRootKey() {
		return rootKey;
	}

	/** 새로 만든 저장 파일명(확장자 보존). */
	public String getStoredName() {
		return storedName;
	}

	/** 저장 시점에 확정된 절대 경로(정규화됨). */
	public Path getPath() {
		return path;
	}

	/** 호출자가 준 원본 이름(null 가능). */
	public String getOriginalName() {
		return originalName;
	}

	/** 실제로 기록된 바이트 수. */
	public long getSize() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EgovStoredFile)) {
			return false;
		}
		EgovStoredFile that = (EgovStoredFile) o;
		return size == that.size && rootKey.equals(that.rootKey) && storedName.equals(that.storedName)
				&& path.equals(that.path) && Objects.equals(originalName, that.originalName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rootKey, storedName, path, originalName, size);
	}

	@Override
	public String toString() {
		return "EgovStoredFile[root=" + rootKey + ", storedName=" + storedName + ", path=" + path
				+ ", originalName=" + originalName + ", size=" + size + "]";
	}
}
