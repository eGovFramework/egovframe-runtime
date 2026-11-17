/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
 * Chunk 단계 이전에 호출되는 메소드를 갖고 있는 클래스
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
public class EgovChunkPreProcessor extends ChunkListenerSupport {

    /**
     * Chunk 수행 이전에 호출되는 부분
     */
    @Override
    public void beforeChunk(ChunkContext context) {
        beforeChunk();
    }

    /**
     * Chunk 수행 이전에 호출되는 부분
     *
     * @deprecated 프레임워크는 ChunkContext 시그니처만 호출한다. 기존 재정의 호환을 위해
     * {@link #beforeChunk(ChunkContext)} 에서 호출하며, 신규 코드는 {@link #beforeChunk(ChunkContext)} 를 재정의한다.
     */
    @Deprecated
    public void beforeChunk() {
    }

}
