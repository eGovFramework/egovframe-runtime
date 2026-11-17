# org.egovframe.rte.fdl.filehandling

**Foundation 파일 처리(File Handling) 모듈**은 Apache Commons VFS2 기반 파일 유틸(`EgovFileUtil`)과 NIO.2(`java.nio.file`) 기반 표준 구현(`EgovFiles`), zip 아카이브 생성·해제(`EgovArchives`), 업로드 파일 정책·저장명·시그니처 검사·저장소(`upload` 패키지), 다운로드 `Content-Disposition` 헤더 생성(`EgovContentDispositions`)을 제공합니다. `org.egovframe.rte.fdl.excel`이 이 모듈에 의존합니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.filehandling:4.3.1`
- **Runtime**: Java 1.8 / Commons VFS2 2.10.0 / Commons IO 2.18.0
- **실행환경 의존**: `org.egovframe.rte.fdl.string`

---

## 특징

- **VFS2 유틸**: `EgovFileUtil`이 `VFS.getManager()`로 `FileObject`를 다루며 `rm`, `cp`, `mv`, `touch`, `cd`, `pwd`, `ls`, `grep`, 텍스트 읽기/쓰기 등 셸 명령 스타일 API 제공 (프로세스 전역 기준 디렉터리 사용)
- **NIO.2 표준 구현**: `EgovFiles`는 전역 상태 없이 인자만으로 동작(스레드 안전), 실패는 `UncheckedIOException`으로 전파, 텍스트 I/O 기본 UTF-8, `deleteRecursively`·`listRecursively`·`grepLines` 등 올바른 시맨틱 제공
- **경로 조작 방어**: `EgovFiles.resolveSecurely(baseDir, untrustedRelative)`가 traversal·절대 경로·널 바이트를 차단하고, `tryResolveSecurely`/`isContainedIn`/`requireContainedIn`으로 다중 허용 루트 봉쇄
- **안전한 zip 처리**: `EgovArchives.zipDirectory()`/`unzip()`은 Zip Slip(CWE-22)과 압축 폭탄을 `EgovExtractLimits`(기본 엔트리 100,000개·누적 10 GiB)로 봉쇄. 엔트리명 문자셋 지정 가능(MS949 등). tar는 미지원
- **업로드 보안 부품**: `EgovUploadPolicy`(확장자 화이트리스트 fail-closed, 크기·개수 제한), `EgovStoredFileNames`(UUID 기반 예측 불가 저장명), `EgovFileSignatureUtil`(매직넘버로 확장자-내용 일치 검사: pdf, jpg/jpeg, png, gif, bmp, zip/hwpx/docx/xlsx/pptx/jar, hwp/doc/xls/ppt), `EgovFileStore`(신뢰 루트 위 저장·재검증·삭제 조립)
- **다운로드 헤더**: `EgovContentDispositions.attachment()`/`inline()`이 RFC 6266/5987에 따라 `filename="ASCII 폴백"; filename*=UTF-8''...`를 생성하고 CR/LF 등 제어문자 제거(헤더 인젝션 차단)
- Servlet·Spring에 의존하지 않으므로 웹, 배치, 연계 수신 등 어디서나 같은 규칙을 사용

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/filehandling/
├── EgovContentDispositions.java      # attachment(filename), inline(filename)
├── EgovFileUtil.java                 # Commons VFS2 기반 유틸 (static)
├── EgovFiles.java                    # NIO.2 기반 유틸 (static, final)
├── archive/
│   ├── EgovArchives.java             # zipDirectory, unzip
│   └── EgovExtractLimits.java        # defaults(), of(maxEntries, maxTotalBytes), unlimited()
└── upload/
    ├── EgovFileSignatureUtil.java    # matches, detect, isSupportedExtension
    ├── EgovFileStore.java            # builder().root().policy().build(), store/resolve/delete
    ├── EgovStoredFile.java           # 저장 결과 (rootKey, storedName, path, originalName, size)
    ├── EgovStoredFileNames.java      # generate, generateWithoutExtension, extensionOf
    ├── EgovUploadPolicy.java         # builder(), check/validate, checkCount/validateCount, Reason
    └── EgovUploadRejectedException.java
```

