package org.egovframe.rte.fdl.security.bean;

import org.egovframe.rte.fdl.security.secureobject.EgovSecuredObjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EgovReloadableFilterInvocationSecurityMetadataSource#reload()}의 동시성 안전성을 검증한다.
 *
 * <p>reload()가 기존 requestMap을 clear() 후 다시 채우면, 요청 스레드가 재적재 중인 맵을
 * 순회하여 ConcurrentModificationException 또는 일시적 권한 소실을 겪을 수 있다. 수정본은
 * 새 맵을 완성한 뒤 참조를 원자적으로 교체하므로, 순회 스레드는 항상 완전한 맵만 관측한다.</p>
 */
class EgovReloadableFilterInvocationSecurityMetadataSourceReloadTest {

    /** 요청마다 새 매처·권한을 만들어 반환하는 페이크 서비스(reload가 매번 다른 맵을 받도록). */
    private static class FakeSecuredObjectService implements EgovSecuredObjectService {
        @Override
        public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() {
            LinkedHashMap<RequestMatcher, List<ConfigAttribute>> map = new LinkedHashMap<>();
            for (int i = 0; i < 20; i++) {
                List<ConfigAttribute> roles = new ArrayList<>();
                roles.add(new SecurityConfig("ROLE_" + i));
                // 매 엔트리마다 서로 다른 RequestMatcher 인스턴스(맵 키 identity 확보).
                // 비캡처 람다는 JVM이 싱글턴으로 재사용하므로 i를 캡처해 인스턴스를 분리한다.
                final int idx = i;
                RequestMatcher matcher = request -> idx < 0;
                map.put(matcher, roles);
            }
            return map;
        }

        @Override
        public List<ConfigAttribute> getMatchedRequestMapping(String url) {
            return new ArrayList<>();
        }

        @Override
        public String getHierarchicalRoles() {
            return "";
        }
    }

    @Test
    @DisplayName("reload()와 getAllConfigAttributes() 동시 실행 시 예외 없이 완전한 권한 맵만 관측한다")
    void reloadIsConcurrencySafe() throws Exception {
        EgovReloadableFilterInvocationSecurityMetadataSource source =
                new EgovReloadableFilterInvocationSecurityMetadataSource(new LinkedHashMap<>());
        source.setSecuredObjectService(new FakeSecuredObjectService());
        source.reload(); // 초기 적재

        final int iterations = 2000;
        final AtomicBoolean stop = new AtomicBoolean(false);
        final List<Throwable> errors = new CopyOnWriteArrayList<>();
        final CountDownLatch done = new CountDownLatch(2);
        // 읽기 스레드가 관측한 최소 권한 수. 원자 교체가 아니면 재적재 중 빈/부분 맵(<20)을 볼 수 있다.
        final java.util.concurrent.atomic.AtomicInteger minObserved =
                new java.util.concurrent.atomic.AtomicInteger(Integer.MAX_VALUE);

        // 읽기 스레드: requestMap.entrySet()를 반복 순회(보안 필터 핫패스와 동일 경로)
        Thread reader = new Thread(() -> {
            try {
                while (!stop.get()) {
                    int size = source.getAllConfigAttributes().size();
                    minObserved.accumulateAndGet(size, Math::min);
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                done.countDown();
            }
        });

        // 재적재 스레드: reload() 반복
        Thread reloader = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    source.reload();
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                stop.set(true);
                done.countDown();
            }
        });

        reader.start();
        reloader.start();
        done.await();

        assertTrue(errors.isEmpty(), "동시 reload/순회 중 예외 발생: " + errors);
        // 동시 재적재 내내 읽기 스레드는 항상 완전한 맵(20개)만 관측해야 한다(일시적 권한 소실 없음).
        // 기존 clear()+put() 방식이면 부분 맵(<20)이 관측될 수 있어 이 단정이 깨진다.
        assertEquals(20, minObserved.get(),
                "재적재 중 읽기 스레드가 불완전한 권한 맵을 관측했다: min=" + minObserved.get());
        assertEquals(20, source.getAllConfigAttributes().size());
    }
}
