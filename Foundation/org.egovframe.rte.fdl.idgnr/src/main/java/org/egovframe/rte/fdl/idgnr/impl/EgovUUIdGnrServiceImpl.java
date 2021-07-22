/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the ";License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

/**
 * ID Generation 서비스를 위한 UUID 구현 클래스
 * 
 * <p><b>NOTE</b>: UUID(Universally Unique Identifier) 알고리즘 기반의 유일키를 제공 받을 수 있다.</p>
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
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
 */
public class EgovUUIdGnrServiceImpl implements EgovIdGnrService, ApplicationContextAware {

    /**
     * Message Source
     */
    private MessageSource messageSource;

    /**
     * Message Source Injection
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.messageSource = (MessageSource) applicationContext.getBean("messageSource");
    }

    /**
     * Class 사용 로거 지정
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EgovUUIdGnrServiceImpl.class);

    private static final String ERROR_STRING = "address in the configuration should be a valid IP or MAC Address";

    /**
     * Address Id
     */
    private String mAddressId;

    /**
     * MAC Address
     */
    private long hostId;
    
    /**
     * to use Random 
     */
    private Random random;

    /**
     * BigDecimal 타입을 아이디 제공
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
     * @return byte 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public byte getNextByteId() throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "Byte" }, null);
    }

    /**
     * int 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * @return int 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public int getNextIntegerId() throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "Integer" }, null);
    }

    /**
     * long 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * @return long 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public long getNextLongId() throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "Long" }, null);
    }

    /**
     * short 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * @return short 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public short getNextShortId() throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "Short" }, null);
    }

    /**
     * String 타입을 아이디 제공
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public String getNextStringId() throws FdlException {
        return getUUId();
    }

    /**
     * 정책정보를 입력받아 String 타입을 아이디 제공을 요청하면 불가능한 요청이라는 에러 발생
     * @param strategy 정책정보 오브젝트
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
	public String getNextStringId(EgovIdGnrStrategy strategy) throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "String" }, null);
    }

    /**
     * 정책정보를 입력받아 String 타입을 아이디 제공을 요청하면 불가능한 요청이라는 에러 발생
     * @param strategyId 정책 String
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public String getNextStringId(String strategyId) throws FdlException {
		throw new FdlException(messageSource, "error.idgnr.not.supported", new String[] { "String" }, null);
    }

    /**
     * Config 정보에 지정된 Address 세팅
     * @param address Config 에 지정된 address 정보
     * @throws FdlException IP 정보가 이상한 경우
     */
    public void setAddress(String address) throws FdlException, NoSuchAlgorithmException {
        byte[] addressBytes = new byte[6];
        random = SecureRandom.getInstanceStrong();
//        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        if (null == address) {
            LOGGER.warn("IDGeneration Service : Using a random number as the base for id's."
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
					addressBytes[i++] = Integer.valueOf(stok.nextToken(), 16).byteValue();
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
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeBasedUUIDGenerator.class);
	public static final Object LOCK = new Object();
	private static long lastTime;
	private static long clockSequence = 0;
	private static final long HOST_IDENTIFIER = getHostId();
	
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
		long time;
		synchronized (LOCK) {
			if (currentTimeMillis > lastTime) {
				lastTime = currentTimeMillis;
				clockSequence = 0;
			} else {
				++clockSequence;
			}
		}

		time = currentTimeMillis;
		// low Time
		time = currentTimeMillis << 32;
		// mid Time
		time |= ((currentTimeMillis & 0xFFFF00000000L) >> 16);
		// hi Time
		time |= 0x1000 | ((currentTimeMillis >> 48) & 0x0FFF);

		long clockSequenceHi = clockSequence;
		clockSequenceHi <<= 48;
		long lsb = (hostId != 0L ? clockSequenceHi | hostId : clockSequenceHi | HOST_IDENTIFIER);

		return new UUID(time, lsb);
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
		} catch(IllegalArgumentException | UnknownHostException | SocketException e) {
			LOGGER.error("[IllegalArgumentException] getHostId Exception : "+ e.getMessage());
		}

		LOGGER.debug("MAC Address (from Network Interface) : " + getMacAddressAsString(getMacAddress(macAddressAsLong)));

		return macAddressAsLong;
	}

	public static byte[] getMacAddress(long address) {
		byte[] addressInBytes = new byte[] {
			(byte)((address >> 40) & 0xff),
			(byte)((address >> 32) & 0xff),
			(byte)((address >> 24) & 0xff),
			(byte)((address >> 16) & 0xff),
			(byte)((address >> 8 ) & 0xff),
			(byte)((address >> 0) & 0xff)
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