`src/main/resources`는 없으며, 테스트 리소스에 VFS 프로바이더 설정 예시(`META-INF/vfs-providers_sample.xml`)가 있습니다.

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovFileUtil` | VFS2 `FileObject` 기반. `rm(cmd)`, `cp(src, target)`, `mv(src, target)`, `pwd()`, `touch(path)`, `cd(dir)`, `readFile(File[, encoding])`, `readFileContent(InputStream)`, `writeFile(File 또는 fileName, text[, encoding])`, `getContent(FileObject)`, `writeContent`, `copyContent`, `getFileExtension`, `isExistsFile`, `stripFilename`, `delete(File)`, `readTextFile(fileName, newline)`, `getFileObject(path)`, `grep(Object[] 또는 File, pattern)`, `getTmpDirectory()`, `ls(String[])`(인스턴스 메서드) |
| `EgovFiles` | NIO.2 `Path` 기반. `readString`, `readLines`, `writeString`(Charset 오버로드), `copy`, `move`, `deleteRecursively`, `list`, `listRecursively`, `grepLines(file, regex[, charset])`, `touch`, `resolveSecurely`, `tryResolveSecurely`, `isContainedIn`, `requireContainedIn`, `getExtension`, `getBaseName`, `createDirectories` |
| `EgovContentDispositions` | `attachment(filename)`, `inline(filename)`. 파일명만 남기고(경로 구분자 앞 제거) 제어문자 제거, 비ASCII는 RFC 5987 인코딩 + ASCII 폴백 병기 |
| `archive.EgovArchives` | `zipDirectory(sourceDir, zipFile)`(엔트리 수 반환), `unzip(zipFile, targetDir[, entryNameCharset[, EgovExtractLimits]])`. 모든 엔트리를 `resolveSecurely`로 확정 후 생성, 위반 시 `IllegalArgumentException` |
| `archive.EgovExtractLimits` | `DEFAULT_MAX_ENTRIES`(100,000), `DEFAULT_MAX_TOTAL_BYTES`(10 GiB). `defaults()`, `of(maxEntries, maxTotalBytes)`, `unlimited()` |
| `upload.EgovUploadPolicy` | 불변 정책. `builder().allowExtensions(...)`/`allowExtensionList("png,jpg")`/`maxFileSize(long)`/`maxFileCount(int)`/`allowEmptyFile(boolean)`. `check(fileName, size)` → `Optional<Reason>`, `validate(...)` → 위반 시 `EgovUploadRejectedException`. `Reason`: `NO_FILENAME`, `INVALID_FILENAME`, `INVALID_SIZE`, `EMPTY_FILE`, `NO_EXTENSION`, `EXTENSION_NOT_ALLOWED`, `SIZE_EXCEEDED`, `COUNT_EXCEEDED` |
| `upload.EgovStoredFileNames` | `generate(originalFileName[, UUID])` → `<32자리 hex>.<확장자>`, `generateWithoutExtension()`, `extensionOf(name)` |
| `upload.EgovFileSignatureUtil` | `matches(byte[] 또는 InputStream, fileName)`, `detect(byte[])`(판별 불가 시 `null`), `isSupportedExtension(fileName)`, `getSupportedExtensions()`. CFBF 계열(hwp/doc/xls/ppt)은 서로 구분하지 않음 |
| `upload.EgovFileStore` | `builder().root(key, dir)`(첫 루트가 기본)`.policy(policy).keepExtension(boolean).build()`. `store([rootKey,] InputStream 또는 Path, originalName)` → `EgovStoredFile`, `resolve(rootKey, storedName)`/`resolve(storedPath)`/`resolve(EgovStoredFile)`, `delete(...)`, `roots()`, `defaultRootKey()`, `policy()`. 정책이 있으면 실제 기록 바이트 수로 검증하고 위반 시 파일 삭제 후 예외 |
| `upload.EgovStoredFile` | `getRootKey()`, `getStoredName()`, `getPath()`, `getOriginalName()`, `getSize()` |
| `upload.EgovUploadRejectedException` | `RuntimeException`. `getReason()`, `getFileName()` |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.string` | 4.3.1 | compile (commons-logging 제외) |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.apache.commons:commons-vfs2` | 2.10.0 | compile (commons-logging, maven-scm-api, maven-scm-provider-svnexe 제외) |
| `commons-io:commons-io` | 2.18.0 | compile |
| `junit:junit` | 4.13.2 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다.

---

## 설정 및 사용 예시

Spring Bean 설정 없이 static API / 빌더로 사용합니다.

### 1) VFS2 유틸 (`src/test/java/.../FilehandlingServiceTest.java`)

```java
EgovFileUtil.cd(System.getProperty("user.dir"));          // 기준 디렉터리 (전역)
String tmp = EgovFileUtil.getTmpDirectory();
EgovFileUtil.writeFile(tmp + "/testing.txt", "내용", "UTF-8");
boolean exists = EgovFileUtil.isExistsFile(tmp + "/testing.txt");
EgovFileUtil.delete(new File(tmp + "/testing.txt"));

