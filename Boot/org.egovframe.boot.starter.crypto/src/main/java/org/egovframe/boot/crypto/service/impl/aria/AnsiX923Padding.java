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
package org.egovframe.boot.crypto.service.impl.aria;

/**
 * ANSI X.923 패딩 구현.
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * </pre>
 * @since 2025.06.01
 */
public class AnsiX923Padding implements CryptoPadding {

    private static final byte PADDING_VALUE = 0x00;
    /** 패딩 규칙 이름 */
    private String name = "ANSI-X.923-Padding";

    /** blockSize 배수가 되도록 패딩을 추가한다. */
    public byte[] addPadding(byte[] source, int blockSize) {
        int paddingCnt = source.length % blockSize;
        byte[] paddingResult = null;
        if (paddingCnt != 0) {
            paddingResult = new byte[source.length + (blockSize - paddingCnt)];
            System.arraycopy(source, 0, paddingResult, 0, source.length);
            // 마지막 바이트를 제외하고 0x00 으로 채움
            int addPaddingCnt = blockSize - paddingCnt;
            for (int i = 0; i < addPaddingCnt; i++) {
                paddingResult[source.length + i] = PADDING_VALUE;
            }
            // 마지막 바이트는 패딩 길이
            paddingResult[paddingResult.length - 1] = (byte) addPaddingCnt;
        } else {
            paddingResult = source;
        }
        return paddingResult;
    }

    /** addPadding 으로 추가된 패딩을 제거한다. */
    public byte[] removePadding(byte[] source, int blockSize) {
        byte[] paddingResult = null;
        boolean isPadding = false;
        // 패딩 길이
        int lastValue = source[source.length - 1];
        if (lastValue < (blockSize - 1)) {
            int zeroPaddingCount = lastValue - 1;
            for (int i = 2; i < (zeroPaddingCount + 2); i++) {
                if (source[source.length - i] != PADDING_VALUE) {
                    isPadding = false;
                    break;
                }
            }
            isPadding = true;
        } else {
            // blockSize 보다 크면 패딩 없음
            isPadding = false;
        }
        // 패딩 길이 1 보정
        if (lastValue != 0) {
            if ((blockSize % lastValue) == 1) {
                isPadding = true;
            } else if (isPadding && lastValue <= 0) { // 0 이하는 패딩 아님(패딩 길이 1 보정)
                isPadding = false;
            }
        }
        if (isPadding) {
            for (int index = source.length - lastValue; index < source.length - 1; index++) {
                if (source[index] != (byte) 0) {
                    isPadding = false;
                    break;
                }
            }
        }
        if (isPadding) {
            paddingResult = new byte[source.length - lastValue];
            try {
                System.arraycopy(source, 0, paddingResult, 0, paddingResult.length);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return source;
            }
        } else {
            paddingResult = source;
        }
        return paddingResult;
    }

    public String getName() {
        return name;
    }

}
