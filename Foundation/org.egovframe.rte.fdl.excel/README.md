# org.egovframe.rte.fdl.excel

**Foundation 엑셀(Excel) 모듈**은 Apache POI 기반으로 엑셀 파일(xls/xlsx)을 생성·적재하고, 업로드한 엑셀의 행을 VO로 매핑하여 MyBatis(`SqlSessionTemplate`) 또는 iBatis(`SqlMapClient`)로 일괄 INSERT 하는 `EgovExcelService`와, Spring MVC에서 xlsx 다운로드 응답을 만드는 `AbstractPOIExcelView`를 제공합니다. JXLS 템플릿 엔진(`jxls`, `jxls-poi`, `jxls-reader`)을 의존성으로 함께 제공합니다. 실행환경의 다른 모듈은 이 모듈에 의존하지 않습니다.

- **Artifact**: `org.egovframe.rte:org.egovframe.rte.fdl.excel:4.3.1`
- **Runtime**: Java 1.8 / Spring Framework 5.3.39-egov.1 / Apache POI 5.4.0 / JXLS 2.14.0 (jxls-reader 2.1.0)
- **실행환경 의존**: `org.egovframe.rte.fdl.cmmn`, `org.egovframe.rte.psl.dataaccess`, `org.egovframe.rte.fdl.filehandling` (`fdl.string`은 전이)

---

## 특징

- **워크북 생성/적재**: `createWorkbook()`으로 디렉터리 생성 후 파일 저장, `loadWorkbook()`/`loadExcelTemplate()`으로 HSSF(xls) 또는 XSSF(xlsx) 워크북 적재
- **엑셀 업로드 → DB 일괄 입력**: `uploadExcel(queryId, ...)`가 시트의 행을 `EgovExcelMapping.mappingColumn(Row)`으로 VO로 변환한 뒤 `commitCnt` 단위로 나누어 `batchInsert` 수행. 시트는 인덱스 또는 이름으로 선택 가능, `start` 행부터 처리
- **MyBatis / iBatis 양쪽 지원**: `sqlSessionTemplate` 설정 시 `EgovExcelServiceMapper`, `sqlMapClient` 설정 시 `EgovExcelServiceDAO`(iBatis 배치) 사용. 둘 다 없으면 `error.excel.persistence.error` 메시지의 예외
- **매핑 클래스 지정**: `mapBeanName`(Spring Bean) 또는 `mapClass`(클래스명, `EgovObjectUtil.instantiate`)로 `EgovExcelMapping` 구현체 지정. 둘 다 없으면 `error.excel.property.error`
- **xlsx 다운로드 뷰**: `AbstractPOIExcelView.buildExcelDocument()`만 구현하면 `XSSFWorkbook`을 생성해 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`로 응답. 파일명은 `model`/`request`의 `filename` 속성(없으면 클래스 단순명) + `.xlsx`
- **보안 처리**: `Content-Disposition` 파일명은 `EgovContentDispositions.attachment()`로 제어문자 제거·RFC 5987 인코딩(헤더 인젝션 차단), `setText()`는 `=`, `+`, `-`, `@`, 탭, CR로 시작하는 값 앞에 `'`를 붙여 수식 인젝션(CWE-1236) 방지
- **셀 값 유틸**: `EgovExcelUtil.getValue(Cell)`이 BOOLEAN/FORMULA/NUMERIC(날짜는 `yyyy/MM/dd`)/STRING/BLANK 타입을 문자열로 변환

---

## 소스 디렉터리 구조

```
src/main/java/org/egovframe/rte/fdl/excel/
├── EgovExcelMapping.java              # abstract Object mappingColumn(Row row)
├── EgovExcelService.java              # 서비스 인터페이스
├── impl/
│   ├── EgovExcelServiceImpl.java      # 구현체 (ApplicationContextAware)
│   ├── EgovExcelServiceMapper.java    # MyBatis 배치 입력 (EgovAbstractMapper 상속)
│   └── EgovExcelServiceDAO.java       # iBatis 배치 입력 (SqlMapClientDaoSupport 상속)
└── util/
    ├── AbstractPOIExcelView.java      # Spring MVC AbstractView 기반 xlsx 뷰
    ├── EgovExcelUtil.java             # getValue(Cell), doubleToString(double)
    └── FormulaInjectionSanitizer.java # 패키지 내부용 수식 인젝션 방지

src/main/resources/org/egovframe/rte/fdl/excel/messages/
├── excel.properties                   # error.excel.runtime.error / property.error / persistence.error
├── excel_en.properties
└── excel_ko.properties
```

---

## 주요 클래스

