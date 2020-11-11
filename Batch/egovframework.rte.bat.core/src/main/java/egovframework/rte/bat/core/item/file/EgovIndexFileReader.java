package egovframework.rte.bat.core.item.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author 신용호
 * @since 2017.10.30
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일        수정자           수정내용
 *  -------       --------          ---------------------------
 *   2017.10.30    신용호           최초 생성
 *
 * </pre>
 */
public class EgovIndexFileReader<T> implements ItemStreamReader<T>{

	// 실행시 사용하는 Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovIndexFileReader.class);
	
	private static final String INDEX_FILE_UNIQUE_KEY = "_NDX";
	private static final int INDEX_NUMBER_LENGTH = 14;

	// 실제 동작하는 Reader
	private ItemReader<T> reader;

	// 공통 설정
	private String stepName;

	@Autowired
	private ResourceLoader resourceLoader;
	
	// File 입력인 경우 사용되는 설정
	private Resource resource;		// 공통
	private DefaultLineMapper<T> lineMapper;
	private String indexResource;
	private String indexFileHeader = "";
	private String indexKey = "0";

	public String getIndexResource() {
		return indexResource;
	}

	public void setIndexResource(String indexResource) {
		this.indexResource = indexResource;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public DefaultLineMapper<T> getLineMapper() {
		return lineMapper;
	}

	public void setLineMapper(DefaultLineMapper<T> lineMapper) {
		this.lineMapper = lineMapper;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
	    this.stepName = stepExecution.getStepName();
	    //this.jobParameters = stepExecution.getJobParameters();

	    // Index Resource 설정처리
	    configureReaderIndexResouce();
	}

	@Override
	public void close() throws ItemStreamException {
		if(this.reader instanceof ItemStream) {
			((ItemStream) this.reader).close();
		}
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		// ItemReader 생성
		makeIndexItemReader();

		if(this.reader instanceof ItemStream) {
			((ItemStream) this.reader).open(executionContext);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		if(this.reader instanceof ItemStream) {
			((ItemStream) this.reader).update(executionContext);
		}
	}

	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		return reader.read();
	}

	//@SuppressWarnings({ "rawtypes", "unchecked" })
	private void makeIndexItemReader() {

		this.reader = new FlatFileItemReader<T>();
		((FlatFileItemReader<T>)this.reader).setLineMapper(lineMapper);
		((FlatFileItemReader<T>)this.reader).setResource(resource);

		try {
			((FlatFileItemReader<T>)this.reader).afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException("["+e.getClass()+"] File을 read 하기 위한 FlatFileItemReader 생성에 실패 하였습니다.("+e.getMessage()+")");
		}

	}
	
	private void configureReaderIndexResouce() {

	    // Input Resource Type에 따라 필요한 설정 값 세팅
		LOGGER.debug(">>>>> indexResource = " + indexResource);
		int index = indexResource.lastIndexOf("/");
		String resourceFileName = indexResource.substring(index+1);
		String resourceDirectory = indexResource.substring(0,index);
		LOGGER.debug(">>>>> fileName = " + resourceFileName);
		LOGGER.debug(">>>>> resourceDirectory = " + resourceDirectory);
		
		// 파일명에서 Index 값 즉, (?)로 정의된 값을 찾는다.
		Matcher matcher = Pattern.compile("\\([+-]?[0-9]+\\)").matcher( resourceFileName );

		while (matcher.find()) {
			String findIndex = matcher.group();
			indexKey = findIndex.substring(1, findIndex.length() - 1);
			LOGGER.debug("=================>>>>>>>>>>>>>"+indexKey);
		}
		// (일련번호)부분을 추출하고 검증한다.
		if ( indexKey==null || "".equals(indexKey) || indexKey.length()==0 ) {
			throw new RuntimeException("The specified index number of the file can not be found(Resouce Property). example : 'FileName'+'_NDX(number)'");
		}
		
		// "파일명" + "_NDX" + "(순번)" + ".확장자"
		// 1) 파일명은 영문,숫자및 _ 허용
		// 2) _NDX가 들어가야 일련번호(GDG) 파일로 인식
		// 3) (순번)은 반드시 음수 또는 양수의 숫자
		// 4) ".확장자" 생략 가능
		Pattern p = Pattern.compile("^[a-zA-Z0-9_]+"+INDEX_FILE_UNIQUE_KEY+"\\([+-]?[0-9]\\)[a-zA-Z0-9_.]*$");
        Matcher m = p.matcher(resourceFileName);
        
        if(m.find())
        {
        	index = resourceFileName.indexOf(INDEX_FILE_UNIQUE_KEY);
        	indexFileHeader = resourceFileName.substring(0,index+INDEX_FILE_UNIQUE_KEY.length());
        	
        	LOGGER.debug(">>>>> fileHeader = "+indexFileHeader);

        	LOGGER.debug(">>>>> fileName Pattern is OK !!!");
        }
        else
        {
        	LOGGER.debug(">>>>> fileName Pattern is invalid !!!");
			throw new RuntimeException("The FileNamePattern is invalid(Resouce Property). example : 'FileName'+'_NDX(number)'");
        }
        
        //Resource resource = new FileSystemResource(resourceDirectory);
        //LOGGER.debug(">>>>> class path3 = " + new ClassPathResource(resourceDirectory));
        //String resourcePath = ClassLoader.getSystemResource(resourceDirectory).getPath();
        //Resource resource = new FileSystemResource(resourcePath);

        Resource resourceIndex = resourceLoader.getResource(resourceDirectory);
        
        File dir = null;
    	LOGGER.debug(">>>>> resourceLoader.getResource(() = " + resourceIndex);
    	
		try {
			//LOGGER.debug(">>>>> resourceLoader.getResource(() = " + resource.getURL().getPath().toString());
			//LOGGER.debug(">>>>> resourceLoader.getResource(() = " + resourceIndex.getFile().getAbsolutePath());
    		//dir = new File(resourceDirectory);
			dir = resourceIndex.getFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LOGGER.debug(">>>>> error : " + e.getMessage());
			throw new RuntimeException("Resource File Exception : "+e.getMessage());
		}
        
		// 지정된 디렉토리에서 규격에 맞는 파일을 피터링하여 파일목록을 추출한다.
		File[] fileInfoList = dir.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){

				return name.startsWith(indexFileHeader);
			}

		});
		if (fileInfoList == null || fileInfoList.length == 0) {
			throw new RuntimeException("The file resource not found in '"+resourceDirectory+"'");
		}
		List<String> indexFileList = new ArrayList<String>();

		// 추출된 파일목록중 디렉토리인경우 제외시킨다.
		for (int i = 0; i < fileInfoList.length; i++) {

			File file = fileInfoList[i];

			if (file.isFile()) {

				// 파일이 있다면 파일 이름 출력
				LOGGER.debug(">>>>> File = " + file.getName());
				indexFileList.add(file.getName());

			} else if (file.isDirectory()) {

				LOGGER.debug(">>>>> Direcotry = " + file.getName());
			}

		}
		Collections.sort(indexFileList);

		if ( indexFileList.size() == 0 ) {
			throw new RuntimeException("The file resource not found in '"+resourceDirectory+"'. example : 'FileName'+'_NDX(number)'");
		}
		
		for (int i = 0; i < indexFileList.size(); i++) {
			LOGGER.debug(">>>>> Sort File ("+i+") = "+ indexFileList.get(i));
		}
		
		int indexKeyNo =  Integer.parseInt(indexKey);
		if (indexKeyNo > 0 ) {
			LOGGER.debug("Err : +1 이상 불가");
			throw new RuntimeException("can not use '+'index on the ItemReader. ("+indexKey+")");
		}
		if ( indexFileList.size()+indexKeyNo <= 0  ) {
			LOGGER.debug("Err : index 지정 범위 초과");
			throw new RuntimeException("The file specified by the index does not exist. Please check the range. ("+indexKey+")");
		}
		
		String nominatedFileName = indexFileList.get(indexFileList.size()+indexKeyNo-1);
		LOGGER.debug("=====>>>>> Result File: "+nominatedFileName);

		Pattern patternFile = Pattern.compile("^[a-zA-Z0-9_]+"+INDEX_FILE_UNIQUE_KEY+"_"+"\\d{"+INDEX_NUMBER_LENGTH+"}[a-zA-Z0-9_.]*$");
        Matcher matcherFile = patternFile.matcher(nominatedFileName);
        
        if(!matcherFile.find()){
        	LOGGER.debug("Result : index 파일 유형 - FAIL");
			throw new RuntimeException("The rules for filenames that ItemReader should read are not correct. example : 'FileName'+'_NDX_'+'YYYYMMDDhhmmss'");
        } else {
        	LOGGER.debug("Result : index 파일 유형 - OK");
        }

        String convertResource = org.apache.commons.io.FilenameUtils.concat(resourceDirectory,nominatedFileName);
    	LOGGER.debug("Convert Resource Path = "+convertResource);
    	
    	this.resource = resourceLoader.getResource(convertResource);

	}

}

