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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.egovframe.rte.fdl.filehandling.EgovFiles;

/**
 * 확장자 화이트리스트·파일 크기·파일 개수를 검증하는 불변 업로드 정책.
 * 화이트리스트는 필수(fail-closed)이며, 서블릿·Spring에 의존하지 않는다.
 *
 * <pre>
 * EgovUploadPolicy policy = EgovUploadPolicy.builder().allowExtensions("png", "jpg", "pdf").maxFileSize(10 * 1024 * 1024L).build();
 * policy.validate(fileName, fileSize);                            // 위반 시 예외
 * Optional&lt;Reason&gt; reason = policy.check(fileName, fileSize);   // 예외 없이 판정
 * </pre>
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
public final class EgovUploadPolicy {

	/** 파일 개수를 제한하지 않음을 뜻하는 값. */
	public static final int UNLIMITED_COUNT = -1;

	private final Set<String> allowedExtensions;

	private final long maxFileSize;

	private final int maxFileCount;

	private final boolean emptyFileAllowed;

	private EgovUploadPolicy(Builder builder) {
		this.allowedExtensions = Collections.unmodifiableSet(new LinkedHashSet<>(builder.allowedExtensions));
		this.maxFileSize = builder.maxFileSize;
		this.maxFileCount = builder.maxFileCount;
		this.emptyFileAllowed = builder.emptyFileAllowed;
	}

	/**
	 * 정책 빌더를 반환한다.
	 *
	 * @return 빌더
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 파일 하나를 검증하고 위반 사유를 반환한다(예외 없음).
	 *
	 * @param fileName 파일명(경로가 아닌 <b>이름만</b>)
	 * @param fileSize 바이트 크기
	 * @return 위반 사유. 통과하면 {@link Optional#empty()}
	 */
	public Optional<Reason> check(String fileName, long fileSize) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return Optional.of(Reason.NO_FILENAME);
		}
		if (fileName.indexOf('\0') >= 0 || fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0
				|| fileName.indexOf(':') >= 0) {
			return Optional.of(Reason.INVALID_FILENAME);
		}
		if (fileSize < 0) {
			return Optional.of(Reason.INVALID_SIZE);
		}
		if (fileSize == 0 && !emptyFileAllowed) {
			return Optional.of(Reason.EMPTY_FILE);
		}
		String extension = EgovFiles.getExtension(fileName);
		if (extension.isEmpty()) {
			return Optional.of(Reason.NO_EXTENSION);
		}
		if (!allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
			return Optional.of(Reason.EXTENSION_NOT_ALLOWED);
		}
		if (fileSize > maxFileSize) {
			return Optional.of(Reason.SIZE_EXCEEDED);
		}
		return Optional.empty();
	}

	/**
	 * 파일 하나를 검증하고, 위반이면 예외를 던진다.
	 *
	 * @param fileName 파일명(경로가 아닌 <b>이름만</b>)
	 * @param fileSize 바이트 크기
	 * @throws EgovUploadRejectedException 정책 위반인 경우
	 */
	public void validate(String fileName, long fileSize) {
		check(fileName, fileSize).ifPresent(reason -> {
			throw new EgovUploadRejectedException(reason, fileName);
		});
	}

	/**
	 * 파일 개수를 검증하고 위반 사유를 반환한다(예외 없음).
	 *
	 * @param fileCount 실제 업로드된 파일 개수
	 * @return 위반 사유. 통과하면 {@link Optional#empty()}
	 */
	public Optional<Reason> checkCount(int fileCount) {
		if (maxFileCount != UNLIMITED_COUNT && fileCount > maxFileCount) {
			return Optional.of(Reason.COUNT_EXCEEDED);
		}
		return Optional.empty();
	}

	/**
	 * 파일 개수를 검증하고, 위반이면 예외를 던진다.
	 *
	 * @param fileCount 실제 업로드된 파일 개수
	 * @throws EgovUploadRejectedException 개수 초과인 경우
	 */
	public void validateCount(int fileCount) {
		checkCount(fileCount).ifPresent(reason -> {
			throw new EgovUploadRejectedException(reason, null);
		});
	}

	/**
	 * 허용 확장자 집합을 반환한다(소문자, 점 없음, 불변).
	 *
	 * @return 허용 확장자
	 */
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}

	/**
	 * 파일 하나의 최대 바이트 크기를 반환한다.
	 *
	 * @return 최대 크기
	 */
	public long getMaxFileSize() {
		return maxFileSize;
	}

	/**
	 * 최대 파일 개수를 반환한다({@link #UNLIMITED_COUNT} 이면 제한 없음).
	 *
	 * @return 최대 개수
	 */
	public int getMaxFileCount() {
		return maxFileCount;
	}

	/**
	 * 0 바이트 파일 허용 여부를 반환한다.
	 *
	 * @return 허용하면 {@code true}
	 */
	public boolean isEmptyFileAllowed() {
		return emptyFileAllowed;
	}

	@Override
	public String toString() {
		return "EgovUploadPolicy[extensions=" + allowedExtensions
				+ ", maxFileSize=" + maxFileSize
				+ ", maxFileCount=" + (maxFileCount == UNLIMITED_COUNT ? "unlimited" : maxFileCount)
				+ ", emptyFileAllowed=" + emptyFileAllowed + "]";
	}

	/**
	 * 업로드 거부 사유.
	 */
	public enum Reason {

		/** 파일명이 없거나 공백뿐이다. */
		NO_FILENAME("file name is missing"),

		/** 파일명에 널 바이트, 경로 구분자, 또는 드라이브·대체 데이터 스트림 구분자({@code :})가 들어 있다. */
		INVALID_FILENAME("file name contains a path separator, a colon, or a null byte"),

		/** 크기가 음수다(호출부가 잘못된 값을 넘겼다). */
		INVALID_SIZE("file size is negative"),

		/** 내용이 없는 0 바이트 파일이다. */
		EMPTY_FILE("file is empty"),

		/** 확장자가 없다. */
		NO_EXTENSION("file has no extension"),

		/** 확장자가 허용 목록에 없다. */
		EXTENSION_NOT_ALLOWED("file extension is not allowed"),

		/** 파일 하나의 크기가 한도를 넘었다. */
		SIZE_EXCEEDED("file size exceeds the limit"),

		/** 파일 개수가 한도를 넘었다. */
		COUNT_EXCEEDED("file count exceeds the limit");

		private final String description;

		Reason(String description) {
			this.description = description;
		}

		/**
		 * 사유 설명을 반환한다.
		 *
		 * @return 설명
		 */
		public String getDescription() {
			return description;
		}
	}

	/**
	 * {@link EgovUploadPolicy} 빌더. 잘못된 설정은 생성 시점에 즉시 실패한다.
	 */
	public static final class Builder {

		private final Set<String> allowedExtensions = new LinkedHashSet<>();

		private long maxFileSize = 10L * 1024 * 1024;

		private int maxFileCount = UNLIMITED_COUNT;

		private boolean emptyFileAllowed;

		private Builder() {
		}

		/**
		 * 허용 확장자를 추가한다. 점은 있어도 되고 없어도 되며, 대소문자를 가리지 않는다.
		 *
		 * @param extensions 확장자(예: {@code "png"}, {@code ".PDF"})
		 * @return 빌더
		 */
		public Builder allowExtensions(String... extensions) {
			return allowExtensions(Arrays.asList(extensions));
		}

		/**
		 * 허용 확장자를 추가한다.
		 *
		 * @param extensions 확장자 목록
		 * @return 빌더
		 */
		public Builder allowExtensions(Collection<String> extensions) {
			if (extensions == null) {
				throw new IllegalArgumentException("extensions must not be null");
			}
			for (String extension : extensions) {
				if (extension == null) {
					continue;
				}
				String normalized = extension.trim().toLowerCase(Locale.ROOT);
				if (normalized.startsWith(".")) {
					normalized = normalized.substring(1);
				}
				if (!normalized.isEmpty()) {
					allowedExtensions.add(normalized);
				}
			}
			return this;
		}

		/**
		 * 쉼표로 구분된 확장자 문자열을 파싱해 추가한다(설정 파일 값을 그대로 넘길 때).
		 *
		 * @param commaSeparated 예: {@code ".png,.jpg, pdf"}
		 * @return 빌더
		 */
		public Builder allowExtensionList(String commaSeparated) {
			if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
				return this;
			}
			return allowExtensions(Arrays.asList(commaSeparated.split(",")));
		}

		/**
		 * 파일 하나의 최대 바이트 크기를 지정한다(기본 10MB).
		 *
		 * @param maxFileSize 최대 크기. <b>양수여야 한다</b>
		 * @return 빌더
		 */
		public Builder maxFileSize(long maxFileSize) {
			if (maxFileSize <= 0) {
				throw new IllegalArgumentException("maxFileSize must be positive: " + maxFileSize);
			}
			this.maxFileSize = maxFileSize;
			return this;
		}

		/**
		 * 최대 파일 개수를 지정한다(기본 제한 없음).
		 *
		 * @param maxFileCount 최대 개수. 양수 또는 {@link #UNLIMITED_COUNT}
		 * @return 빌더
		 */
		public Builder maxFileCount(int maxFileCount) {
			if (maxFileCount <= 0 && maxFileCount != UNLIMITED_COUNT) {
				throw new IllegalArgumentException(
						"maxFileCount must be positive or UNLIMITED_COUNT: " + maxFileCount);
			}
			this.maxFileCount = maxFileCount;
			return this;
		}

		/**
		 * 0 바이트 파일을 허용할지 지정한다(기본 거부).
		 *
		 * @param emptyFileAllowed 허용하면 {@code true}
		 * @return 빌더
		 */
		public Builder allowEmptyFile(boolean emptyFileAllowed) {
			this.emptyFileAllowed = emptyFileAllowed;
			return this;
		}

		/**
		 * 정책을 생성한다.
		 *
		 * @return 불변 정책
		 * @throws IllegalStateException 허용 확장자가 하나도 없는 경우(fail-closed)
		 */
		public EgovUploadPolicy build() {
			if (allowedExtensions.isEmpty()) {
				throw new IllegalStateException(
						"at least one allowed extension is required — "
								+ "an empty whitelist would accept every file");
			}
			return new EgovUploadPolicy(this);
		}
	}
}