FileSystemManager manager = VFS.getManager();
FileObject baseDir = manager.resolveFile(System.getProperty("user.dir"));
FileObject file = manager.resolveFile(baseDir, "testfolder/file1.txt");
```

### 2) NIO.2 유틸과 경로 조작 방어

```java
Path baseDir = Paths.get("/data/upload");
Path target = EgovFiles.resolveSecurely(baseDir, userSuppliedRelativePath);   // "../" 등은 IllegalArgumentException
EgovFiles.writeString(target, "내용");                                        // UTF-8
List<String> hits = EgovFiles.grepLines(target, "ERROR.*");
long deleted = EgovFiles.deleteRecursively(baseDir.resolve("tmp"));
```

### 3) zip 생성·해제

```java
long entries = EgovArchives.zipDirectory(dataDir, backupDir.resolve("data.zip"));
EgovArchives.unzip(zipFile, restoreDir);                                          // UTF-8, 기본 한도
EgovArchives.unzip(zipFile, restoreDir, Charset.forName("MS949"));                // 구형 윈도우 zip
EgovArchives.unzip(uploaded, workDir, StandardCharsets.UTF_8,
        EgovExtractLimits.of(1_000, 100L * 1024 * 1024));                         // 사용자 업로드: 1,000개·100MB
```

### 4) 업로드 정책·저장소·다운로드 헤더

```java
EgovUploadPolicy policy = EgovUploadPolicy.builder()
        .allowExtensions("png", "jpg", "pdf")
        .maxFileSize(10 * 1024 * 1024L)
        .maxFileCount(5)
        .build();

EgovFileStore store = EgovFileStore.builder()
        .root("store", Paths.get("/data/upload"))     // 첫 루트 = 기본 루트
        .root("image", Paths.get("/data/image"))
        .policy(policy)
        .build();

// 업로드: 이름·크기 정책 + 내용 시그니처 검사 + 저장
policy.validate(fileName, fileSize);
if (!EgovFileSignatureUtil.matches(bytes, fileName)) {
    throw new EgovUploadRejectedException(EgovUploadPolicy.Reason.EXTENSION_NOT_ALLOWED, fileName);
}
EgovStoredFile stored = store.store(inputStream, fileName);
// DB 에는 stored.getRootKey(), getStoredName(), getOriginalName() 보관

// 다운로드: 읽는 시점에 루트 내부인지 재검증
Path verified = store.resolve(attach.getRootKey(), attach.getStoredName());
response.setHeader("Content-Disposition", EgovContentDispositions.attachment(attach.getOriginalName()));
Files.copy(verified, response.getOutputStream());
```

`EgovContentDispositions.attachment("연차보고서.hwp")` →
`attachment; filename="_____.hwp"; filename*=UTF-8''%EC%97%B0%EC%B0%A8...%EC%84%9C.hwp`

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.filehandling -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.filehandling
```

테스트는 로컬 파일 시스템(`user.dir` 아래 `testfolder`, 임시 디렉터리)만 사용하며 외부 서버가 필요 없습니다. `*SecurityTest`는 경로 조작·Zip Slip·헤더 인젝션·정책 fail-closed 동작을 검증합니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.filehandling</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.string](../org.egovframe.rte.fdl.string/README.md), [org.egovframe.rte.fdl.excel](../org.egovframe.rte.fdl.excel/README.md), [org.egovframe.rte.ptl.mvc](../../Presentation/org.egovframe.rte.ptl.mvc/README.md) (MultipartFile 어댑터)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
