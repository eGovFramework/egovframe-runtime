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
package org.egovframe.rte.bat.core.step;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Objects;

public class TaskletDeleteStep implements Tasklet, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskletDeleteStep.class);

    private Resource directory;
    private Resource file;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.debug("### TaskletDeleteStep execute() Strat ");

        File fileInfo = directory.getFile();
        if (fileInfo.isDirectory()) { // Directory
            File[] files = fileInfo.listFiles();
            Boolean isEmpty = ArrayUtils.isEmpty(files);
            if (!isEmpty && Objects.requireNonNull(files).length > 0) {
                for (File value : files) {
                    boolean deleted = value.delete();
                    if (!deleted) {
                        throw new UnexpectedJobExecutionException("Could not delete file " + value.getPath());
                    } else {
                        LOGGER.debug("### TaskletDeleteStep execute() File Deleted : {}", value.getPath());
                    }
                }
            } else {
                throw new NullPointerException("File does not exist.");
            }
        } else { // File
            boolean deleted = fileInfo.delete();
            if (!deleted) {
                throw new UnexpectedJobExecutionException("Could not delete file " + fileInfo.getPath());
            } else {
                LOGGER.debug("### TaskletDeleteStep execute() File Deleted : {}", fileInfo.getPath());
            }
        }

        return RepeatStatus.FINISHED;
    }

    public Resource getDirectory() {
        return directory;
    }

    public void setDirectory(Resource directory) {
        this.directory = directory;
    }

}
