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
package egovframework.rte.fdl.idgnr.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.StringTokenizer;

import egovframework.rte.fdl.cmmn.exception.FdlException;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.fdl.idgnr.EgovIdGnrStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

/**
 * ID Generation 서비스를 위한 UUID 구현 클래스
 * 
 * <p><b>NOTE</b>: UUID(Universally Unique Identifier)
 * 알고리즘 기반의 유일키를 제공 받을 수 있다.</p>
 * 
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @deprecated Use the EgovUUIdGnrServiceImpl class.
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자      수정내용
 *  ----------------------------------------
 *   2009.02.01  김태호      최초 생성
 *   2020.08.31  ESFC		 ES-적절하지 않은 난수값 사용[CWE-330]
 *
 * </pre>
 */
@Deprecated
public class EgovUUIdGnrService implements EgovIdGnrService, ApplicationContextAware {

    /**
     * Message Source
     */
    private MessageSource messageSource;

    /**
     * Message Source Injection
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.messageSource =
            (MessageSource) applicationContext.getBean("messageSource");
    }

    /**
     * Class 사용 로거 지정
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EgovUUIdGnrService.class);

    private static final String ERROR_STRING = "address in the configuration should be a valid IP or MAC Address";

    /**
     * Address Id
     */
    private String mAddressId;

    /**
     * UUID 생성에 필요한 Time Factor에 의한 ID
     */
    private String mTimeId;

    // 사용자 지정 Address
    // - PMD error report: Avoid unused private fields
    // such as 'address'
    // private String address;

    /**
     * Loop Counter
     */
    private short mLoopCounter = 0;

    /**
     * BigDecimal 타입을 아이디 제공
     * 
     * @return BigDecimal 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public BigDecimal getNextBigDecimalId() throws FdlException {
        String newId = getNextStringId();
        byte[] bytes = newId.getBytes(); // get 16 bytes
        BigDecimal bd = new BigDecimal(0);

        // CHECKSTYLE:OFF
        for (int i = 0; i < bytes.length; i++) {
            bd = bd.multiply(new BigDecimal(256));
            bd = bd.add(new BigDecimal(((int) bytes[i]) & 0xFF));
        }
        // CHECKSTYLE:ON
        
        return bd;
    }

    /**
     * byte 타입을 아이디 제공
     * 
     * @return byte 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public byte getNextByteId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"Byte" }, null);
    }

    /**
     * int 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * 
     * @return int 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public int getNextIntegerId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"Integer" }, null);
    }

    /**
     * long 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * 
     * @return long 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public long getNextLongId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"Long" }, null);
    }

    /**
     * short 타입을 아이디 제공을 요청하면 불가능한 요청이라는 Exception 발생
     * 
     * @return short 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */
    public short getNextShortId() throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"Short" }, null);
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
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"String" }, null);
    }

    /**
     * 정책정보를 입력받아 String 타입을 아이디 제공을 요청하면 불가능한 요청이라는 에러 발생
     * 
     * @param strategyId 정책 String
     * @return String 타입 ID
     * @throws FdlException 아이디 생성에 실패한 경우
     */

    public String getNextStringId(String strategyId) throws FdlException {
        throw new FdlException(messageSource, "error.idgnr.not.supported",
            new String[] {"String" }, null);
    }

    /**
     * Config 정보에 지정된 Address 세팅
     * 
     * @param address Config 에 지정된 address 정보
     * @throws FdlException IP 정보가 이상한 경우
     */
    public void setAddress(String address) throws FdlException {
    	// CHECKSTYLE:OFF
        // this.address = address;
        byte[] addressBytes = new byte[6];
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        if (null == address) {
            LOGGER.warn("IDGeneration Service : Using a random number as the "
                + "base for id's.  This is not the best method for many "
                + "purposes, but may be adequate in some circumstances."
                + " Consider using an IP or ethernet (MAC) address if "
                + "available. ");
            for (int i = 0; i < 6; i++) {
                // 2020.08.31 ESFC ES-적절하지 않은 난수값 사용[CWE-330]
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
                try {
                    while (stok.hasMoreTokens()) {
                        addressBytes[i++] =
                            Integer.valueOf(stok.nextToken(), 16).byteValue();
                    }
                } catch (IllegalArgumentException e) {
                    throw new FdlException(ERROR_STRING);
                }
            } else if (address.indexOf(":") > 0) {
                // we should have a MAC
                StringTokenizer stok = new StringTokenizer(address, ":");
                if (stok.countTokens() != 6) {
                    throw new FdlException(ERROR_STRING);
                }
                int i = 0;
                try {
                    while (stok.hasMoreTokens()) {
                        addressBytes[i++] =
                            Integer.valueOf(stok.nextToken(), 16).byteValue();
                    }
                } catch (IllegalArgumentException e) {
                    throw new FdlException(ERROR_STRING);
                }
            } else {
                throw new FdlException(ERROR_STRING);
            }
        }
        mAddressId = Base64.encode(addressBytes);
        // CHECKSTYLE:ON
    }

    /**
     * UUID 얻기
     * @return String unique id
     */
    private String getUUId() {
    	// CHECKSTYLE:OFF
        byte[] bytes6 = new byte[6];

        long timeNow = System.currentTimeMillis();

        timeNow = (int) timeNow & 0xFFFFFFFF;

        byte[] bytes4 = new byte[4];

        toFixSizeByteArray(new BigInteger(String.valueOf(timeNow)), bytes4);
        bytes6[0] = bytes4[0];
        bytes6[1] = bytes4[1];
        bytes6[2] = bytes4[2];
        bytes6[3] = bytes4[3];

        short counter;
        synchronized (this) {
            counter = getLoopCounter();
        }

        byte[] bytes2 = new byte[2];

        toFixSizeByteArray(new BigInteger(String.valueOf(counter)), bytes2);
        bytes6[4] = bytes2[0];
        bytes6[5] = bytes2[1];

        mTimeId = Base64.encode(bytes6);

        // CHECKSTYLE:ON
        return (mAddressId + mTimeId).replace('+', '_').replace('/', '@');
    }

    /**
     * Get the counter value as a signed short
     * 
     * @original Get the counter value as a signed short
     * @return short loop count
     */
    private short getLoopCounter() {
        return mLoopCounter++;
    }

    // CHECKSTYLE:OFF
    /**
     * To fix size byte array.
     * 
     * @original This method transforms Java BigInteger type into a fix size byte array 
     * containing the two's-complement representation of the integer. 
     * The byte array will be in big-endian byte-order: the most significant byte is in the zeroth element. 
     * If the destination array is shorter then the BigInteger.toByteArray(), 
     * the the less significant bytes will be copy only.
     * If the destination array is longer then the BigInteger.toByteArray(), destination will be left padded with zeros.
     * 
     * @param bigInt Java BigInteger type
     * @param destination destination array
     */
    // CHECKSTYLE:ON
    private void toFixSizeByteArray(BigInteger bigInt, byte[] destination) {
        // Prepare the destination
        for (int i = 0; i < destination.length; i++) {
            destination[i] = 0x00;
        }

        // Convert the BigInt to a byte array
        byte[] source = bigInt.toByteArray();

        // Copy only the fix size length
        if (source.length <= destination.length) {
            for (int i = 0; i < source.length; i++) {
                destination[destination.length - source.length + i] = source[i];
            }
        } else {
            for (int i = 0; i < destination.length; i++) {
                destination[i] = source[source.length - destination.length + i];
            }
        }
    }
}
