/*
 * Copyright 2002-2008 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.excel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.egovframe.rte.fdl.excel.util.EgovExcelUtil;
import org.egovframe.rte.fdl.excel.vo.PersonHourVO;
import org.egovframe.rte.fdl.filehandling.EgovFileUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * FileServiceTest is TestCase of File Handling Service
 * @author Seongjong Yoon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/context-*.xml" })
public class EgovExcelServiceTest extends AbstractJUnit4SpringContextTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovExcelServiceTest.class);

	@Resource(name = "excelService")
	private EgovExcelService excelService;

	private String fileLocation;

	@Before
	public void onSetUp() {
		this.fileLocation = "testdata";
	}

	/**
	 * [Flow #-1] 엑셀 파일 생성 : 새 엑셀 파일을 생성함
	 */
	@Test
	public void testWriteExcelFile() throws Exception {
		LOGGER.debug("testWriteExcelFile start....");
		try {
			String sheetName1 = "first sheet";
			String sheetName2 = "second sheet";

			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testWriteExcelFile.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file...." + sb);
			}

			Workbook workbook = new HSSFWorkbook();
			workbook.createSheet(sheetName1);
			workbook.createSheet(sheetName2);
			workbook.createSheet();

			// 엑셀 파일 생성
			Workbook workbook1 = excelService.createWorkbook(workbook, sb.toString());

			// 파일 존재 확인, 저장된 Sheet명 일치 점검
			assertTrue(EgovFileUtil.isExistsFile(sb.toString()));
			assertEquals(sheetName1, workbook1.getSheetName(0));
			assertEquals(sheetName2, workbook1.getSheetName(1));
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testWriteExcelFile end....");
		}
	}

	/**
	 * [Flow #-2] 액셀 파일 수정 : 엑샐 파일 내 셀의 내용을 변경하고 저장할 수 있음
	 */
	@Test
	public void testModifyCellContents() throws Exception {
		LOGGER.debug("testModifyCellContents start....");
		try {
			String content = "Use \n with word wrap on to create a new line";
			short rownum = 2;
			int cellnum = 2;

			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testModifyCellContents.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file...." + sb);
			}

			// 엑셀 파일 생성
			Workbook workbook = new HSSFWorkbook();
			workbook.createSheet("test");
			Sheet sheet = workbook.getSheetAt(0);
			Font font = workbook.createFont();
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);
			cellStyle.setWrapText(true);
			Row row = sheet.createRow(rownum);
			row.setHeight((short) 0x349);
			Cell cell = row.createCell(cellnum);
			cell.setCellValue(new HSSFRichTextString(content));
			cell.setCellStyle(cellStyle);
			sheet.setColumnWidth(20, (int) ((50 * 8) / ((double) 1 / 20)));

			// 엑셀 파일 저장
			excelService.createWorkbook(workbook, sb.toString());

			// 엑셀 파일 로드
			Workbook workbook1 = excelService.loadWorkbook(sb.toString());
			Sheet sheet1 = workbook1.getSheetAt(0);
			Row row1 = sheet1.getRow(rownum);
			Cell cell1 = row1.getCell(cellnum);

			// 수정된 셀의 내용 점검
			LOGGER.debug("testModifyCellContents content {}", content);
			LOGGER.debug("testModifyCellContents cell {}", cell1.getRichStringCellValue());
			assertNotSame("TEST", cell1.getRichStringCellValue().toString());
			assertEquals(content, cell1.getRichStringCellValue().toString());
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testModifyCellContents end....");
		}
	}

	/**
	 * [Flow #-3] 엑셀 파일 속성 수정 : 엑셀 파일의 속성(셀의 크기, Border의 속성, 셀의 색상, 정렬 등)을 수정함
	 */
	@Test
	public void testWriteExcelFileAttribute() throws Exception {
		LOGGER.debug("testWriteExcelFileAttribute start....");
		try {
			short rowheight = 40*10;
			int columnwidth = 30;

			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testWriteExcelFileAttribute.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file....{}", sb);
			}

			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet("new sheet");
			workbook.createSheet("second sheet");
			sheet.setDefaultRowHeight(rowheight);
			sheet.setDefaultColumnWidth(columnwidth);
			Font font = workbook.createFont();
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);
			cellStyle.setWrapText(true);
			cellStyle.setAlignment(HorizontalAlignment.RIGHT); // 정렬
			cellStyle.setFillPattern(FillPatternType.DIAMONDS); // 무늬 스타일
			cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex()); // 무늬 색
			cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex()); // 배경색
			sheet.setDefaultColumnStyle((short) 0, cellStyle);

			Workbook workbook1 = excelService.createWorkbook(workbook, sb.toString());
			Sheet sheet1 = workbook1.getSheetAt(0);
			assertEquals(rowheight, sheet1.getDefaultRowHeight());
			assertEquals(columnwidth, sheet1.getDefaultColumnWidth());

			CellStyle cellStyle1 = workbook1.getCellStyleAt((short) (workbook1.getNumCellStyles() - 1));

			LOGGER.debug("getAlignment : {}", cellStyle1.getAlignment());
			LOGGER.debug("getFillPattern : {}", cellStyle1.getFillPattern());
			LOGGER.debug("getFillForegroundColor : {}", cellStyle1.getFillForegroundColor());
			LOGGER.debug("getFillBackgroundColor : {}", cellStyle1.getFillBackgroundColor());

			assertEquals(FillPatternType.DIAMONDS, cellStyle1.getFillPattern());
			assertEquals(HorizontalAlignment.RIGHT, cellStyle1.getAlignment());
			assertEquals(IndexedColors.BLUE.getIndex(), cellStyle1.getFillForegroundColor());
			assertEquals(IndexedColors.RED.getIndex(), cellStyle1.getFillBackgroundColor());
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testWriteExcelFileAttribute end....");
		}
	}

	/**
	 * [Flow #-4] 엑셀 문서 속성 수정 : 엑셀 파일 문서의 속성(Header, Footer)을 수정함
	 */
	@Test
	public void testModifyDocAttribute() throws Exception {
		LOGGER.debug("testModifyDocAttribute start....");
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testModifyDocAttribute.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file....{}", sb);
			}

			Workbook workbook = new HSSFWorkbook();
			workbook.createSheet();
			Sheet sheet = workbook.createSheet("doc test sheet");
			Row row = sheet.createRow(1);
			Cell cell = row.createCell(1);
			cell.setCellValue(new HSSFRichTextString("Header/Footer Test"));

			// Header
			Header header = sheet.getHeader();
			header.setCenter("Center Header");
			header.setLeft("Left Header");
			header.setRight(HSSFHeader.stripFields("Stencil-Normal Italic font and size 16"));

			// Footer
			Footer footer = sheet.getFooter();
			footer.setCenter(HSSFHeader.stripFields("Fixedsys"));
			footer.setLeft("Left Footer");
			footer.setRight("Right Footer");

			// 엑셀 파일 저장
			excelService.createWorkbook(workbook, sb.toString());

			assertTrue(EgovFileUtil.isExistsFile(sb.toString()));

			// 검증
			Workbook workbook1 = excelService.loadWorkbook(sb.toString());
			Sheet sheet1 = workbook1.getSheet("doc test sheet");

			Header header1 = sheet1.getHeader();
			assertEquals("Center Header", header1.getCenter());
			assertEquals("Left Header", header1.getLeft());
			assertEquals(HSSFHeader.stripFields("Stencil-Normal Italic font and size 16"), header1.getRight());

			Footer footer1 = sheet1.getFooter();
			assertEquals("Right Footer", footer1.getRight());
			assertEquals("Left Footer", footer1.getLeft());
			assertEquals(HSSFHeader.stripFields("Fixedsys"), footer1.getCenter());
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testModifyDocAttribute end....");
		}
	}

	/**
	 * [Flow #-5] 셀 내용 추출 : 엑셀 파일을 읽어 특정 셀의 값을 얻어 옴
	 */
	@Test
	public void testGetCellContents() throws Exception {
		LOGGER.debug("testGetCellContents start....");
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testGetCellContents.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file....{}", sb);
			}

			Workbook workbook = new HSSFWorkbook();
			workbook.createSheet();
			Sheet sheet = workbook.createSheet("cell test sheet");
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setWrapText(true);
			for (int i = 0; i < 100; i++) {
				Row row = sheet.createRow(i);
				for (int j = 0; j < 5; j++) {
					Cell cell = row.createCell(j);
					cell.setCellValue(new HSSFRichTextString("row " + i + ", cell " + j));
					cell.setCellStyle(cellStyle);
				}
			}

			// 엑셀 파일 저장
			excelService.createWorkbook(workbook, sb.toString());

			// 검증
			Workbook workbook1 = excelService.loadWorkbook(sb.toString());
			Sheet sheet1 = workbook1.getSheet("cell test sheet");
			for (int i = 0; i < 100; i++) {
				Row row1 = sheet1.getRow(i);
				for (int j = 0; j < 5; j++) {
					Cell cell1 = row1.getCell(j);
					LOGGER.debug("row {}, cell {} : {}", i, j, cell1.getRichStringCellValue());
					assertEquals("row " + i + ", cell " + j, cell1.getRichStringCellValue().toString());
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testGetCellContents end....");
		}
	}

	/**
	 * [Flow #-6] 셀 속성 수정 : 특정 셀의 속성(폰트, 사이즈 등)을 수정함
	 */
	@Test
	public void testModifyCellAttribute() throws Exception {
		LOGGER.debug("testModifyCellAttribute start....");
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(fileLocation).append("/").append("testModifyCellAttribute.xls");
			if (EgovFileUtil.isExistsFile(sb.toString())) {
				EgovFileUtil.delete(new File(sb.toString()));
				LOGGER.debug("Delete file....{}", sb);
			}

			Workbook workbook = new HSSFWorkbook();
			workbook.createSheet();
			Sheet sheet = workbook.createSheet("cell test sheet2");
			CellStyle cellStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setFontHeight((short)16);
			font.setBold(true);
			font.setFontName("fixedsys");
			cellStyle.setFont(font);
			cellStyle.setAlignment(HorizontalAlignment.RIGHT); // cell 정렬
			cellStyle.setWrapText(true);
			for (int i = 0; i < 100; i++) {
				Row row = sheet.createRow(i);
				for (int j = 0; j < 5; j++) {
					Cell cell = row.createCell(j);
					cell.setCellValue(new HSSFRichTextString("row " + i + ", cell " + j));
					cell.setCellStyle(cellStyle);
				}
			}

			// 엑셀 파일 저장
			excelService.createWorkbook(workbook, sb.toString());

			// 검증
			Workbook workbook1 = excelService.loadWorkbook(sb.toString());
			Sheet sheet1 = workbook1.getSheet("cell test sheet2");
			CellStyle cellStyle1 = workbook1.getCellStyleAt((short)(workbook1.getNumCellStyles() - 1));
			Font font1 = ((HSSFCellStyle) cellStyle1).getFont(workbook1);

			LOGGER.debug("getAlignment : {}", cellStyle1.getAlignment());
			LOGGER.debug("getWrapText : {}", cellStyle1.getWrapText());
			LOGGER.debug("font getFontHeight : {}", font1.getFontHeight());
			LOGGER.debug("font getBoldweight : {}", font1.getBold());
			LOGGER.debug("font getFontName : {}", font1.getFontName());

			for (int i = 0; i < 100; i++) {
				Row row1 = sheet1.getRow(i);
				for (int j = 0; j < 5; j++) {
					Cell cell1 = row1.getCell(j);
					LOGGER.debug("row {}, cell {} : {}", i, j, cell1.getRichStringCellValue());
					assertEquals(16, font1.getFontHeight());
					assertTrue(font1.getBold());
					assertEquals(HorizontalAlignment.RIGHT, cellStyle1.getAlignment());
					assertTrue(cellStyle1.getWrapText());
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testModifyCellAttribute end....");
		}
	}

	/**
	 * [Flow #-7] 공통 템플릿 사용 : 공통 템플릿을 사용하여 일관성을 유지함
	 */
	@Test
	public void testUseTemplate1() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(fileLocation).append("/template/").append("template1.xls");

		StringBuilder sbResult = new StringBuilder();
		sbResult.append(fileLocation).append("/").append("testUseTemplate1.xls");

		Object[][] sample_data = {
				{ "Yegor Kozlov", "YK", 5.0, 8.0, 10.0, 5.0, 5.0, 7.0, 6.0 },
				{ "Gisella Bronzetti", "GB", 4.0, 3.0, 1.0, 3.5, null, null, 4.0 },
		};

		try {
			FileInputStream in = new FileInputStream(sb.toString());
			Workbook workbook = new HSSFWorkbook(in);
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = 0; i < sample_data.length; i++) {
				Row row = sheet.getRow(i+2);
				for (int j = 0; j < sample_data[i].length; j++) {
					if (sample_data[i][j] == null) continue;
					Cell cell = row.getCell(j);
					if (sample_data[i][j] instanceof String) {
						cell.setCellValue(new HSSFRichTextString((String) sample_data[i][j]));
						LOGGER.debug("##### cell string >>> " + cell.getRichStringCellValue().getString());
					} else {
						cell.setCellValue((Double) sample_data[i][j]);
						LOGGER.debug("##### cell number >>> " + cell.getNumericCellValue());
					}
				}
			}
			// 수식 재계산
			workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
			FileOutputStream out = new FileOutputStream(sbResult.toString());
			workbook.write(out);
			out.close();
			in.close();

			// 검증
			Workbook workbook1 = excelService.loadWorkbook(sbResult.toString());
			Sheet sheet1 = workbook1.getSheetAt(0);
			for (int i = 0; i < sample_data.length; i++) {
				Row row = sheet1.getRow(2 + i);
				for (int j = 0; j < sample_data[i].length; j++) {
					Cell cell = row.getCell(j);
					if (sample_data[i][j] == null) {
						assertEquals(cell.getCellType(), CellType.BLANK);
					} else if (cell.getCellType() == CellType.NUMERIC) {
						assertEquals(sample_data[i][j], cell.getNumericCellValue());
					} else {
						assertEquals(sample_data[i][j], cell.getRichStringCellValue().getString());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testUseTemplate end....");
		}
	}

	/**
	 * [Flow #-7-1] 공통 템플릿 사용 : 공통 템플릿을 사용하여 일관성을 유지함
	 * jXLS 사용
	 */
	@Test
	public void testUseTemplate2() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(fileLocation).append("/template/").append("template2.xls");

		StringBuilder sbResult = new StringBuilder();
		sbResult.append(fileLocation).append("/").append("testUseTemplate2.xls");

		try {
			// 출력할 객체를 만든다.
			List<PersonHourVO> persons = new ArrayList<>();
			PersonHourVO person = new PersonHourVO();
			person.setName("Yegor Kozlov");
			person.setId("YK");
			person.setMon(5.0);
			person.setTue(8.0);
			person.setWed(10.0);
			person.setThu(5.0);
			person.setFri(5.0);
			person.setSat(7.0);
			person.setSun(6.0);
			persons.add(person);

			PersonHourVO person1 = new PersonHourVO();
			person1.setName("Gisella Bronzetti");
			person1.setId("GB");
			person1.setMon(4.0);
			person1.setTue(3.0);
			person1.setWed(1.0);
			person1.setThu(3.5);
			person1.setSun(4.0);
			persons.add(person1);

			Context context = new Context();
			context.putVar("persons", persons);
			FileInputStream in = new FileInputStream(sb.toString());
			FileOutputStream out = new FileOutputStream(sbResult.toString());
			Transformer transformer = PoiTransformer.createTransformer(in, out);
			transformer.setFullFormulaRecalculationOnOpening(true);
			JxlsHelper.getInstance().processTemplate(context, transformer);
			out.close();
			in.close();

			Double[][] value = {{5.0, 8.0, 10.0, 5.0, 5.0, 7.0, 6.0}, {4.0, 3.0, 1.0, 3.5, null, null, 4.0}};

			// 검증
			Workbook workbook1 = excelService.loadWorkbook(sbResult.toString());
			Sheet sheet = workbook1.getSheetAt(0);
			for (int i = 0; i < 2; i++) {
				Row rowValue = sheet.getRow(i + 2);
				for (int j = 0; j < 7; j++) {
					Cell cellValue = rowValue.getCell((j + 2));
					if (value[i][j] == null) {
						assertEquals(cellValue.getCellType(), CellType.BLANK);
					} else if (cellValue.getCellType() == CellType.NUMERIC) {
						assertEquals(value[i][j], Double.valueOf(cellValue.getNumericCellValue()));
					} else {
						assertEquals(value[i][j], cellValue.getRichStringCellValue().getString());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new Exception(e);
		} finally {
			LOGGER.debug("testUseTemplate end....");
		}
	}

	/**
	 * 셀 Data type 테스트
	 *   셀의 값이 Null 인경우 오류발생
	 */
	@Test
	public void testCellDataFormat() throws Exception {
		Workbook workbook = excelService.loadWorkbook(fileLocation + "/" + "testDataFormat.xls");
		Sheet sheet = workbook.getSheetAt(0);
		Row row = sheet.getRow(7);
		Cell cell = row.getCell(0);
		assertEquals("2009/04/01", EgovExcelUtil.getValue(cell));

		row = sheet.getRow(8);
		cell = row.getCell(0);
		assertEquals("2009/04/02", EgovExcelUtil.getValue(cell));
	}
}
