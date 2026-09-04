/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.idgnr.impl;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * MaxIdMessageArgsTest 클래스
 * <p>
 * 최대값 초과로 ID 생성이 중단될 때 사용자에게 전달되는 예외 메시지가 로그와 같은
 * 치환 인자를 받는지 검증한다.
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2026.09.03  개발팀          최초 생성
 */
public class MaxIdMessageArgsTest {

    private static final String MAX_ID_KEY = "error.idgnr.greater.maxid";

    private static MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * 예외 메시지가 치환되지 않은 자리표시자를 남기지 않고 로그 메시지와 같은지 확인한다.
     */
    private static void assertMessageIsFormatted(MessageSource messageSource, FdlException exception) {
        assertFalse(exception.getMessage().contains("{0}"), "예외 메시지에 치환되지 않은 자리표시자가 남아있다.");
        assertEquals(messageSource.getMessage(MAX_ID_KEY, new String[]{"Long"}, Locale.getDefault()), exception.getMessage());
    }

    /**
     * 다음 ID 가 요청한 타입의 최대값을 넘을 때의 예외 메시지를 확인한다.
     */
    @Test
    public void testNextIdGreaterThanMaxIdMessageIsFormatted() {
        MessageSource messageSource = messageSource();
        CounterIdGnrService service = new CounterIdGnrService(messageSource);
        service.setCounter(Byte.MAX_VALUE);

        FdlException exception = assertThrows(FdlException.class, service::getNextByteId);

        assertMessageIsFormatted(messageSource, exception);
    }

    /**
     * BigDecimal 로 채번한 다음 ID 가 long 범위를 넘을 때의 예외 메시지를 확인한다.
     */
    @Test
    public void testBigDecimalGreaterThanLongMessageIsFormatted() {
        MessageSource messageSource = messageSource();
        CounterIdGnrService service = new CounterIdGnrService(messageSource);
        service.setCounter(Long.MAX_VALUE);
        service.setUseBigDecimals(true);

        FdlException exception = assertThrows(FdlException.class, service::getNextLongId);

        assertMessageIsFormatted(messageSource, exception);
    }

    /**
     * 할당받은 블럭에서 채번한 ID 가 long 범위를 넘을 때의 예외 메시지를 확인한다.
     */
    @Test
    public void testBlockAllocationOverflowMessageIsFormatted() throws Exception {
        MessageSource messageSource = messageSource();
        BlockIdGnrService service = new BlockIdGnrService(messageSource);
        service.setBlockSize(2);
        service.afterPropertiesSet();
        assertEquals(Long.MAX_VALUE, service.getNextLongId());

        FdlException exception = assertThrows(FdlException.class, service::getNextLongId);

        assertMessageIsFormatted(messageSource, exception);
    }

    private static class CounterIdGnrService extends AbstractIdGnrService {

        private BigDecimal counter = BigDecimal.ZERO;

        CounterIdGnrService(MessageSource messageSource) {
            this.messageSource = messageSource;
        }

        void setCounter(long counter) {
            this.counter = BigDecimal.valueOf(counter);
        }

        @Override
        protected BigDecimal getNextBigDecimalIdInner() throws FdlException {
            counter = counter.add(BigDecimal.ONE);
            return counter;
        }

        @Override
        protected long getNextLongIdInner() throws FdlException {
            return getNextBigDecimalIdInner().longValue();
        }
    }

    private static class BlockIdGnrService extends AbstractDataBlockIdGnrService {

        BlockIdGnrService(MessageSource messageSource) {
            this.messageSource = messageSource;
        }

        @Override
        protected BigDecimal allocateBigDecimalIdBlock(int blockSize) throws FdlException {
            return BigDecimal.valueOf(allocateLongIdBlock(blockSize));
        }

        @Override
        protected long allocateLongIdBlock(int blockSize) throws FdlException {
            return Long.MAX_VALUE;
        }
    }

}
