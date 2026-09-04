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
package org.egovframe.rte.bat.core.item.database;

import org.egovframe.rte.bat.core.item.database.support.EgovMethodMapItemPreparedStatementSetter;
import org.egovframe.rte.bat.core.reflection.EgovReflectionSupport;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * sqlTypes 산출부와 소비부의 float 타입 표기 일치 검증.
 *
 * <pre>
 * 산출부 EgovReflectionSupport.getSqlTypeArray 는 Class.getSimpleName() 을 쓰므로
 * primitive 필드에 대해 "int", "double", "long" 처럼 소문자 이름을 낸다.
 * primitive float 도 마찬가지로 "float" 이 산출되며, 소비부
 * EgovMethodMapItemPreparedStatementSetter 가 이를 받아 ps.setFloat 으로 넘겨야 한다.
 * </pre>
 *
 * @author 배치실행개발팀
 * @since 2026.09.03
 */
class EgovMethodMapFloatSqlTypeTest {

    /** primitive float 과, 정상 동작하는 형제 primitive(double)를 함께 담은 VO. */
    public static class PrimitiveFloatVo {
        private double score;
        private float rate;

        public PrimitiveFloatVo(double score, float rate) {
            this.score = score;
            this.rate = rate;
        }

        public double getScore() {
            return score;
        }

        public float getRate() {
            return rate;
        }
    }

    /** wrapper Float 필드 VO. 기존 동작 보존 확인용. */
    public static class WrapperFloatVo {
        private Float ratio;

        public WrapperFloatVo(Float ratio) {
            this.ratio = ratio;
        }

        public Float getRatio() {
            return ratio;
        }
    }

    /** ps.setXxx 호출을 (메소드명, 인덱스, 값) 으로 기록하는 PreparedStatement 대역. */
    private static class RecordingPsHandler implements InvocationHandler {
        private final List<Object[]> calls = new ArrayList<Object[]>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (name.startsWith("set") && args != null && args.length == 2) {
                calls.add(new Object[]{name, args[0], args[1]});
                return null;
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }
            if ("toString".equals(name)) {
                return "RecordingPs";
            }
            return null;
        }
    }

    private static PreparedStatement newRecordingPs(RecordingPsHandler handler) {
        return (PreparedStatement) Proxy.newProxyInstance(
                RecordingPsHandler.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, handler);
    }

    /**
     * 산출부가 낸 sqlTypes 를 그대로 소비부에 넘겼을 때, primitive float 이
     * 형제 primitive 와 동일하게 ps.setFloat 으로 설정되어야 한다.
     */
    @Test
    void primitiveFloatIsSetLikeSiblingPrimitives() throws Exception {
        String[] params = {"score", "rate"};
        PrimitiveFloatVo item = new PrimitiveFloatVo(88.5d, 1.25f);

        EgovReflectionSupport<PrimitiveFloatVo> support = new EgovReflectionSupport<PrimitiveFloatVo>();
        support.generateGetterMethodMap(params, item);
        Map<String, Method> methodMap = support.getMethodMap();
        String[] sqlTypes = support.getSqlTypeArray(params, item);

        // 산출부는 primitive 를 소문자 이름으로 낸다.
        assertEquals("double", sqlTypes[0]);
        assertEquals("float", sqlTypes[1]);

        RecordingPsHandler handler = new RecordingPsHandler();
        PreparedStatement ps = newRecordingPs(handler);

        new EgovMethodMapItemPreparedStatementSetter<PrimitiveFloatVo>()
                .setValues(item, ps, params, sqlTypes, methodMap);

        assertEquals(2, handler.calls.size());

        assertEquals("setDouble", handler.calls.get(0)[0]);
        assertEquals(1, handler.calls.get(0)[1]);
        assertEquals(88.5d, handler.calls.get(0)[2]);

        assertEquals("setFloat", handler.calls.get(1)[0]);
        assertEquals(2, handler.calls.get(1)[1]);
        assertEquals(1.25f, handler.calls.get(1)[2]);
    }

    /**
     * wrapper Float 필드는 기존과 동일하게 ps.setFloat 으로 설정되어야 한다.
     */
    @Test
    void wrapperFloatIsStillSet() throws Exception {
        String[] params = {"ratio"};
        WrapperFloatVo item = new WrapperFloatVo(2.5f);

        EgovReflectionSupport<WrapperFloatVo> support = new EgovReflectionSupport<WrapperFloatVo>();
        support.generateGetterMethodMap(params, item);
        Map<String, Method> methodMap = support.getMethodMap();
        String[] sqlTypes = support.getSqlTypeArray(params, item);

        assertEquals("Float", sqlTypes[0]);

        RecordingPsHandler handler = new RecordingPsHandler();
        PreparedStatement ps = newRecordingPs(handler);

        new EgovMethodMapItemPreparedStatementSetter<WrapperFloatVo>()
                .setValues(item, ps, params, sqlTypes, methodMap);

        assertEquals(1, handler.calls.size());
        assertEquals("setFloat", handler.calls.get(0)[0]);
        assertEquals(1, handler.calls.get(0)[1]);
        assertEquals(2.5f, handler.calls.get(0)[2]);
    }

    /**
     * EgovJdbcBatchItemWriter.write() 를 경유하는 실제 청크 쓰기 경로에서도 primitive float 이
     * ps.setFloat 으로 설정되어야 한다. sqlTypes 산출과 소비가 모두 프레임워크 내부에서 일어난다.
     * PreparedStatement 대역은 형제 테스트의 FakePsHandler 를 재사용한다.
     */
    @Test
    void primitiveFloatIsSetThroughWriter() throws Exception {
        final EgovJdbcBatchWriteReflectionTest.FakePsHandler handler =
                new EgovJdbcBatchWriteReflectionTest.FakePsHandler();

        EgovJdbcBatchItemWriter<PrimitiveFloatVo> writer = new EgovJdbcBatchItemWriter<PrimitiveFloatVo>();
        writer.setSql("insert into sample (score, rate) values (?, ?)");
        writer.setParams(new String[]{"score", "rate"});
        writer.setItemPreparedStatementSetter(new EgovMethodMapItemPreparedStatementSetter<PrimitiveFloatVo>());
        writer.setSimpleJdbcTemplate(new JdbcTemplate() {
            @Override
            public <X> X execute(String sql, PreparedStatementCallback<X> action) {
                try {
                    return action.doInPreparedStatement((PreparedStatement) Proxy.newProxyInstance(
                            EgovMethodMapFloatSqlTypeTest.class.getClassLoader(),
                            new Class<?>[]{PreparedStatement.class}, handler));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        writer.afterPropertiesSet();

        writer.write(new Chunk<PrimitiveFloatVo>(List.of(new PrimitiveFloatVo(88.5d, 1.25f))));

        assertEquals(2, handler.calls.size());

        assertEquals("setDouble", handler.calls.get(0)[0]);
        assertEquals(1, handler.calls.get(0)[1]);
        assertEquals(88.5d, handler.calls.get(0)[2]);

        assertEquals("setFloat", handler.calls.get(1)[0]);
        assertEquals(2, handler.calls.get(1)[1]);
        assertEquals(1.25f, handler.calls.get(1)[2]);
    }

}
