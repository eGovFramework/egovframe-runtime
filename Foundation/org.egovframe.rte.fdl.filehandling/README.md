# egovframe-rte-fdl-filehandling

**Apache Commons VFS2**를 활용해 로컬·URL 등 다양한 **파일 시스템을 통합 추상화**하고, 업로드/다운로드·경로 처리에 필요한 유틸을 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-filehandling:5.0.0`
- **Runtime**: Java 17
- **의존**: `egovframe-rte-fdl-string`, `commons-vfs2`, `commons-io`, `commons-lang3`

---

## 특징

- **VFS 기반**: `file:`, `http` 등 스킴을 동일 API로 다루기 쉬움
- **문자열 유틸 연동**: `fdl.string` 의존

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/filehandling/
└── EgovFileUtil.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovFileUtil` | 파일 복사·삭제·존재 여부·크기·경로 정규화, VFS `FileObject` 활용 등 |

구체 시그니처는 소스 및 JavaDoc을 참고하세요.

---

## 빌드

```bash
# 이 모듈 디렉터리에서 실행
mvn clean package
```

## 테스트

```bash
# 단위/통합 테스트 실행
mvn test
```

## 최소 설정 예시

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-fdl-filehandling</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
