package org.egovframe.rte.psl.dataaccess.resulthandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 쿼리결과를 파일로 출력하는 ResultHandler
 * 
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  최초생성
 * 
 */
@Scope("prototype")
@Component("fileWritingResultHandler")
public class FileWritingResultHandler implements ResultHandler {

	private File file;

	private BufferedOutputStream bufferedOutput = null;

	@Resource(name = "schemaProperties")
	private Properties schemaProperties;

	private int totalCount = 0;

	@PostConstruct
	public void init() throws IOException {

		this.file = new File("./src/test/resources/META-INF/testdata/" + schemaProperties.getProperty("outResultFile"));
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
			// FileUtils.writeStringToFile(this.file, ToStringBuilder.reflectionToString(context));
			// bufferedOutput.write(ToStringBuilder.reflectionToString(context)).getBytes()); 과 동일
			bufferedOutput.write(ToStringBuilder.reflectionToString(context.getResultObject()).getBytes());
			bufferedOutput.write("\n".getBytes());
			totalCount++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void releaseResource() {
		// Close the BufferedOutputStream
		try {
			if (bufferedOutput != null) {
				bufferedOutput.flush();
				bufferedOutput.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
