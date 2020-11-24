package org.egovframe.rte.fdl.excel.upload;

import java.math.BigDecimal;

import org.egovframe.rte.fdl.excel.EgovExcelMapping;
import org.egovframe.rte.fdl.excel.util.EgovExcelUtil;
import org.egovframe.rte.fdl.excel.vo.ZipVO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author sjyoon
 *
 */
public class EgovExcelBigTestMapping extends EgovExcelMapping {

	@Override
	public ZipVO mappingColumn(Row row) {
		Cell cell0 = row.getCell(0);
    	Cell cell1 = row.getCell(1);
    	Cell cell2 = row.getCell(2);
    	Cell cell3 = row.getCell(3);
    	Cell cell4 = row.getCell(4);
    	Cell cell5 = row.getCell(5);
    	Cell cell6 = row.getCell(6);
    	Cell cell7 = row.getCell(7);

		ZipVO vo = new ZipVO();

		vo.setZipNo(new BigDecimal(cell0.getNumericCellValue()));
		vo.setSerNo(new BigDecimal(cell1.getNumericCellValue()));
		vo.setSidoNm(EgovExcelUtil.getValue(cell2));
		vo.setCggNm(EgovExcelUtil.getValue(cell3));
		vo.setUmdNm(EgovExcelUtil.getValue(cell4));
		vo.setBdNm(EgovExcelUtil.getValue(cell5));
		vo.setJibun(EgovExcelUtil.getValue(cell6));
		vo.setRegId(EgovExcelUtil.getValue(cell7));

		return vo;
	}
}
