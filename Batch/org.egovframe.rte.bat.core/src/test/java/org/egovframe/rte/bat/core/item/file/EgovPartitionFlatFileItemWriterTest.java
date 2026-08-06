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
package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EgovPartitionFlatFileItemWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void setsWritableResource() throws Exception {
        Path output = tempDir.resolve("output.txt");
        WritableResource resource = new FileSystemResource(output);
        EgovPartitionFlatFileItemWriter<String> writer = createWriter();

        writer.setResource(resource);
        writer.open(new ExecutionContext());
        writer.close();

        assertTrue(Files.exists(output));
    }

    @Test
    void writesChunk() throws Exception {
        Path output = tempDir.resolve("output.txt");
        Resource resource = new FileSystemResource(output);
        EgovPartitionFlatFileItemWriter<String> writer = createWriter();

        writer.setResource(resource);
        writer.open(new ExecutionContext());
        writer.write(new Chunk<>(List.of("first", "second")));
        writer.close();

        assertEquals("first\nsecond\n", Files.readString(output));
    }

    private EgovPartitionFlatFileItemWriter<String> createWriter() throws Exception {
        EgovPartitionFlatFileItemWriter<String> writer = new EgovPartitionFlatFileItemWriter<>();
        writer.setLineAggregator(item -> item);
        writer.setLineSeparator("\n");
        writer.setTransactional(false);
        writer.afterPropertiesSet();
        return writer;
    }
}
