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
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * IdGnrStrategyIsolationTest 클래스
 * <p>
 * String ID 생성 시 호출 인자로 받은 정책이 서비스의 기본 정책 상태를 오염시키지
 * 않는지 검증한다.
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2026.07.31  개발팀          최초 생성
 */
public class IdGnrStrategyIsolationTest {

    /**
     * 정책 오브젝트 인자 호출 후에도 기본 정책은 유지된다.
     */
    @Test
    public void testStrategyArgumentDoesNotReplaceDefaultStrategy() throws FdlException {
        CounterIdGnrService service = new CounterIdGnrService();
        EgovIdGnrStrategy defaultStrategy = service.getStrategy();

        assertEquals("1", service.getNextStringId());
        assertEquals("SMPL-####2", service.getNextStringId(new EgovIdGnrStrategyImpl("SMPL-", 5, '#')));
        assertSame(defaultStrategy, service.getStrategy());
        assertEquals("3", service.getNextStringId());
        assertSame(defaultStrategy, service.getStrategy());
    }

    /**
     * 정책 빈 이름 인자 호출 후에도 기본 정책은 유지된다.
     */
    @Test
    public void testStrategyBeanNameDoesNotReplaceDefaultStrategy() throws FdlException {
        CounterIdGnrService service = new CounterIdGnrService();
        EgovIdGnrStrategy defaultStrategy = service.getStrategy();
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("sampleStrategy", new EgovIdGnrStrategyImpl("SMPL-", 5, '#'));
        service.setBeanFactory(beanFactory);

        assertEquals("SMPL-####1", service.getNextStringId("sampleStrategy"));
        assertSame(defaultStrategy, service.getStrategy());
        assertEquals("2", service.getNextStringId());
        assertSame(defaultStrategy, service.getStrategy());
    }

    /**
     * setStrategy 로 지정한 정책은 무인자 호출에 계속 적용된다.
     */
    @Test
    public void testSetStrategyContinuesToApplyToDefaultStringId() throws FdlException {
        CounterIdGnrService service = new CounterIdGnrService();
        EgovIdGnrStrategy strategy = new EgovIdGnrStrategyImpl("SET-", 4, '0');
        service.setStrategy(strategy);

        assertSame(strategy, service.getStrategy());
        assertEquals("SET-0001", service.getNextStringId());
        assertEquals("SET-0002", service.getNextStringId());
        assertSame(strategy, service.getStrategy());
    }

    private static class CounterIdGnrService extends AbstractIdGnrService {

        private long counter;

        @Override
        protected BigDecimal getNextBigDecimalIdInner() throws FdlException {
            return new BigDecimal(getNextLongIdInner());
        }

        @Override
        protected long getNextLongIdInner() throws FdlException {
            return ++counter;
        }
    }

}
