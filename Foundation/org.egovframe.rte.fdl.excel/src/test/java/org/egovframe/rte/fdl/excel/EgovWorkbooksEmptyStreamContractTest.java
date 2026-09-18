package org.egovframe.rte.fdl.excel;

import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;
import org.egovframe.rte.fdl.excel.util.EgovWorkbooks;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EgovWorkbooks 클래스 javadoc 은 "엑셀이 아니거나 읽을 수 없는 경우" BaseRuntimeException 으로 알린다고
 * 약속한다. 빈 스트림은 WorkbookFactory 가 IOException 이 아닌 unchecked EmptyFileException 으로 거부하는
 * 경우라 그 약속이 지켜지는지 확인한다.
 */
public class EgovWorkbooksEmptyStreamContractTest {

    @Test
    public void 빈_스트림은_BaseRuntimeException으로_감싸져야_한다() {
        BaseRuntimeException ex = assertThrows(BaseRuntimeException.class,
                () -> EgovWorkbooks.open(new ByteArrayInputStream(new byte[0])),
                "빈 스트림은 BaseRuntimeException 이어야 하는데 원본 POI 예외가 그대로 새어나갔다");
        assertNotNull(ex.getCause(), "원인 예외가 보존돼야 한다");
    }
}
