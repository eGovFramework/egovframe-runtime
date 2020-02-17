package egovframework.rte.bat.core.item.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import egovframework.rte.bat.core.item.database.EgovJdbcBatchItemWriter;
import egovframework.rte.bat.core.item.database.support.EgovMethodMapItemPreparedStatementSetter;
import egovframework.rte.bat.core.item.file.transform.EgovFieldExtractor;
import egovframework.rte.bat.core.item.file.transform.EgovFixedLengthLineAggregator;

/**
 * @author 신용호
 * @since 2017.11.02
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일        수정자           수정내용
 *  -------       --------          ---------------------------
 *   2017.11.02    신용호           최초 생성
 *
 * </pre>
 */
public class EgovIndexFileWriter<T> implements ItemStreamWriter<T>{

	// 실행시 사용하는 Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovIndexFileWriter.class);
	
	private static final String INDEX_FILE_UNIQUE_KEY = "_NDX";
	private static final int INDEX_NUMBER_LENGTH = 14;	
	
	// 실제 동작하는 Reader
	private ItemWriter<T> writer;

	// 공통 설정
	private String stepName;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
	private JobParameters jobParameters;
	private String writerResourceType;
	
	// File 입력인 경우 사용되는 설정
	private Resource resource;		// 공통
	private LineAggregator<T> lineAggregator;
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


	public LineAggregator<T> getLineAggregator() {
		return lineAggregator;
	}


	public void setLineAggregator(LineAggregator<T> lineAggregator) {
		this.lineAggregator = lineAggregator;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) throws ClassNotFoundException{
	    this.stepName = stepExecution.getStepName();
	    //this.jobParameters = stepExecution.getJobParameters();
	    
	    
	    // Index Resource 설정처리
	    configureWriterIndexResouce();
	}
	
	
	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		// ItemReader 생성
		makeIndexItemWriter();
		
		if(this.writer instanceof ItemStream) {
			((ItemStream) this.writer).open(executionContext);
		}
	}

	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
		if(this.writer instanceof ItemStream) {
			((ItemStream) this.writer).update(executionContext);
		}
	}

	@Override
	public void close() throws ItemStreamException {
		if(this.writer instanceof ItemStream) {
			((ItemStream) this.writer).close();
		}
	}

	@Override
	public void write(List<? extends T> items) throws Exception {
		this.writer.write(items);
	}

	
	
	private void makeIndexItemWriter() {
			
		this.writer = new FlatFileItemWriter<T>();
		((FlatFileItemWriter<T>) this.writer).setResource(this.resource);
		((FlatFileItemWriter<T>) this.writer).setLineAggregator(lineAggregator);

		try {
			((FlatFileItemWriter<T>) this.writer).afterPropertiesSet();
		} catch (Exception e) {
			// throw new RuntimeException(this.writerResourceType +
			// " 타입의 File을 write 하기 위한 FlatFileItemWriter 생성에 실패 하였습니다.");
			// 2017.02.15 장동한 시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
			throw new RuntimeException(
					"["
							+ e.getClass()
							+ "]"
							+ this.writerResourceType
							+ " 타입의 File을 write 하기 위한 FlatFileItemWriter 생성에 실패 하였습니다.("
							+ e.getMessage() + ")");
		}
	
	}
	
	private void configureWriterIndexResouce() {

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
		if (fileInfoList.length == 0) {
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
//		if (indexKeyNo > 0 ) {
//			LOGGER.debug("Err : +1 이상 불가");
//			throw new RuntimeException("can not use '+'index on the ItemReader. ("+indexKey+")");
//		}
		if ( indexFileList.size()+indexKeyNo <= 0  ) {
			LOGGER.debug("Err : index 지정 범위 초과");
			throw new RuntimeException("The file specified by the index does not exist. Please check the range. ("+indexKey+")");
		}
		
		String nominatedFileName = "";
		if ( indexKeyNo <= 0 ) {
			nominatedFileName = indexFileList.get(indexFileList.size()+indexKeyNo-1);
			LOGGER.debug("=====>>>>> Result File: "+nominatedFileName);
		} else if ( indexKeyNo == 1 ) {
			// 파일명 생성 규칙 검증후 새로운 파일명 생성
			nominatedFileName = indexFileList.get(indexFileList.size()-1);
		} else {
			throw new RuntimeException("The file specified by the index does not exist. Please check the range. ("+indexKey+")");
		}

		Pattern patternFile = Pattern.compile("^[a-zA-Z0-9_]+"+INDEX_FILE_UNIQUE_KEY+"_"+"\\d{"+INDEX_NUMBER_LENGTH+"}[a-zA-Z0-9_.]*$");
        Matcher matcherFile = patternFile.matcher(nominatedFileName);
        
        if(!matcherFile.find()){
        	LOGGER.debug("Result : index 파일 유형 - FAIL");
			throw new RuntimeException("The rules for filenames that ItemReader should read are not correct. example : 'FileName'+'_NDX_'+'YYYYMMDDhhmmss'");
        } else {
        	LOGGER.debug("Result : index 파일 유형 - OK");
        }

        if ( indexKeyNo == 1 ) {
        	nominatedFileName = generateNewIndexFilename(nominatedFileName);
        }
        
        String convertResource = org.apache.commons.io.FilenameUtils.concat(resourceDirectory,nominatedFileName);
    	LOGGER.debug("Convert Resource Path = "+convertResource);
    	
    	this.resource = resourceLoader.getResource(convertResource);

	}
	
	// resource에서 (+1)로 지정되었으면 
	// 최근 파일(0)에서 YYYYMMDDhhmmss +1을 하여 파일명을 새로 생성한다.
	private String generateNewIndexFilename(String nominatedFilenameOld) {

		String nominatedFilenameNew = "";
		String[] arrFileNamePart = nominatedFilenameOld.split(INDEX_FILE_UNIQUE_KEY+"_");
		LOGGER.debug(">>>>> arrFileNamePart = "+arrFileNamePart[0]);
		LOGGER.debug(">>>>> arrFileNamePart = "+arrFileNamePart[1]);
		
		String indexValue = arrFileNamePart[1].substring(0, INDEX_NUMBER_LENGTH);
		LOGGER.debug(">>>>> indexValue = "+indexValue);
		long indexValueNew = Long.parseLong(indexValue)+1;
		LOGGER.debug(">>>>> indexValue = "+indexValueNew);

		String extValue = "";
		
		if ( arrFileNamePart[1].length() > INDEX_NUMBER_LENGTH ) {
			extValue = arrFileNamePart[1].substring(INDEX_NUMBER_LENGTH);	
		}
		nominatedFilenameNew = arrFileNamePart[0] + INDEX_FILE_UNIQUE_KEY+"_" + indexValueNew + extValue;
		LOGGER.debug(">>>>> nominatedFilenameNew = "+nominatedFilenameNew);
		
		return nominatedFilenameNew;
	}
}
