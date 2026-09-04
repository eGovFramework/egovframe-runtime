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
 * {@link EgovFileStore} 가 파일을 저장한 결과를 담는 <b>불변 값</b>.
 *
 * <p><b>NOTE:</b> 애플리케이션은 이 값에서 <b>루트 키와 저장명</b>을 보관해 두었다가
 * 다운로드 시 {@link EgovFileStore#resolve(String, String)} 로 경로를 다시 확정한다.
 * 원본 이름은 화면 표시·{@code Content-Disposition} 용도로만 보관하고 경로에는 쓰지
 * 않는다(저장 경로에는 확장자 외에 원본 이름의 어떤 부분도 들어가지 않는다).</p>
 *
 * <pre>
 * EgovStoredFile stored = store.store(in, file.getOriginalFilename());
 * attachRepository.save(new Attach(stored.getRootKey(), stored.getStoredName(),
 *                                  stored.getOriginalName(), stored.getSize()));
 * </pre>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.02  실행환경팀     최초 생성 (저장 결과 값 객체)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
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

	/** 새로 만든 저장 파일명(루트 바로 아래, 확장자 보존). DB 에 보관하는 값이다. */
	public String getStoredName() {
		return storedName;
	}

	/** 저장 시점에 확정된 절대 경로(정규화됨). */
	public Path getPath() {
		return path;
	}

	/** 호출자가 준 원본 이름 그대로(null 가능). 경로에는 쓰이지 않는다. */
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