| 클래스 | 설명 |
|---|---|
| `EgovExcelService` | `createWorkbook(Workbook, filepath)`, `loadExcelTemplate(templateName[, XSSFWorkbook])`, `loadWorkbook(filepath 또는 InputStream[, XSSFWorkbook])`, `uploadExcel(queryId, Sheet, start, commitCnt)`, `uploadExcel(queryId, InputStream[, sheetIndex 또는 sheetName][, start, commitCnt][, XSSFWorkbook])`. `XSSFWorkbook` 인자가 있는 메서드는 xlsx, 없으면 xls(HSSF) 처리 |
| `impl.EgovExcelServiceImpl` | 프로퍼티 `mapClass`, `mapBeanName`, `sqlMapClient`, `sqlSessionTemplate`. 메시지는 `org/egovframe/rte/fdl/excel/messages/excel` 번들 사용. I/O 오류는 `BaseRuntimeException`으로 전환 |
| `impl.EgovExcelServiceMapper` | `batchInsert(queryId, list[, start])` : `insert()`를 반복 호출하고 건수 합산 (`SqlSessionTemplate`은 `ExecutorType.BATCH` 권장) |
| `impl.EgovExcelServiceDAO` | `batchInsert(queryId, list[, start])` : `startBatch()`/`executeBatch()` 기반 iBatis 배치 |
| `EgovExcelMapping` | 업로드 행(`Row`) → VO 변환 추상 클래스. 사용자가 구현 |
| `util.AbstractPOIExcelView` | `buildExcelDocument(model, XSSFWorkbook, request, response)` 추상 메서드, `getCell(sheet, row, col)`(없으면 생성), `setText(cell, text)` 제공 |
| `util.EgovExcelUtil` | `getValue(Cell)`, `doubleToString(double)`(정수면 소수점 제거) |

---

## 주요 의존성

| 라이브러리 | 버전 | scope/비고 |
|---|---|---|
| `org.egovframe.rte:org.egovframe.rte.fdl.cmmn` / `psl.dataaccess` / `fdl.filehandling` | 4.3.1 | compile |
| `spring-webmvc` | 5.3.39-egov.1 | compile |
| `javax.servlet:javax.servlet-api` | 4.0.1 | provided |
| `javax.annotation:javax.annotation-api` | 1.3.2 | compile |
| `org.apache.logging.log4j:log4j-core` | 2.25.5 | compile |
| `commons-beanutils:commons-beanutils` / `commons-io:commons-io` | 1.11.0 / 2.18.0 | compile |
| `org.apache.poi:poi`, `poi-ooxml` | 5.4.0 | compile |
| `org.jxls:jxls`, `jxls-poi` / `jxls-reader` | 2.14.0 / 2.1.0 | compile |
| `commons-logging:commons-logging` | 1.3.5 | 모듈 POM `dependencyManagement` (commons-dbcp2 test 전이 의존성 고정) |
| `commons-dbcp2`, `hsqldb`(`jdk8`), `mysql-connector-j`, `jackson-databind`, `spring-test`, `junit`, `easymock` | 2.13.0 / 2.7.3 / 8.4.0 / 2.18.11 / 5.3.39-egov.1 / 4.13.2 / 5.2.0 | test |

버전은 루트 POM(`org.egovframe.rte.root` 4.3.1)의 `dependencyManagement`에서 관리하며 모듈 POM은 버전을 선언하지 않습니다 (위 `commons-logging` 제외).

---

## 설정 및 사용 예시

### 1) 서비스 Bean 설정 (`src/test/resources/META-INF/spring/context-excel.xml`)

```xml
<!-- MyBatis -->
<bean id="excelService" class="org.egovframe.rte.fdl.excel.impl.EgovExcelServiceImpl">
    <property name="mapClass" value="org.egovframe.rte.fdl.excel.upload.EgovExcelTestMapping"/>
    <property name="sqlSessionTemplate" ref="sqlSessionTemplate"/>
</bean>

<!-- iBatis, 매핑 클래스를 Bean 으로 지정 -->
<bean id="excelBigService" class="org.egovframe.rte.fdl.excel.impl.EgovExcelServiceImpl">
    <property name="mapBeanName" value="mappingBean"/>
    <property name="sqlMapClient" ref="sqlMapClient"/>
</bean>
<bean id="mappingBean" class="org.egovframe.rte.fdl.excel.upload.EgovExcelBigTestMapping"/>
```

배치 입력용 `SqlSessionTemplate` (`context-mybatis.xml`):

```xml
<bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
    <constructor-arg index="0" ref="sqlSession"/>
    <constructor-arg index="1" value="BATCH"/>
</bean>
```

### 2) 업로드 매핑 클래스 (`src/test/java/.../upload/EgovExcelTestMapping.java`)

