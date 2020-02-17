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
package egovframework.rte.fdl.cryptography.impl.aria;

/**
 * 암호화에서 블럭 사이즈를 맞추기 위해 사용되는 Padding을 추상화 한 Interface
 *
 * @author jinuk jung, junducki@naver.com
 * @version 1.0, 2008. 03. 11
 */
/*
 * History -2008. 03. 11, Create, jinuk jung
 */

public interface CryptoPadding {

    /**
     * 요청한 Block Size를 맞추기 위해 Padding을 추가한다.
     * 
     * @param source byte[] 패딩을 추가할 bytes
     * @param blockSize int block size
     * @return byte[] 패딩이 추가 된 결과 bytes
     */
    public byte[] addPadding(byte[] source, int blockSize);

    /**
     * 요청한 Block Size를 맞추기 위해 추가 된 Padding을 제거한다.
     * 
     * @param source byte[] 패딩을 제거할 bytes
     * @param blockSize int block size
     * @return byte[] 패딩이 제거 된 결과 bytes
     */
    public byte[] removePadding(byte[] source, int blockSize);

}
