# egovframe-rte-fdl-excel

**Apache POI**·**jxls-poi** 기반 **엑셀 업로드/다운로드·템플릿 렌더링**과 Spring MVC **AbstractExcelView** 계열을 제공합니다. `psl.dataaccess`·`fdl.filehandling`·`fdl.cmmn`과 연동해 DB 매핑 엑셀 처리가 가능합니다.

- **Artifact**: `org.egovframe.rte:egovframe-rte-fdl-excel:5.0.0`
- **Runtime**: Java 17 / Spring Framework 6.x / Jakarta Servlet 6.x

---

## 특징

- **대용량 시트**: POI 스트리밍·워크북 API 활용
- **템플릿**: jxls로 양식 기반 리포트 출력
- **웹 연동**: `AbstractExcelView`, `AbstractPOIExcelView`로 MVC 응답 바이너리 생성
- **매퍼/DAO**: `EgovExcelServiceMapper`, `EgovExcelServiceDAO`로 SQL 매퍼와 결합

---

## 소스 디렉터리 구조

```
org/egovframe/rte/fdl/excel/
├── EgovExcelService.java
├── EgovExcelMapping.java
├── impl/
│   ├── EgovExcelServiceImpl.java
│   ├── EgovExcelServiceMapper.java
│   └── EgovExcelServiceDAO.java
└── util/
    ├── EgovExcelUtil.java
    ├── AbstractExcelView.java
    └── AbstractPOIExcelView.java
```

---

## 주요 기능

| 클래스 | 설명 |
|--------|------|
| `EgovExcelService` / `Impl` | 엑셀 읽기·쓰기 비즈니스 API |
| `EgovExcelUtil` | 셀·시트·스타일 등 저수준 헬퍼 |
| `AbstractExcelView` / `AbstractPOIExcelView` | Spring MVC Excel 뷰 베이스 |
| `EgovExcelMapping` | 컬럼-필드 매핑 메타 |

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
  <artifactId>egovframe-rte-fdl-excel</artifactId>
  <version>5.0.0</version>
</dependency>
```

---

## 참고

- 상위 개요: 저장소 루트 `README.md`
