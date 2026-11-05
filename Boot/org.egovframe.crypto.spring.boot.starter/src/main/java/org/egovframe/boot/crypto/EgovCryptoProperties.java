/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="org.egovframe.crypto")
@Getter
@Setter
public class EgovCryptoProperties {

    private boolean initial = true;
    private boolean crypto = true;
    private String algorithm;
    private String algorithmKey;
    private String algorithmKeyHash;
    private int cryptoBlockSize = 1024;
    private boolean plainDigest = true;
    private String cryptoDBDriver = "";
    private String cryptoDBUrl = "";
    private String cryptoDBUsername = "";
    private String cryptoDBPassword = "";

}
