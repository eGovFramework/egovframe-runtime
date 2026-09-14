/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUID 버전 7(RFC 9562) 채번 서비스.
 *
 * <p>UUIDv7 은 <b>선두 48비트 Unix 밀리초 타임스탬프 + 랜덤 74비트</b> 구조다. 시간 기반 v1 방식의
 * {@link EgovUUIdGnrServiceImpl} 이나 무작위 v4 와 달리</p>
 * <ul>
 *   <li><b>시간순 정렬성</b> — 문자열·바이트 정렬이 생성 시각 순서와 일치해 DB 인덱스 지역성이 좋다. PK 채번에 적합하다</li>
 *   <li><b>다중 인스턴스 안전</b> — 랜덤부가 {@link SecureRandom} 이라 서버 간 조정이나 address 설정 없이 쓸 수 있다</li>
 *   <li><b>설정 불필요</b> — 네트워크 정보나 messageSource 빈에 의존하지 않는다</li>
 * </ul>
 *
 * <p>같은 밀리초 안에서 만든 값 사이의 순서는 보장하지 않는다. 지원 타입은 {@link #getNextStringId()}(표준 36자 표기)와
 * {@link #getNextBigDecimalId()}(128비트 값)이며, 축소 숫자 타입과 정책 기반 문자열은 기존 UUID 구현과 같이
 * {@link FdlException} 으로 알린다.</p>
 *
 * <pre class="code">
 * &lt;bean id="uuidV7GnrService" class="org.egovframe.rte.fdl.idgnr.impl.EgovUuidV7GnrServiceImpl"/&gt;
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovUuidV7GnrServiceImpl implements EgovIdGnrService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String UNSUPPORTED = " type id is not supported by UUID v7 generation service";

    /**
     * RFC 9562 UUIDv7 을 만든다 — unix_ts_ms(48) | ver(4)=7 | rand_a(12) | var(2)=10 | rand_b(62).
     *
     * @return UUIDv7
     */
    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);

        long mostSigBits = (timestamp & 0xFFFFFFFFFFFFL) << 16;           // 48비트 Unix 밀리초
        mostSigBits |= 0x7000L;                                            // 버전 7
        mostSigBits |= ((random[0] & 0x0FL) << 8) | (random[1] & 0xFFL);   // rand_a 12비트

        long leastSigBits = 0L;
        for (int i = 2; i < 10; i++) {
            leastSigBits |= (random[i] & 0xFFL) << (8 * (9 - i));          // rand_b 64비트
        }
        leastSigBits = (leastSigBits & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // variant 10xx

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * UUIDv7 문자열(36자, 표준 표기). 생성 시각 순서로 정렬된다.
     */
    @Override
    public String getNextStringId() throws FdlException {
        return generate().toString();
    }

    /**
     * UUIDv7 의 128비트 값을 BigDecimal 로 돌려준다.
     */
    @Override
    public BigDecimal getNextBigDecimalId() throws FdlException {
        UUID uuid = generate();
        BigInteger high = BigInteger.valueOf(uuid.getMostSignificantBits()).and(UNSIGNED_LONG_MASK);
        BigInteger low = BigInteger.valueOf(uuid.getLeastSignificantBits()).and(UNSIGNED_LONG_MASK);
        return new BigDecimal(high.shiftLeft(64).or(low));
    }

    private static final BigInteger UNSIGNED_LONG_MASK = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    @Override
    public byte getNextByteId() throws FdlException {
        throw new FdlException("Byte" + UNSUPPORTED);
    }

    @Override
    public short getNextShortId() throws FdlException {
        throw new FdlException("Short" + UNSUPPORTED);
    }

    @Override
    public int getNextIntegerId() throws FdlException {
        throw new FdlException("Integer" + UNSUPPORTED);
    }

    @Override
    public long getNextLongId() throws FdlException {
        throw new FdlException("Long" + UNSUPPORTED);
    }

    @Override
    public String getNextStringId(String strategyId) throws FdlException {
        throw new FdlException("Strategy-based String" + UNSUPPORTED);
    }

    @Override
    public String getNextStringId(EgovIdGnrStrategy strategy) throws FdlException {
        throw new FdlException("Strategy-based String" + UNSUPPORTED);
    }

}
