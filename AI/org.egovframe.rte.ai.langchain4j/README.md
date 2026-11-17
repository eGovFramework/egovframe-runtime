# egovframe-rte-ai-langchain4j

`ai.core`의 부품을 **LangChain4j** 확장점에 연결하는 얇은 어댑터입니다. 문장 경계 · 청킹 · 정규화 · 마스킹 · 가드 · 융합 · 실패 분류는 다시 구현하지 않고 `ai.core`를 호출합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ai-langchain4j:5.1.0`
- **Runtime**: Java 17 / LangChain4j 1.18.1
- **의존(compile)**: `egovframe-rte-ai-core`, `langchain4j-core`, `jackson-databind`, `slf4j-api`
- **의존(optional)**: `spring-jdbc`, `spring-tx`(JDBC 구현), `micrometer-observation`(관측)

---

## 특징

- **확장점 중심**: LangChain4j 본체(`langchain4j`)는 `test` 스코프에만 두고, 확장점이 모두 있는 `langchain4j-core`에만 컴파일 의존
- **공통 로직은 `ai.core`**: 이 모듈은 LangChain4j 타입으로 변환하는 층
- **LangChain4j 버전**: `egovframe-boot-starter-parent` 5.1.0이 관리하는 1.18.1과 동일

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ai/langchain4j/
├── etl/            # 문서 로딩 · 분할 · 변환 · 재색인 라이터
├── guardrail/      # 프롬프트 인젝션 가드레일
├── memory/         # JDBC 채팅 메모리 저장소
├── observability/  # Micrometer Observation 브리지
├── rag/            # 하이브리드 검색 · 대화 맥락 질의 변환
└── response/       # 실패 분류 · 폴백
resources/egov/ai/langchain4j/
├── query-compression.txt
└── schema-ansi.sql, schema-hsqldb.sql
```

---

## 주요 기능

| 영역 | 클래스 | 용도 |
|------|--------|------|
| ETL | `EgovDocumentLoader` | 추출기 모음(HWP · HWPX · DOCX · PDF · Markdown)으로 파일을 읽어 LangChain4j `Document`로 변환 |
| ETL | `EgovKoreanSentenceSplitter` | 한국어 문장 경계 청킹을 `DocumentSplitter`로 노출 |
| ETL | `EgovNormalizingTransformer` | 텍스트 정규화(본문이 바뀐 문서만 새 `Document`) |
| ETL | `EgovPiiMaskingTransformer` | 색인용 PII 마스킹 |
| ETL | `EgovEmbeddingStoreWriter` | 같은 원본(`original_id`)의 기존 세그먼트를 지운 뒤 재색인 |
| ETL | `EgovSegmentSizers` / `EgovTruncationPolicy` | 크기 계산기, 청크 한도 도달 시 정책(FAIL 또는 MARK) |
| RAG | `EgovHybridContentRetriever` | 밀집 검색과 어휘 검색(`EgovLexicalSearch`)을 RRF로 융합 |
| RAG | `EgovConversationQueryTransformer` | 대화 맥락 질의 압축(한국어 템플릿, 결과 검증) |
| RAG | `EgovSegmentLookup` / `EgovJdbcSegmentLookup` | 어휘 채널에만 잡힌 세그먼트 본문 조회(pgvector 표 규약) |
| 가드레일 | `EgovInjectionGuardrail` | `InputGuardrail`로 사용자 메시지의 프롬프트 인젝션 판정 |
| 메모리 | `EgovJdbcChatMemoryStore` | `ChatMemoryStore`의 관계형 DB 구현 |
| 관측 | `EgovObservationChatModelListener` | `ChatModelListener` → Micrometer Observation |
| 응답 | `EgovLangChain4jErrorClassifier` / `EgovLangChain4jFallbacks` | LangChain4j 예외 분류 및 폴백 구성 |

---

## 빌드

루트에서 `mvn clean install -pl AI/org.egovframe.rte.ai.core,AI/org.egovframe.rte.ai.langchain4j`로 `ai.core`와 함께 빌드하거나, `ai.core`를 먼저 설치한 뒤 이 모듈 디렉터리에서 빌드합니다.

```bash
# 이 모듈 디렉터리에서 실행
mvn clean package
```

## 테스트

```bash
mvn test
```

## 최소 설정 예시

```xml
<dependency>
  <groupId>org.egovframe.rte</groupId>
  <artifactId>egovframe-rte-ai-langchain4j</artifactId>
  <version>5.1.0</version>
</dependency>
```

LangChain4j 모델 · 임베딩 저장소 구현체는 사용하는 앱이 선택하여 추가합니다.

---

## 참고

- 기반 모듈: [`ai.core`](../org.egovframe.rte.ai.core/README.md)
- Spring AI 어댑터: [`ai.springai`](../org.egovframe.rte.ai.springai/README.md)
- 상위 개요: 저장소 루트 `README.md`
