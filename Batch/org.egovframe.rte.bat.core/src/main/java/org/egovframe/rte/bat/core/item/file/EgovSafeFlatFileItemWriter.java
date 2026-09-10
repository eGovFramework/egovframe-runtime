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
package org.egovframe.rte.bat.core.item.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 파일 운영 안전장치가 적용된 {@link FlatFileItemWriter}.
 *
 * <ol>
 *   <li><b>원자적 완료</b> — 임시 파일({@code <파일명>.egovtmp})에 쓰고 Step 이 COMPLETED 일 때만 최종 파일명으로 옮긴다.
 *       후속 시스템이 미완성 파일을 집어가는 사고를 막는다.</li>
 *   <li><b>실패 시 정리</b> — Step 이 완료되지 않으면 임시 파일을 지워 불완전 산출물을 남기지 않는다.</li>
 *   <li><b>최대 크기 가드</b> — {@code maxFileSizeBytes} 초과가 예상되는 청크는 쓰기 전에 거부해 Step 을 실패시킨다
 *       (라인 집계 결과의 인코딩 바이트 수로 미리 계산, 위반 시 {@link WriteFailedException}).</li>
 * </ol>
 *
 * <p>Step 의 writer 로 등록하면 ItemStream 과 StepExecutionListener 가 함께 등록되어 별도 설정이 없다.
 * <b>주의:</b> 실패 시 임시 파일을 지우는 정책상 재시작 이어쓰기(saveState)와 함께 쓸 수 없어 saveState 는 false 로 고정된다
 * (실패하면 처음부터 다시 실행하는 전제).</p>
 *
 * <pre>
 * &lt;bean id="safeWriter" class="org.egovframe.rte.bat.core.item.file.EgovSafeFlatFileItemWriter" scope="step"&gt;
 *     &lt;property name="resource" value="file:/data/batch/out/daily.dat"/&gt;
 *     &lt;property name="lineAggregator" ref="lineAggregator"/&gt;
 *     &lt;property name="maxFileSizeBytes" value="1073741824"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @param <T> 항목 타입
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovSafeFlatFileItemWriter<T> extends FlatFileItemWriter<T> implements StepExecutionListener {

    /** 임시 파일 접미사 */
    public static final String TMP_SUFFIX = ".egovtmp";

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSafeFlatFileItemWriter.class);

    private WritableResource finalResource;
    private LineAggregator<T> sizeCheckAggregator;
    private String encoding = StandardCharsets.UTF_8.name();
    private String lineSeparator = FlatFileItemWriter.DEFAULT_LINE_SEPARATOR;
    private long maxFileSizeBytes = 0L;
    private long writtenBytes = 0L;

    private File finalFile;
    private File tmpFile;

    public EgovSafeFlatFileItemWriter() {
        // 실패 시 임시 파일을 지우는 정책과 재시작 이어쓰기는 양립하지 않는다 — 전체 재실행 전제
        setSaveState(false);
        super.setEncoding(encoding);
    }

    /**
     * 최종 산출 파일. 쓰기는 같은 디렉터리의 {@code .egovtmp} 임시 파일에 한다.
     */
    @Override
    public void setResource(WritableResource resource) {
        this.finalResource = resource;
    }

    @Override
    public void setLineAggregator(LineAggregator<T> lineAggregator) {
        this.sizeCheckAggregator = lineAggregator;
        super.setLineAggregator(lineAggregator);
    }

    @Override
    public void setEncoding(String newEncoding) {
        this.encoding = newEncoding;
        super.setEncoding(newEncoding);
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
        super.setLineSeparator(lineSeparator);
    }

    /**
     * 산출 파일 최대 크기(byte). 초과가 예상되는 청크는 쓰기 전에 거부된다(기본 0 = 제한 없음).
     */
    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (finalResource == null) {
            throw new ItemStreamException("resource must be set on EgovSafeFlatFileItemWriter");
        }
        try {
            finalFile = finalResource.getFile();
        } catch (IOException ex) {
            throw new ItemStreamException("Failed to resolve output file from resource", ex);
        }
        File parentDirectory = finalFile.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists() && !parentDirectory.mkdirs() && !parentDirectory.exists()) {
            throw new ItemStreamException("Failed to create output directory: " + parentDirectory);
        }
        tmpFile = new File(parentDirectory, finalFile.getName() + TMP_SUFFIX);
        if (tmpFile.exists() && !tmpFile.delete()) {
            throw new ItemStreamException("Failed to delete stale temporary file: " + tmpFile);
        }
        writtenBytes = 0L;
        super.setResource(new FileSystemResource(tmpFile));
        super.open(executionContext);
        LOGGER.debug("EgovSafeFlatFileItemWriter writing to temporary file: {}", tmpFile);
    }

    @Override
    public void write(Chunk<? extends T> items) throws Exception {
        if (maxFileSizeBytes > 0) {
            long chunkBytes = estimateChunkBytes(items);
            if (writtenBytes + chunkBytes > maxFileSizeBytes) {
                throw new WriteFailedException("Output file size limit exceeded for '" + finalFile.getName()
                        + "': written=" + writtenBytes + "B, chunk=" + chunkBytes + "B, max=" + maxFileSizeBytes + "B");
            }
            super.write(items);
            writtenBytes += chunkBytes;
        } else {
            super.write(items);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        close();
        if (tmpFile == null) {
            return stepExecution.getExitStatus();
        }
        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                try {
                    Files.move(tmpFile.toPath(), finalFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicUnsupported) {
                    Files.move(tmpFile.toPath(), finalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                LOGGER.info("Batch output committed: {} -> {}", tmpFile.getName(), finalFile);
            } catch (IOException ex) {
                throw new ItemStreamException("Failed to commit output file: " + finalFile, ex);
            }
        } else {
            if (tmpFile.exists() && tmpFile.delete()) {
                LOGGER.warn("Batch step not completed (status={}); incomplete output removed: {}",
                        stepExecution.getStatus(), tmpFile);
            }
        }
        return stepExecution.getExitStatus();
    }

    private long estimateChunkBytes(Chunk<? extends T> items) throws IOException {
        long separatorBytes = lineSeparator.getBytes(encoding).length;
        long bytes = 0;
        for (T item : items) {
            bytes += sizeCheckAggregator.aggregate(item).getBytes(encoding).length + separatorBytes;
        }
        return bytes;
    }

}
