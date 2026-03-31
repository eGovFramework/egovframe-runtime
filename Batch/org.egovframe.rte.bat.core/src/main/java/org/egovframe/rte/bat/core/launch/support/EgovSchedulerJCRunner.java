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
package org.egovframe.rte.bat.core.launch.support;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * EgovBatchRunner 클래스
 *
 * @author 실행환경 개발팀 이도형
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.25	배치실행개발팀		최초 생성
 * 2013.04.01	한성곤			delay가 0보다 작은 경우 loop 반복 처리 (-delayTime 동안 대기 처리)
 * </pre>
 * @since 2012.06.28
 */
public class EgovSchedulerJCRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSchedulerJCRunner.class);

    /**
     * Java Config 클래스
     */
    private Class<?>[] configClasses;

    /**
     * Scheduling을 위한 대기시간
     */
    private long delayTime;

    public EgovSchedulerJCRunner(Class<?>[] configClasses, long delayTime) {
        this.configClasses = configClasses != null ? Arrays.copyOf(configClasses, configClasses.length) : null;
        this.delayTime = delayTime;
    }

    // 2026.02.28 KISA 보안취약점 조치
    public void setConfigClasses(Class<?>... configClasses) {
        this.configClasses = configClasses != null ? Arrays.copyOf(configClasses, configClasses.length) : null;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public void start() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        for (Class<?> configClass : configClasses) {
            context.register(configClass);
        }

        context.refresh();
        LOGGER.debug("### EgovSchedulerJCRunner EgovSchedulerJCRunner() Running Time: {} seconds", delayTime / 1000);

        boolean mustContinue = false;
        long realDelayTime = delayTime;

        if (delayTime < 0) {
            mustContinue = true;
            realDelayTime = -1 * delayTime;
        }

        if (mustContinue) {
            LOGGER.debug("### EgovSchedulerJCRunner EgovSchedulerJCRunner() Continue ");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(realDelayTime);
                } catch (InterruptedException ie) {
                    break;
                }
            }
            LOGGER.debug("### EgovSchedulerJCRunner EgovSchedulerJCRunner() End ");
        } else {
            try {
                LOGGER.debug("### EgovSchedulerJCRunner EgovSchedulerJCRunner() Sleeping Time: {} seconds", realDelayTime / 1000);
                Thread.sleep(realDelayTime);
                LOGGER.debug("### EgovSchedulerJCRunner EgovSchedulerJCRunner() Sleeping End ");
            } catch (InterruptedException e) {
                LOGGER.debug("[{}] EgovSchedulerJCRunner EgovSchedulerJCRunner() Sleeping has Interrupted : {}", e.getClass().getName(), e.getMessage());
            }
        }

        context.close();
    }

}
