package org.egovframe.rte.psl.dataaccess.resulthandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 쿼리결과를 파일로 출력하는 ResultHandler
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  최초생성
 */
@Scope("prototype")
@Component("fileWritingResultHandler")
public class FileWritingResultHandler implements ResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWritingResultHandler.class);

    @Resource(name = "schemaProperties")
    private Properties schemaProperties;

    private File file;
    private BufferedOutputStream bufferedOutput;
    private int totalCount = 0;

    @PostConstruct
    public void init() throws IOException {
        this.file = new File("./src/test/resources/META-INF/spring/" + schemaProperties.getProperty("outResultFile"));
        if (this.file.exists()) {
            this.file.delete();
            this.file.createNewFile();
        } else {
            this.file.createNewFile();
        }

        this.bufferedOutput = new BufferedOutputStream(new FileOutputStream(this.file));
    }

    public void handleResult(ResultContext context) {
        try {
            bufferedOutput.write(ToStringBuilder.reflectionToString(context.getResultObject()).getBytes());
            bufferedOutput.write("\n".getBytes());
            totalCount++;
        } catch (IOException e) {
            LOGGER.debug("[{}] FileWritingResultHandler handleResult() : {}", e.getClass().getName(), e.getMessage());
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void releaseResource() {
        try {
            if (bufferedOutput != null) {
                bufferedOutput.flush();
                bufferedOutput.close();
            }
        } catch (IOException e) {
            LOGGER.debug("[{}] FileWritingResultHandler releaseResource() : {}", e.getClass().getName(), e.getMessage());
        }
    }

}
