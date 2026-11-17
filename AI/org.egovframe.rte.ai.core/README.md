# egovframe-rte-ai-core

Spring AI · LangChain4j 타입을 **참조하지 않는** AI 영역의 프레임워크 중립 기반 모듈입니다. 두 어댑터(`ai.springai`, `ai.langchain4j`)가 이 모듈의 부품을 호출만 하고 다시 구현하지 않도록 하는 단일 원천입니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-ai-core:5.1.0`
- **Runtime**: Java 17 / Spring Framework 6.2.x
- **의존(compile)**: `slf4j-api` (다른 실행환경 모듈에 의존하지 않음)
- **의존(optional)**: `hwplib`, `hwpxlib`, `poi-ooxml`, `commons-compress`, `pdfbox`, `jedis`, `spring-jdbc`, `spring-tx`

---

## 특징

- **프레임워크 중립**: 어댑터에 넘기는 것은 함수형 인터페이스와 값 타입뿐
- **optional 의존**: 문서 파서 · 어휘 검색 클라이언트는 사용하는 모듈이 자기 pom에 직접 선언합니다. 라이브러리가 없으면 좌표를 담은 예외로 알립니다(침묵 폴백 없음).
- **보수적 · 결정론적 처리**: 정규화는 글자를 지우지 않고, 문장 경계 판정은 같은 입력에 같은 결과를 냅니다.

---

## 소스 디렉터리 구조

```
org/egovframe/rte/ai/core/
├── audit/       # AI 감사 채널
├── document/    # 문서 텍스트 추출 (HWP · HWPX · DOCX · PDF · Markdown)
├── guard/       # 프롬프트 인젝션 가드
├── ingest/      # 재색인용 문서 해시 저장소
├── pii/         # 색인 단계 개인정보 마스킹
├── prompt/      # 프롬프트 템플릿 레지스트리
├── response/    # 모델 출력 정리 · 실패 분류 · 폴백
├── retrieval/   # 어휘 검색 SPI · RRF 융합 · 백엔드(pg_trgm, RediSearch)
└── text/        # 정규화 · 한국어 문장 경계 · 청킹
resources/egov/ai/
├── guard/injection-lexicon.txt
├── ingest/schema-ansi.sql, schema-hsqldb.sql
└── retrieval/schema-postgresql-pg_trgm.sql
```

---

## 주요 기능

### 문서 추출 (`document`)

| 클래스 | 용도 | 필요 라이브러리 |
|--------|------|-----------------|
| `EgovTextExtractor` / `EgovTextExtractors` | 추출기 SPI와 파일명 기반 레지스트리, 일괄 추출 | - |
| `EgovHwpTextExtractor` | HWP(5.0 이진) 텍스트 추출, 문단 · 표 구조 보존 | `hwplib` |
| `EgovHwpxTextExtractor` | HWPX 텍스트 추출 | `hwpxlib` |
| `EgovDocxTextExtractor` | DOCX 텍스트 추출 | `poi-ooxml` |
| `EgovPdfTextExtractor` | PDF 텍스트 추출(페이지 단위) | `pdfbox` 3.x |
| `EgovMarkdownTextExtractor` | Markdown 텍스트 추출, 구조 메타데이터 | - |
| `EgovExtractionBudget` | 파일 바이트 · PDF 페이지 · 추출 문자 수 · zip 한도 | - |
| `EgovZipDocumentGuard` | zip 기반 문서의 DOCTYPE(XXE) · 압축 폭탄 사전 차단 | `commons-compress` |
| `EgovExtractedDocument` / `EgovDocumentMetadata` / `EgovDocumentIds` | 중립 문서 값, 메타데이터 키 규약, 안정 식별자 | - |

### 텍스트 처리 (`text`)

| 클래스 | 용도 |
|--------|------|
| `EgovTextNormalizer` | 색인 전 정규화(글자를 지우지 않음) |
| `EgovKoreanSentences` | 한국어 문장 경계 판정 |
| `EgovSentenceChunker` | 문장 경계를 보존하는 청킹(크기 계산기 주입) |

### 색인 · 개인정보 (`ingest`, `pii`)

| 클래스 | 용도 |
|--------|------|
| `EgovDocumentHashStore` | 문서별 본문 해시 저장 SPI(변경 문서만 재색인) |
| `EgovInMemoryDocumentHashStore` | 메모리 구현(단일 인스턴스 · 개발 · 테스트) |
| `EgovJdbcDocumentHashStore` | JDBC 구현(`spring-jdbc` 필요, 스키마는 `resources/egov/ai/ingest`) |
| `EgovContentHash` | UTF-8 바이트의 SHA-256 해시 |
| `EgovIndexPiiMasker` | 색인 단계에서 유출 위험이 큰 식별자만 토큰으로 치환 |
| `EgovRrnRules` | 주민등록번호 값 규칙(형식 검증 / 레거시 검증번호) |
| `EgovLuhn` | 카드번호 후보 검증(Luhn) |

### 검색 (`retrieval`)

| 클래스 | 용도 |
|--------|------|
| `EgovLexicalSearch` | 어휘 검색 SPI(질의 → 문서 식별자 순위) |
| `EgovPgTrgmLexicalSearch` | PostgreSQL `pg_trgm` 백엔드(`spring-jdbc`) |
| `EgovRediSearchLexicalSearch` / `EgovRediSearchQueries` | RediSearch `FT.SEARCH` 백엔드(`jedis`) |
| `EgovRrfFusion` | RRF(Reciprocal Rank Fusion)로 여러 검색 채널 순위 융합 |

### 가드 · 응답 · 기타

| 클래스 | 용도 |
|--------|------|
| `EgovPromptInjectionGuard` / `EgovInjectionLexicon` / `GuardPolicy` / `GuardDecision` | 렉시콘 기반 프롬프트 인젝션 판정(LOG 또는 BLOCK) |
| `EgovAiErrorType` / `EgovAiErrorClassifier` / `EgovAiFallback` | 모델 호출 실패 유형 통일 · 분류 SPI · 폴백 문구 정책 |
| `EgovModelOutputs` | 추론 태그(`<think>`) 제거 및 JSON 객체 추출 |
| `EgovPromptTemplates` | 키 기반 프롬프트 템플릿, `{변수}` 치환 |
| `EgovAiAudit` | `EGOV_AI_AUDIT` 감사 채널(`키=값` 한 줄 형식) |

---

## 빌드

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
  <artifactId>egovframe-rte-ai-core</artifactId>
  <version>5.1.0</version>
</dependency>

<!-- 사용하는 기능의 optional 라이브러리만 직접 추가 (예: PDF) — 버전은 egovframe-rte-root 가 관리 -->
<dependency>
  <groupId>org.apache.pdfbox</groupId>
  <artifactId>pdfbox</artifactId>
</dependency>
```

---

## 참고

- 어댑터: [`ai.springai`](../org.egovframe.rte.ai.springai/README.md), [`ai.langchain4j`](../org.egovframe.rte.ai.langchain4j/README.md)
- 상위 개요: 저장소 루트 `README.md`
