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
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * ID Generation 서비스를 위한 UUID 구현 클래스
 *
 * <p><b>NOTE</b>: UUID(Universally Unique Identifier) 알고리즘 기반의 유일키를 제공 받을 수 있다.</p>
 *
 * <p><b>보안 주의:</b> {@link #setAddress(String)}로 IP/MAC 주소를 설정하면 {@link #getNextStringId()}는
 * RFC 9562 UUIDv1(타임스탬프+노드+기동 시 random 초기화된 14-bit clockSequence)로 생성한다. 예측 가능한
 * 시각 기반 값이므로 세션ID·비밀번호 재설정 토큰·API 키 등 <b>보안 목적 식별자로 사용하지 말 것</b> —
 * 그런 용도라면 {@code setAddress}를 호출하지 않은 기본 경로({@link java.util.UUID#randomUUID()}) 또는
 * 별도의 CSPRNG 기반 토큰 생성기를 사용하라. 또한 clockSequence는 JVM 기동 시 random 초기화되므로 여러
 * WAS 인스턴스가 동일한 address로 설정되어도 고정 초기값 충돌은 없지만, 같은 밀리초·같은 노드 조합에서는
 * 14-bit 공간(1/16384) 내 확률적 충돌 가능성은 남는다(RFC 9562 UUIDv1 고유 특성).</p>
 *
 * @author 실행환경 개발팀 김태호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.02.01	김태호				최초 생성
 * 2014.08.18	한성곤				UUID 오류 수정
 * 2017.02.03	장동한				시큐어코딩(ES)-시큐어 코딩 적절하지 않은 난수값 사용[CWE-330]
 * 2017.02.15	장동한				시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 * @since 2009.02.01
 */
public class EgovUUIdGnrServiceImpl implements EgovIdGnrService, ApplicationContextAware {

    /**
     * Class 사용 로거 지정
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EgovUUIdGnrServiceImpl.class);
    private static final String ERROR_STRING = "address in the configuration should be a valid IP or MAC Address";
    /**
     * Message Source
     */
    private MessageSource messageSource;
    /**
     * Address Id
     */
    private String mAddressId;
    /**
     * MAC Address
     */
    private long hostId;

    /**
     * Message Source Injection
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.messageSource = (MessageSource) applicationContext.getBean("messageSource");
    }

    /**
     * BigDecimal 타입을 아이디 제공
     *
     * @return BigDecimal 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public BigDecimal getNextBigDecimalId() throws FdlException {
        String newId = getNextStringId().replaceAll("-", "");
        // CHECKSTYLE:OFF
        BigInteger bi = new BigInteger(newId, 16);
        // CHECKSTYLE:ON
        BigDecimal bd = new BigDecimal(bi);
        return bd;
    }

    /**
     * byte 타입을 아이디 제공
     *
     * @return byte 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public byte getNextByteId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"Byte"}, null);
    }

    /**
     * int 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     *
     * @return int 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public int getNextIntegerId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"Integer"}, null);
    }

    /**
     * long 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     *
     * @return long 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public long getNextLongId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"Long"}, null);
    }

    /**
     * short 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     *
     * @return short 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public short getNextShortId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"Short"}, null);
    }

    /**
     * String 타입을 아이디 제공
     *
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public String getNextStringId() throws FdlException {
        return getUUId();
    }

    /**
     * 정책정보를 입력받아 String 타입을 아이디 제공을 요청하면 불가능한 요청이라는 에러 발생
     *
     * @param strategy 정책정보 오브젝트
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public String getNextStringId(EgovIdGnrStrategy strategy) throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"String"}, null);
    }

    /**
     * 정책정보를 입력받아 String 타입을 아이디 제공을 요청하면 불가능한 요청이라는 에러 발생
     *
     * @param strategyId 정책 String
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public String getNextStringId(String strategyId) throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported", new String[]{"String"}, null);
    }

    /**
     * Config 정보에 지정된 Address 세팅
     *
     * @param address Config 에 지정된 address 정보
     * @throws FdlException IP 정보가 이상한 경우
     */
    public void setAddress(String address) throws FdlException, NoSuchAlgorithmException {
        byte[] addressBytes = new byte[6];
        Random random = SecureRandom.getInstanceStrong();
        random.setSeed(System.currentTimeMillis());
        if (null == address) {
            LOGGER.debug("IDGeneration Service : Using a random number as the base for id's."
                    + "This is not the best method for many purposes, but may be adequate in some circumstances."
                    + " Consider using an IP or ethernet (MAC) address if available. ");
            for (int i = 0; i < 6; i++) {
                //2017-02-03 장동한 시큐어코딩(ES)-시큐어 코딩 적절하지 않은 난수값 사용[CWE-330]
                addressBytes[i] = (byte) (random.nextDouble() * 255 + 0);
            }
        } else {
            if (address.indexOf(".") > 0) {
                // we should have an IP
                StringTokenizer stok = new StringTokenizer(address, ".");
                if (stok.countTokens() != 4) {
                    throw new FdlException(ERROR_STRING);
                }
                addressBytes[0] = (byte) 255;
                addressBytes[1] = (byte) 255;
                int i = 2;
                while (stok.hasMoreTokens()) {
                    addressBytes[i++] = Integer.valueOf(stok.nextToken(), 10).byteValue();
                }
            } else if (address.indexOf(":") > 0) {
                // we should have a MAC
                StringTokenizer stok = new StringTokenizer(address, ":");
                if (stok.countTokens() != 6) {
                    throw new FdlException(ERROR_STRING);
                }
                int i = 0;
                while (stok.hasMoreTokens()) {
                    addressBytes[i++] = Integer.valueOf(stok.nextToken(), 16).byteValue();
                }
            } else {
                throw new FdlException(ERROR_STRING);
            }
        }
        // CHECKSTYLE:ON

        mAddressId = TimeBasedUUIDGenerator.getMacAddressAsString(addressBytes);
        hostId = TimeBasedUUIDGenerator.getMacAddressAsLong(addressBytes);
        LOGGER.debug("Address Id : " + mAddressId);
    }

    /**
     * UUID 얻기
     *
     * @return String unique id
     */
    private String getUUId() {
        if (mAddressId == null) {
            return UUID.randomUUID().toString();
        } else {
            return TimeBasedUUIDGenerator.generateId(hostId).toString();
        }
    }
}

/**
 * Will generate time-based UUID (version 1 UUID).
 * Requires JDK 1.6+
 *
 * @author Oleg Zhurakousky
 */
final class TimeBasedUUIDGenerator {
    public static final Object LOCK = new Object();
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeBasedUUIDGenerator.class);
    private static final long HOST_IDENTIFIER = getHostId();
    /**
     * UUID epoch(1582-10-15 00:00:00 UTC, RFC 5.1)부터 Unix epoch(1970-01-01)까지의
     * 100ns 간격 수. 141,427일 × 86,400초 × 10,000,000 = 122,192,928,000,000,000
     * (0x01B21DD213814000).
     */
    private static final long UUID_EPOCH_TICKS = 122192928000000000L;
    /** node 필드 48-bit (RFC 5.1) */
    private static final long NODE_MASK = 0x0000FFFFFFFFFFFFL;
    /** IEEE 802 multicast bit: IEEE 주소를 구하지 못한 node 는 관리(로컬) 주소임을 표시 (RFC 6.10) */
    private static final long MULTICAST_BIT = 0x0000010000000000L;
    /**
     * clock sequence 초기화 및 node fallback 용 난수원. RFC 5.1 은 clock sequence 를
     * 시스템 식별자당 한 번 random 값으로 초기화할 것(Node ID 와 상관되지 않을 것)을 요구한다.
     */
    private static final SecureRandom RANDOM = new SecureRandom();
    /** JVM 기동 시 random 초기화된 14-bit clock sequence */
    private static final long CLOCK_SEQUENCE = RANDOM.nextInt(1 << 14);
    /** IEEE 주소를 구하지 못한 인스턴스용 random multicast node (JVM 생애 주기 동안 고정) */
    private static final long RANDOM_NODE = (RANDOM.nextLong() & NODE_MASK) | MULTICAST_BIT;
    /** 마지막으로 사용한 UUID timestamp (100ns 단위, 단일 JVM 내 strictly monotonic) */
    private static long lastTime = Long.MIN_VALUE;

    private TimeBasedUUIDGenerator() {
    }

    /**
     * Will generate unique time based UUID where the next UUID is
     * always greater then the previous.
     */
    public static final UUID generateId() {
        return generateIdFromTimestamp(System.currentTimeMillis(), 0L);
    }

    public static final UUID generateId(long hostId) {
        return generateIdFromTimestamp(System.currentTimeMillis(), hostId);
    }

    public static final UUID generateIdFromTimestamp(long currentTimeMillis, long hostId) {
        long node = (hostId != 0L ? hostId : HOST_IDENTIFIER) & NODE_MASK;
        if (node == 0L) {
            // RFC 5.1: IEEE 주소를 구할 수 없으면 random 으로 생성한 node 를 사용한다
            node = RANDOM_NODE;
        }

        long timestamp;
        synchronized (LOCK) {
            // RFC 5.1: UUIDv1 timestamp = 1582-10-15 UTC 자정부터의 100ns count
            timestamp = currentTimeMillis * 10_000L + UUID_EPOCH_TICKS;
            if (timestamp <= lastTime) {
                // 동일 시각 연속 생성 또는 system clock 후퇴 시 마지막 timestamp 를 재사용해
                // 100ns 단위로 논리적 증가시켜 단일 JVM 내 유일성을 보장한다 (RFC 6.1, 6.2)
                timestamp = lastTime + 1;
            }
            lastTime = timestamp;
        }

        // RFC 5.1 Figure 6: time_low / time_mid / ver / time_high
        long msb = ((timestamp & 0xFFFFFFFFL) << 32)
                | (((timestamp >>> 32) & 0xFFFFL) << 16)
                | 0x1000L
                | ((timestamp >>> 48) & 0x0FFFL);
        // RFC 4.1/5.1: var = 0b10, 14-bit clock sequence, 48-bit node
        long lsb = 0x8000000000000000L
                | ((CLOCK_SEQUENCE & 0x3FFFL) << 48)
                | node;

        return new UUID(msb, lsb);
    }

    private static final long getHostId() {
        long macAddressAsLong = 0;
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        macAddressAsLong <<= 8;
                        macAddressAsLong ^= (long) mac[i] & 0xFF;
                    }
                }
            }
            //2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
        } catch (IllegalArgumentException | UnknownHostException | SocketException e) {
            LOGGER.debug("[IllegalArgumentException] getHostId Exception : " + e.getMessage());
        }

        LOGGER.debug("MAC Address (from Network Interface) : " + getMacAddressAsString(getMacAddress(macAddressAsLong)));

        return macAddressAsLong;
    }

    public static byte[] getMacAddress(long address) {
        byte[] addressInBytes = new byte[]{
                (byte) ((address >> 40) & 0xff),
                (byte) ((address >> 32) & 0xff),
                (byte) ((address >> 24) & 0xff),
                (byte) ((address >> 16) & 0xff),
                (byte) ((address >> 8) & 0xff),
                (byte) ((address >> 0) & 0xff)
        };
        return addressInBytes;
    }

    public static String getMacAddressAsString(byte[] address) {
        StringBuilder builder = new StringBuilder();
        for (byte b : address) {
            if (builder.length() > 0) {
                builder.append(":");
            }
            builder.append(String.format("%02X", b & 0xFF));
        }
        return builder.toString();
    }

    public static long getMacAddressAsLong(byte[] address) {
        long mac = 0;
        // CHECKSTYLE:OFF
        for (int i = 0; i < 6; i++) {
            long t = (address[i] & 0xffL) << ((5 - i) * 8);
            mac |= t;
        }

        return mac;
    }

}
