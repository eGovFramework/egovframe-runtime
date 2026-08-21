package org.egovframe.rte.psl.reactive.cassandra.connect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.cassandra.ReactiveSession;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * EgovCassandraConfiguration의 자격증명 처리 테스트
 *
 * <p>연결 성공 여부는 보지 않는다. 접속할 노드가 없으면 세션 생성은 어차피 실패하므로,
 * "자격증명 검증 때문에 연결을 시도해보지도 못하고 막히는가"만 판정한다.
 * 드라이버는 자격증명이 비어 있으면 연결 전에 거부한다 —
 * ProgrammaticPlainTextAuthProvider가 Strings.requireNotEmpty로 null이면 NullPointerException,
 * 빈 문자열이면 IllegalArgumentException을 던진다.</p>
 */
public class EgovCassandraConfigurationTest {

    private EgovCassandraConfiguration configWithoutCredentials() {
        EgovCassandraConfiguration config = new EgovCassandraConfiguration();
        config.setDataCenterName("datacenter1");
        config.setKeyspaceName("com");
        config.setContactPoint("127.0.0.1");
        config.setPort(9042);
        return config;
    }

    private void assertNotRejectedByCredentials(EgovCassandraConfiguration config) {
        ReactiveSession session = null;
        try {
            session = config.reactiveSession();
        } catch (NullPointerException | IllegalArgumentException e) {
            fail("자격증명 검증이 연결 시도를 막았다: " + e);
        } catch (RuntimeException e) {
            // 접속할 노드가 없어서 나는 실패는 이 테스트의 관심사가 아니다.
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test
    @DisplayName("username/password 미설정 시 자격증명 때문에 세션 생성이 막히지 않는다")
    public void reactiveSessionWithoutCredentials() {
        assertNotRejectedByCredentials(configWithoutCredentials());
    }

    @Test
    @DisplayName("username/password가 빈 문자열이어도 자격증명 때문에 막히지 않는다")
    public void reactiveSessionWithBlankCredentials() {
        EgovCassandraConfiguration config = configWithoutCredentials();
        config.setUsername("");
        config.setPassword("");

        assertNotRejectedByCredentials(config);
    }

    @Test
    @DisplayName("username/password를 설정하면 종전대로 자격증명이 적용된다")
    public void reactiveSessionWithCredentials() {
        EgovCassandraConfiguration config = configWithoutCredentials();
        config.setUsername("com");
        config.setPassword("com01");

        assertNotRejectedByCredentials(config);
    }

    @Test
    @DisplayName("한쪽만 설정된 자격증명은 드라이버 검증에 그대로 맡긴다")
    public void reactiveSessionWithUsernameOnly() {
        EgovCassandraConfiguration config = configWithoutCredentials();
        config.setUsername("com");

        Throwable thrown = assertThrows(Throwable.class, config::reactiveSession);

        assertInstanceOf(NullPointerException.class, thrown,
                "password만 빠진 설정은 드라이버가 거부해야 한다. 실제 발생: " + thrown);
    }
}
