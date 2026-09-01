/*
 * Copyright 2008-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.access.bean;

import org.egovframe.rte.fdl.access.service.EgovAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AuthorityResourceMetadata}의 {@code reload()}가 동시 읽기와 안전한지 검증한다.
 *
 * <p>{@code getAuthorities()}/{@code getRoles()}는 요청마다 static 권한/리소스 리스트를
 * 순회·반환한다. 기존 {@code reload()}는 같은 리스트를 {@code clear()}+{@code add()}로
 * 제자리 변이하여, 갱신과 동시에 순회가 일어나면 {@link java.util.ConcurrentModificationException}이
 * 발생하거나 부분(빈) 상태가 읽혔다. 본 테스트는 reload와 읽기를 동시에 반복하여
 * 예외 없이 동작하는지 확인한다.
 */
class AuthorityResourceMetadataConcurrencyTest {

    /** 호출 시마다 size개의 항목을 가진 새 리스트를 반환하는 단순 구현. */
    private static final class FakeAccessService implements EgovAccessService {
        private final int size;

        private FakeAccessService(int size) {
            this.size = size;
        }

        private List<Map<String, Object>> build() {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("userid", "u" + i);
                row.put("authority", "ROLE_" + i);
                list.add(row);
            }
            return list;
        }

        @Override
        public List<Map<String, Object>> getAuthorityUser() {
            return build();
        }

        @Override
        public List<Map<String, Object>> getRoleAndUrl() {
            return build();
        }
    }

    @AfterEach
    void resetStaticState() {
        new AuthorityResourceMetadata(new ArrayList<>(), new ArrayList<>());
    }

    @Test
    @DisplayName("reload()와 동시 읽기(순회)가 ConcurrentModificationException 없이 동작한다")
    void reloadConcurrentWithReadsIsThreadSafe() throws Exception {
        int seedSize = 50;
        AuthorityResourceMetadata meta = new AuthorityResourceMetadata(
                new ArrayList<>(new FakeAccessService(seedSize).getAuthorityUser()),
                new ArrayList<>(new FakeAccessService(seedSize).getRoleAndUrl()));
        meta.setEgovAccessService(new FakeAccessService(seedSize));

        int threadCount = 16;
        int rounds = 300;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final boolean reloader = (t % 2 == 0);
            futures.add(pool.submit(() -> {
                try {
                    startGate.await();
                    for (int r = 0; r < rounds && failure.get() == null; r++) {
                        if (reloader) {
                            meta.reload();
                        } else {
                            // getAuthorities()/getRoles()와 동일한 순회 패턴
                            long sink = 0;
                            List<Map<String, Object>> authList = AuthorityResourceMetadata.getAuthorityList();
                            if (authList != null) {
                                for (Map<String, Object> row : authList) {
                                    sink += String.valueOf(row.get("userid")).length();
                                }
                            }
                            List<Map<String, Object>> resourceMap = AuthorityResourceMetadata.getResourceMap();
                            if (resourceMap != null) {
                                for (Map<String, Object> row : resourceMap) {
                                    sink += String.valueOf(row.get("authority")).length();
                                }
                            }
                            if (sink < 0) {
                                throw new IllegalStateException("unreachable");
                            }
                        }
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                }
            }));
        }

        startGate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "동시성 테스트 시간 초과");
        for (Future<?> f : futures) {
            f.get();
        }

        assertNull(failure.get(), () -> "동시 reload/읽기 중 예외 발생: " + failure.get());
    }

    @Test
    @DisplayName("reload() 후 최신 스냅샷이 반영된다")
    void reloadReplacesSnapshot() throws Exception {
        AuthorityResourceMetadata meta = new AuthorityResourceMetadata(new ArrayList<>(), new ArrayList<>());
        meta.setEgovAccessService(new FakeAccessService(7));

        meta.reload();

        assertEquals(7, AuthorityResourceMetadata.getAuthorityList().size());
        assertEquals(7, AuthorityResourceMetadata.getResourceMap().size());
        assertEquals("u0", AuthorityResourceMetadata.getAuthorityList().get(0).get("userid"));
    }
}
