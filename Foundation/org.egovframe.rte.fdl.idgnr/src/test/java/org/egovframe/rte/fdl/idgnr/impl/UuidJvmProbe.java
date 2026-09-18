package org.egovframe.rte.fdl.idgnr.impl;

import java.util.UUID;

/**
 * {@link TimeBasedUUIDGenerator} 의 static 상태(lastTime, clockSequence)를 검증 JVM 과 분리하기 위한
 * fresh child JVM probe. {@link EgovUuidV1ComplianceTest} 가 자식 프로세스로 실행한다.
 */
public final class UuidJvmProbe {

    private UuidJvmProbe() {
    }

    /**
     * args[0] = Unix epoch millis, args[1] = node(hostId) 로 단일 UUID 를 생성해 stdout 에 출력한다.
     */
    public static void main(String[] args) {
        UUID uuid = TimeBasedUUIDGenerator.generateIdFromTimestamp(Long.parseLong(args[0]), Long.parseLong(args[1]));
        System.out.println(uuid);
    }

}
