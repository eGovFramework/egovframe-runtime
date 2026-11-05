# egovframe-rte-ai-springai

`ai.core`의 부품을 **Spring AI** 확장점(DocumentReader / Transformer / Writer, DocumentRetriever, QueryTransformer, Advisor, StructuredOutputConverter 등)에 연결하는 얇은 어댑터입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ai-springai:5.1.0`
- **Runtime**: Java 17 / Spring Framework 6.2.x / Spring AI 1.1.8
- **의존(compile)**: `egovframe-rte-ai-core`, `spring-ai-commons` · `-model` · `-client-chat` · `-rag` · `-vector-store` · `-retry`, `reactor-core`, `slf4j-api`

---

## 특징

- **직접 import 하는 Spring AI 아티팩트 6종만 의존**: 벡터 저장소 백엔드(`spring-ai-*-store`) · 모델 클라이언트 · Spring Boot는 사용하는 앱이 선택하며, 어떤 스코프로도 의존하지 않습니다.
- **LangChain4j 타입 미사용**: 그쪽은 `ai.langchain4j`가 담당합니다.
- **토큰 계산**: Spring AI의 `JTokkitTokenCountEstimator`를 통해서만 사용(직접 의존 없음)
- **Spring Framework 정렬**: Spring AI가 전이하는 Spring 6.2.x를 실행환경 버전으로 고정합니다(소비자에게는 전파되지 않음).

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ai/springai/
├── etl/        # 문서 읽기 · 분할 · 변환 · 재색인 라이터
├── guard/      # 프롬프트 인젝션 어드바이저
├── rag/        # 하이브리드 검색 · 대화 맥락 질의 변환
└── response/   # 출력 변환 · 실패 분류 · 폴백
resources/egov/ai/springai/query-compression.st
```

---

## 주요 기능

| 영역 | 클래스 | 용도 |
|------|--------|------|
| ETL | `EgovDocumentReader` | 추출기 모음(HWP · HWPX · DOCX · PDF · Markdown)으로 파일을 읽어 Spring AI `Document`로 변환 |
| ETL | `EgovKoreanSentenceSplitter` | 한국어 문장 경계 청킹을 `TextSplitter`로 노출 |
| ETL | `EgovNormalizingTransformer` | 텍스트 정규화 |
| ETL | `EgovPiiMaskingTransformer` | 색인용 PII 마스킹 |
| ETL | `EgovVectorStoreWriter` | 같은 원본(`original_id`)의 기존 청크를 지운 뒤 재색인 |
| ETL | `EgovTokenSizers` / `EgovTruncationPolicy` | 토큰 추정기 기반 크기 계산기, 청크 한도 도달 시 정책 |
| RAG | `EgovHybridDocumentRetriever` | 밀집 검색과 어휘 검색(`EgovLexicalSearch`)을 RRF로 융합 |
| RAG | `EgovConversationQueryTransformer` | `CompressionQueryTransformer`를 한국어 템플릿으로 감싸 결과 검증 |
| RAG | `EgovDocumentLookup` | 어휘 채널에만 잡힌 문서 본문 조회 SPI |
| 가드 | `EgovInjectionGuardAdvisor` | 마지막 사용자 메시지를 프롬프트 인젝션 가드로 판정하는 어드바이저 |
| 응답 | `EgovReasoningStrippingOutputConverter` | `<think>…</think>` 제거(옵션: 첫 JSON 객체만 추출) 후 위임 변환 |
| 응답 | `EgovSpringAiErrorClassifier` / `EgovSpringAiFallbacks` | `TransientAiException`을 일시 오류로 분류, 폴백 구성 |

---

## 빌드

루트에서 `mvn clean install -pl AI/org.egovframe.rte.ai.core,AI/org.egovframe.rte.ai.springai`로 `ai.core`와 함께 빌드하거나, `ai.core`를 먼저 설치한 뒤 이 모듈 디렉터리에서 빌드합니다.

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
  <artifactId>egovframe-rte-ai-springai</artifactId>
  <version>5.1.0</version>
</dependency>
```

Spring AI 버전은 `egovframe-rte-root` 5.1.0(루트 `properties`)이 관리하며, `egovframe-boot-starter-parent` · `egovframe-web-config-parent`와 동일한 값입니다. 모델 클라이언트 · 벡터 저장소는 앱에서 추가합니다.

---

## 참고

- 기반 모듈: [`ai.core`](../org.egovframe.rte.ai.core/README.md)
- LangChain4j 어댑터: [`ai.langchain4j`](../org.egovframe.rte.ai.langchain4j/README.md)
- 상위 개요: 저장소 루트 `README.md`
