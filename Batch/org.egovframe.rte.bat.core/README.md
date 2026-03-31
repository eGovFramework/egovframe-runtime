# egovframe-rte-bat-core

전자정부 표준프레임워크 **배치** 확장 라이브러리입니다. **Spring Batch 5.x**를 기반으로 대용량 파일·DB 연동, 복합 Reader, 잡/스텝 리스너, 배치 실행 진입점을 제공합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-bat-core:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x / Spring Batch 5.x
- **연관 모듈**: `egovframe-rte-fdl-cmmn`, `egovframe-rte-psl-dataaccess` (및 테스트용 `egovframe-rte-fdl-idgnr`)

---

## 특징

- **Spring Batch 표준 확장**: `ItemReader` / `ItemWriter` / `LineMapper` / `Tokenizer` 계열을 Egov 네이밍으로 래핑·보완
- **레거시 연계 파일 형식**: 고정 길이·구분자(이스케이프)·바이트 단위, 인덱스 파일 Reader/Writer
- **DB·MyBatis 연동**: 페이징 Reader, Batch ItemWriter, `PreparedStatement` 세터 유틸
- **운영 편의**: Job/Step/Chunk 전후 리스너, 출력 파일 리스너, 변수 리스너, 커맨드라인·스케줄 실행기
- **기타**: Flow `Decider`, 이벤트 알림 트리거, 셸/삭제 Tasklet 지원

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/bat/
├── exception/
│   └── EgovBatchException.java
├── item/
│   ├── DefaultItemReader.java
│   └── DefaultItemWriter.java
├── support/
│   ├── EgovJobVariableListener.java
│   ├── EgovStepVariableListener.java
│   └── EgovResourceVariable.java
└── core/
    ├── event/                    # 배치 이벤트 알림
    ├── job/flow/               # EgovDecider
    ├── launch/support/         # EgovBatchRunner, CommandLine, Scheduler Runner
    ├── listener/               # Job/Step/Chunk, 출력 파일 리스너
    ├── reflection/             # EgovReflectionSupport
    ├── step/                   # Tasklet (셸, 삭제)
    └── item/
        ├── file/               # 바이트/플랫 파일, 인덱스, 파티션 Writer
        │   ├── mapping/        # LineMapper, ObjectMapper
        │   └── transform/      # Tokenizer, Aggregator, FieldExtractor
        ├── database/           # JdbcBatchItemWriter, MyBatis Reader/Writer
        │   └── support/        # PreparedStatement 세터
        └── composite/          # 복합 Reader·Mapper·Provider
            └── reader/         # 페이징·커서·파일 복합 Reader
```

---

## 주요 기능 (패키지별)

### `core/item/file`

| 영역 | 설명 |
|------|------|
| **Tokenizer** | `EgovDelimitedLineTokenizer`, `EgovEscapableDelimitedLineTokenizer`, 고정 길이/바이트 (`EgovFixedLengthTokenizer`, `EgovFixedByteTokenizer` 등) |
| **Mapper** | `EgovDefaultLineMapper`, `EgovByteLineMapper`, `EgovObjectMapper` |
| **Reader/Writer** | `EgovFlatFileByteReader`, `EgovIndexFileReader` / `EgovIndexFileWriter`, `EgovPartitionFlatFileItemWriter` |
| **Factory** | `EgovByteReaderFactory` |

### `core/item/database`

- `EgovMyBatisPagingItemReader`, `EgovMyBatisBatchItemWriter`
- `EgovJdbcBatchItemWriter`, `EgovItemPreparedStatementSetter`, `EgovMethodMapItemPreparedStatementSetter`

### `core/item/composite`

- `EgovCompositePagingReader`, `EgovCompositeCursorReader`, `EgovCompositeFileReader`
- `EgovCompositeItemMapper`, `EgovItemsMapper`, `EgovCompositeDataProvider`

### `core/listener`

- **Job**: `EgovJobPreProcessor`, `EgovJobPostProcessor`
- **Step**: `EgovStepPreProcessor`, `EgovStepPostProcessor`
- **Chunk**: `EgovChunkPreProcessor`, `EgovChunkPostProcessor`
- **기타**: `EgovOutputFileListener`

### `core/launch/support`

- `EgovBatchRunner` — 배치 기동 헬퍼
- `EgovCommandLineRunner` — CLI 실행
- `EgovSchedulerRunner`, `EgovSchedulerJCRunner` — 스케줄 연동

### `core/step`

- `TaskletShellStep`, `TaskletDeleteStep`, `ShellScriptSupport`

### `support`

- `EgovJobVariableListener`, `EgovStepVariableListener` — 잡/스텝 실행 컨텍스트 변수
- `EgovResourceVariable`

### `exception`

- `EgovBatchException` — 배치 공통 예외

---

## 의존성 요약

- Spring Batch (`spring-batch-core`, `infrastructure`, `integration`, `test`)
- Spring (`jdbc`, `tx`, `orm`, `oxm`, `webmvc`, `messaging`, `context-support` 등)
- `egovframe-rte-fdl-cmmn`, `egovframe-rte-psl-dataaccess`
- 테스트: HSQLDB, Quartz(테스트 스코프), Jakarta 메일/서블릿/검증(API) 등

자세한 버전과 의존성 목록은 루트 및 본 모듈 `pom.xml`을 참고하세요.

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
  <artifactId>egovframe-rte-bat-core</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- [전자정부 표준프레임워크](https://www.egovframe.go.kr)
- 상위 프로젝트 개요: 저장소 루트 `README.md`
