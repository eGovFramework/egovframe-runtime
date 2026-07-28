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
package org.egovframe.rte.fdl.security.config;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 사용자 ID를 salt로 사용하는 SHA-256 비밀번호 인코더.
 *
 * <p>eGovFrame {@code EgovFileScrty.encryptPassword(password, id)} 와 동일한 알고리즘을 적용한다.</p>
 * <p>Spring Security {@link PasswordEncoder#matches(CharSequence, String)} 는 username을 받지 않으므로
 * {@link #matches(CharSequence, String, String)} 를 {@code EgovDaoAuthenticationProvider} 에서 호출한다.</p>
 */
public class EgovIdSaltSha256PasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "SHA-256";

    private final Charset charset;

    public EgovIdSaltSha256PasswordEncoder() {
        this(StandardCharsets.UTF_8);
    }

    public EgovIdSaltSha256PasswordEncoder(Charset charset) {
        this.charset = charset;
    }

    /**
     * 사용자 ID를 salt로 적용하여 비밀번호를 Base64 SHA-256 해시로 반환한다.
     */
    public String encode(CharSequence rawPassword, String username) {
        return digest(rawPassword.toString(), username);
    }

    /**
     * 평문 비밀번호와 DB 저장 해시를 사용자 ID salt 기준으로 비교한다.
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword, String username) {
        if (rawPassword == null || encodedPassword == null || username == null) {
            return false;
        }
        return MessageDigest.isEqual(
                digest(rawPassword.toString(), username).getBytes(charset),
                encodedPassword.getBytes(charset));
    }

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("username is required - use encode(rawPassword, username)");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        throw new UnsupportedOperationException("username is required - use matches(rawPassword, encodedPassword, username)");
    }

    private String digest(String password, String username) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(username.getBytes(charset));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes(charset)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
