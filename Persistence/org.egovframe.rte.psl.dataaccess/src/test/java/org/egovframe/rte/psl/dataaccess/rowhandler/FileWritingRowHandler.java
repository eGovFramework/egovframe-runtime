package org.egovframe.rte.psl.dataaccess.rowhandler;

import com.ibatis.sqlmap.client.event.RowHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Scope("prototype")
@Component("fileWritingRowHandler")
public class FileWritingRowHandler implements RowHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWritingRowHandler.class);

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

    public void handleRow(Object rowObject) {
        try {
            bufferedOutput.write(ToStringBuilder.reflectionToString(rowObject).getBytes());
            bufferedOutput.write("\n".getBytes());
            totalCount++;
        } catch (IOException e) {
            LOGGER.debug("[{}] FileWritingRowHandler handleRow() : {}", e.getClass().getName(), e.getMessage());
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
            LOGGER.debug("[{}] FileWritingRowHandler releaseResource() : {}", e.getClass().getName(), e.getMessage());
        }
    }

}
