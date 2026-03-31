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

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

public class TaskletShellStep implements Tasklet, InitializingBean {

    private String shellScript;
    private String encoding;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (shellScript == null || shellScript.trim().equals("")) {
            throw new UnexpectedJobExecutionException("Shell Script is Empty!");
        }

        String[] arrCmdLine = shellScript.split("[\\r?\\n]+");

        int resultShellScript = 0;
        if (arrCmdLine.length == 0) { // single line
            resultShellScript = ShellScriptSupport.shellCmd(shellScript, encoding);
            if (resultShellScript > 0) {
                throw new UnexpectedJobExecutionException("Error Executing shell script!");
            }
        } else { // multiline
            for (String s : arrCmdLine) {
                resultShellScript = ShellScriptSupport.shellCmd(s, encoding);
                if (resultShellScript > 0) {
                    throw new UnexpectedJobExecutionException("Error Executing shell script!");
                }
            }
        }

        return RepeatStatus.FINISHED;
    }

    public String getShellScript() {
        return shellScript;
    }

    public void setShellScript(String shellScript) {
        this.shellScript = shellScript;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
