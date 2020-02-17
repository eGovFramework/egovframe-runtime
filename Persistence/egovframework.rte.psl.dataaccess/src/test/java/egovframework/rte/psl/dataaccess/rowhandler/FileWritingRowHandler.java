package egovframework.rte.psl.dataaccess.rowhandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ibatis.sqlmap.client.event.RowHandler;

@Scope("prototype")
@Component("fileWritingRowHandler")
public class FileWritingRowHandler implements RowHandler {

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

	public void handleRow(Object rowObject) {
		try {
			// FileUtils.writeStringToFile(this.file,
			// ToStringBuilder
			// .reflectionToString(rowObject));
			bufferedOutput.write(ToStringBuilder.reflectionToString(rowObject).getBytes());
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
