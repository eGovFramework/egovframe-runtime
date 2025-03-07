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
package org.egovframe.rte.fdl.cryptography;

public interface EgovGeneralCryptoService extends EgovCryptoService {

    /**
     * 암복호화 알고리즘.
     * 
     * @param algorithm
     */
    void setAlgorithm(String algorithm);
    
    /**
     * 암복호화 알고리즘.
     * 
     * @return
     */
    String getAlgorithm();

}
