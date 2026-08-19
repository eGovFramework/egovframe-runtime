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
package org.egovframe.rte.bat.core.listener;

import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * Chunk 단계 이후에 호출되는 메소드를 갖고 있는 클래스
 *
 * @author 배치실행개발팀
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.25	배치실행개발팀		최초 생성
 * </pre>
 * @since 2012.06.27
 */
public class EgovChunkPostProcessor extends ChunkListenerSupport {

    /**
     * Chunk 수행 이후에 호출되는 부분
     */
    @Override
    public void afterChunk(ChunkContext context) {
        afterChunk();
    }

    /**
     * Chunk 수행 이후에 호출되는 부분
     *
     * @deprecated 프레임워크가 호출하는 시그니처가 ChunkContext 를 받는 형태로 바뀌어
     * 이 메소드는 더 이상 직접 호출되지 않는다. 기존에 이 메소드를 재정의한 코드가
     * 계속 동작하도록 {@link #afterChunk(ChunkContext)} 에서 호출해 주며,
     * 새로 작성하는 코드는 {@link #afterChunk(ChunkContext)} 를 재정의한다.
     */
    @Deprecated
    public void afterChunk() {
    }

}