```java
public class EgovExcelTestMapping extends EgovExcelMapping {
    @Override
    public EmpVO mappingColumn(Row row) {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(row.getCell(0).getNumericCellValue()));
        vo.setEmpName(EgovExcelUtil.getValue(row.getCell(1)));
        vo.setJob(EgovExcelUtil.getValue(row.getCell(2)));
        return vo;
    }
}
```

### 3) 엑셀 업로드 및 워크북 처리

```java
@Resource(name = "excelService")
private EgovExcelService excelService;

// xls 업로드: 첫 시트 0행부터, 전체를 한 번에 insertEmpUsingBatch 쿼리로 입력
FileInputStream fileIn = new FileInputStream(new File("testdata/testBatch.xls"));
Integer count = excelService.uploadExcel("insertEmpUsingBatch", fileIn);

// xlsx 업로드: 시트명 지정, 2행부터 500건 단위 배치
excelService.uploadExcel("insertEmpUsingBatch", new FileInputStream("emp.xlsx"), "Sheet1", 2, 500, new XSSFWorkbook());

// 워크북 저장/적재
excelService.createWorkbook(new HSSFWorkbook(), "/tmp/out/test.xls");
Workbook wb = excelService.loadWorkbook("/tmp/out/test.xls");
```

### 4) Spring MVC xlsx 다운로드 뷰 (`src/test/resources/META-INF/spring/excel-servlet.xml`, `download/CategoryPOIExcelView.java`)

```xml
<bean id="categoryPOIExcelView" class="org.egovframe.rte.fdl.excel.download.CategoryPOIExcelView"/>
<bean class="org.springframework.web.servlet.view.BeanNameViewResolver">
    <property name="order" value="0"/>
</bean>
```

```java
public class CategoryPOIExcelView extends AbstractPOIExcelView {
    @Override
    protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook wb,
                                      HttpServletRequest req, HttpServletResponse resp) throws Exception {
        XSSFSheet sheet = wb.createSheet("User List");
        setText(getCell(sheet, 0, 0), "User List");
        setText(getCell(sheet, 2, 0), "id");
        // ... model 의 데이터를 행에 기록
    }
}
```

컨트롤러에서 `model.addAttribute("filename", "users")`를 넣으면 `users.xlsx`로 다운로드됩니다.

### 5) JXLS 템플릿 출력 (`src/test/java/.../EgovPOIExcelServiceTest.java`)

```java
Context context = new Context();
context.putVar("employees", employees);
Transformer transformer = PoiTransformer.createTransformer(templateIn, out);
transformer.setFullFormulaRecalculationOnOpening(true);
JxlsHelper.getInstance().processTemplate(context, transformer);
```

---

## 빌드

```bash
# 루트에서 이 모듈만 빌드 (의존 모듈 포함)
mvn clean install -pl Foundation/org.egovframe.rte.fdl.excel -am
```

## 테스트

```bash
mvn test -pl Foundation/org.egovframe.rte.fdl.excel
```

테스트는 `<jdbc:embedded-database type="HSQL">`(`META-INF/testdata/sample_schema_ddl_hsql.sql`) 인메모리 DB와 `testdata/` 아래 엑셀 파일을 사용하며 외부 서버가 필요 없습니다. 컨텍스트는 `classpath:/META-INF/spring/context-*.xml`(datasource, mybatis, sqlMap, transaction, excel, common)로 구성됩니다.

---

## Maven 의존성 추가

```xml
<dependency>
    <groupId>org.egovframe.rte</groupId>
    <artifactId>org.egovframe.rte.fdl.excel</artifactId>
    <version>4.3.1</version>
</dependency>
```

부모 POM(`org.egovframe.boot.starter.parent` 또는 `org.egovframe.web.config.parent` 4.3.1)을 상속하면 `<version>`을 생략할 수 있습니다. `-egov.1` 포크 아티팩트(Spring Framework 등)를 받으려면 `https://maven.egovframe.go.kr/maven/` 저장소 등록이 필요합니다. Servlet 기반 부모 POM은 POI 5.4.0에 맞춰 `xmlbeans` 5.3.0을 관리합니다.

---

## 참고

- 상위 개요: [실행환경 루트 README](../../README.md)
- 관련 모듈: [org.egovframe.rte.fdl.cmmn](../org.egovframe.rte.fdl.cmmn/README.md), [org.egovframe.rte.fdl.filehandling](../org.egovframe.rte.fdl.filehandling/README.md), [org.egovframe.rte.psl.dataaccess](../../Persistence/org.egovframe.rte.psl.dataaccess/README.md)
- 실행환경 위키: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte4.3
