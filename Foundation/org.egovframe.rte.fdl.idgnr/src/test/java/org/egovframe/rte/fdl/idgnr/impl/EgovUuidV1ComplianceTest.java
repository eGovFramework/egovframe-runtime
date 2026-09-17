package org.egovframe.rte.fdl.idgnr.impl;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TimeBasedUUIDGenerator} 가 생성하는 값이 UUID version 1 (RFC 9562) 규약을 만족하는지 검증하는
 * Test 클래스.
 *
 * <p>판정 기준: RFC 4.1(variant), 4.2(version), 5.1(UUIDv1 layout, 1582-10-15 UTC 기준
 * 100ns timestamp, 14-bit clock sequence, 48-bit node, clock sequence 초기값 random 요구) 및
 * {@link UUID#timestamp()}, {@link UUID#variant()} 의 javadoc 계약.</p>
 *
 * <p>절대 timestamp 검증과 cross-JVM 재현은 generator static 상태의 영향을 받지 않도록
 * {@link UuidJvmProbe} 를 fresh child JVM 으로 실행해 격리한다.</p>
 */
public class EgovUuidV1ComplianceTest {

    /** 고정 검증 시각: 2023-11-14T22:13:20Z (Unix epoch millis) */
    private static final long FIXED_MILLIS = 1_700_000_000_000L;
    /** clock 후퇴 재현용 미래 시각 */
    private static final long LATER_MILLIS = FIXED_MILLIS + 600_000L;
    /** 고정 48-bit node (테스트용 MAC) */
    private static final long NODE = 0x0000F079195BL;
    private static final long NODE_MASK = 0x0000FFFFFFFFFFFFL;
    /**
     * UUID epoch(1582-10-15 00:00:00 UTC)부터 Unix epoch(1970-01-01)까지의 100ns 간격 수.
     * RFC 5.1: UUIDv1 timestamp 는 1582-10-15 자정부터의 100ns count.
     * 141,427일 × 86,400초 × 10,000,000 = 122,192,928,000,000,000 (0x01B21DD213814000).
     */
    private static final long UUID_EPOCH_TICKS = 122192928000000000L;
    /** child JVM stdout 에서 UUID 문자열 한 줄을 골라내기 위한 패턴 (로깅 출력 제외용) */
    private static final Pattern UUID_LINE = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /**
     * fresh child JVM 에서 단일 UUID 를 생성해 반환한다.
     */
    private static UUID uuidFromFreshJvm(long millis, long node) throws Exception {
        String javaBin = ProcessHandle.current().info().command().orElse("java");
        Process process = new ProcessBuilder(javaBin, "-cp", System.getProperty("java.class.path"),
                UuidJvmProbe.class.getName(), Long.toString(millis), Long.toString(node))
                .redirectErrorStream(true)
                .start();
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertTrue(finished, "child JVM did not finish in time: " + output);
        assertEquals(0, process.exitValue(), "child JVM failed: " + output);
        Matcher matcher = UUID_LINE.matcher(output);
        String uuidLine = null;
        while (matcher.find()) {
            uuidLine = matcher.group();
        }
        assertTrue(uuidLine != null, "child JVM printed no UUID: " + output);
        return UUID.fromString(uuidLine);
    }

    /**
     * RFC 5.1 에 따라 Unix epoch millis 를 UUID timestamp(100ns, UUID epoch 기준)로 변환한 기대값.
     */
    private static long expectedTimestamp(long unixMillis) {
        return unixMillis * 10_000L + UUID_EPOCH_TICKS;
    }

    /**
     * A. version 필드는 1 이어야 한다. (RFC 4.2, 5.1)
     */
    @Test
    public void versionIsOne() {
        UUID uuid = TimeBasedUUIDGenerator.generateIdFromTimestamp(FIXED_MILLIS, NODE);
        assertEquals(1, uuid.version());
    }

    /**
     * B. variant 는 RFC/IETF (java.util.UUID 기준 2) 이어야 한다. (RFC 4.1: octet 8 의 상위 2 bit = 10)
     */
    @Test
    public void variantIsIetf() {
        UUID uuid = TimeBasedUUIDGenerator.generateIdFromTimestamp(FIXED_MILLIS, NODE);
        assertEquals(2, uuid.variant());
    }

    /**
     * C. 고정 Unix millis 에 대해 timestamp 는 UUID epoch 기준 100ns 값이어야 한다.
     * (RFC 5.1, {@link UUID#timestamp()} javadoc: 100-nanosecond units since 1582-10-15 UTC)
     */
    @Test
    public void timestampIs100nsSinceUuidEpoch() throws Exception {
        UUID uuid = uuidFromFreshJvm(FIXED_MILLIS, NODE);
        assertEquals(expectedTimestamp(FIXED_MILLIS), uuid.timestamp());
    }

    /**
     * D. node 는 hostId 의 하위 48-bit 이어야 한다. (RFC 5.1: node = 48-bit)
     */
    @Test
    public void nodeIsLower48BitsOfHostId() throws Exception {
        assertEquals(NODE, uuidFromFreshJvm(FIXED_MILLIS, NODE).node());
        // 48-bit 를 넘는 상위 bit 가 섞인 hostId 와 음수(전 bit set) hostId 도 하위 48-bit 로 정규화되어야 한다
        assertEquals(NODE, uuidFromFreshJvm(FIXED_MILLIS, (0x1234L << 48) | NODE).node());
        assertEquals(NODE_MASK, uuidFromFreshJvm(FIXED_MILLIS, -1L).node());
    }

    /**
     * E. 동일 timestamp 를 반복 생성해도 중복되어서는 안 된다.
     */
    @Test
    public void sameTimestampRepeatedGenerationIsUnique() {
        UUID first = TimeBasedUUIDGenerator.generateIdFromTimestamp(FIXED_MILLIS, NODE);
        UUID second = TimeBasedUUIDGenerator.generateIdFromTimestamp(FIXED_MILLIS, NODE);
        assertNotEquals(first, second);
    }

    /**
     * F. system clock 이 후퇴해도 JVM 내에서 중복 없이 단조 증가해야 한다.
     */
    @Test
    public void clockRollbackKeepsMonotonicUniqueness() {
        UUID beforeRollback = TimeBasedUUIDGenerator.generateIdFromTimestamp(LATER_MILLIS, NODE);
        UUID afterRollback = TimeBasedUUIDGenerator.generateIdFromTimestamp(FIXED_MILLIS, NODE);
        assertNotEquals(beforeRollback, afterRollback);
        assertTrue(afterRollback.compareTo(beforeRollback) > 0, "clock 후퇴 후에도 이전 값보다 커야 한다");
    }

    /**
     * G. 동일 timestamp, 동일 node 를 사용하는 별도 JVM 이 동일 UUID 를 생성해서는 안 된다.
     * (clock sequence 초기값이 JVM 마다 고정 0 이면 deterministic 하게 충돌한다)
     */
    @Test
    public void twoFreshJvmsWithSameTickAndNodeDoNotCollide() throws Exception {
        UUID first = uuidFromFreshJvm(FIXED_MILLIS, NODE);
        UUID second = uuidFromFreshJvm(FIXED_MILLIS, NODE);
        assertNotEquals(first, second);
    }

}
