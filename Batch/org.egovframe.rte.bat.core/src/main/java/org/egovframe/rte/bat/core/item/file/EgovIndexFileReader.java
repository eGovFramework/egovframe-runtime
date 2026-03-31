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

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 신용호
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.10.30	신용호				최초 생성
 * </pre>
 * @since 2017.10.30
 */
public class EgovIndexFileReader<T> implements ItemStreamReader<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovIndexFileReader.class);
    private static final String INDEX_FILE_UNIQUE_KEY = "_NDX";
    private static final int INDEX_NUMBER_LENGTH = 14;
    private ItemReader<T> reader;
    private String stepName;

    private ResourceLoader resourceLoader;
    private Resource resource;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

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
        configureReaderIndexResouce();
    }

    @Override
    public void close() throws ItemStreamException {
        if (this.reader instanceof ItemStream) {
            ((ItemStream) this.reader).close();
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // ItemReader 생성
        makeIndexItemReader();
        if (this.reader instanceof ItemStream) {
            ((ItemStream) this.reader).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (this.reader instanceof ItemStream) {
            ((ItemStream) this.reader).update(executionContext);
        }
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return reader.read();
    }

    // 2026.02.28 KISA 보안취약점 조치
    // Index 파일용 단일 Reader 생성 - Reader 생성 실패 시 자원 해제
    private void makeIndexItemReader() throws ItemStreamException {
        this.reader = new FlatFileItemReader<T>();
        try {
            ((FlatFileItemReader<T>) this.reader).setLineMapper(lineMapper);
            ((FlatFileItemReader<T>) this.reader).setResource(resource);
            ((FlatFileItemReader<T>) this.reader).afterPropertiesSet();
        } catch (IllegalStateException e) {
            closeReaderOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] FlatFileItemReader 설정이 완료되지 않았습니다. : " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            closeReaderOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] FlatFileItemReader 설정값이 올바르지 않습니다. : " + e.getMessage(), e);
        } catch (Exception e) {
            closeReaderOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] File을 read 하기 위한 FlatFileItemReader 생성에 실패 하였습니다. : " + e.getMessage(), e);
        }
    }

    // 2026.03.24 KISA 보안취약점 조치
    // makeIndexItemReader 초기화 실패 시 생성된 reader의 자원을 해제한다.
    private void closeReaderOnFailure() {
        if (this.reader != null) {
            try {
                if (this.reader instanceof ItemStream) {
                    ((ItemStream) this.reader).close();
                }
            } catch (ItemStreamException e) {
                LOGGER.debug("[" + e.getClass().getSimpleName() + "] Reader close failed during cleanup after init failure : {}", e.getMessage());
            } catch (Exception e) {
                LOGGER.debug("[" + e.getClass().getSimpleName() + "] Reader close failed during cleanup after init failure : {}", e.getMessage());
            } finally {
                this.reader = null;
            }
        }
    }

    private void configureReaderIndexResouce() {
        // Input Resource Type에 따라 필요한 설정 값 세팅
        int index = indexResource.lastIndexOf("/");
        String resourceFileName = indexResource.substring(index + 1);
        String resourceDirectory = indexResource.substring(0, index);

        // 파일명에서 Index 값 즉, (?)로 정의된 값을 찾는다.
        Matcher matcher = Pattern.compile("\\([+-]?[0-9]+\\)").matcher(resourceFileName);

        while (matcher.find()) {
            String findIndex = matcher.group();
            indexKey = findIndex.substring(1, findIndex.length() - 1);
        }

        // (일련번호)부분을 추출하고 검증한다.
        if (indexKey == null || "".equals(indexKey) || indexKey.length() == 0) {
            throw new RuntimeException("The specified index number of the file can not be found(Resouce Property). example : 'FileName'+'_NDX(number)'");
        }

        // "파일명" + "_NDX" + "(순번)" + ".확장자"
        // 1) 파일명은 영문,숫자및 _ 허용
        // 2) _NDX가 들어가야 일련번호(GDG) 파일로 인식
        // 3) (순번)은 반드시 음수 또는 양수의 숫자
        // 4) ".확장자" 생략 가능
        Pattern p = Pattern.compile("^[a-zA-Z0-9_]+" + INDEX_FILE_UNIQUE_KEY + "\\([+-]?[0-9]+\\)[a-zA-Z0-9_.]*$");
        Matcher m = p.matcher(resourceFileName);
        if (m.find()) {
            index = resourceFileName.indexOf(INDEX_FILE_UNIQUE_KEY);
            indexFileHeader = resourceFileName.substring(0, index + INDEX_FILE_UNIQUE_KEY.length());
        } else {
            throw new RuntimeException("The FileNamePattern is invalid(Resouce Property). example : 'FileName'+'_NDX(number)'");
        }

        Resource resourceIndex = resourceLoader.getResource(resourceDirectory);
        File dir = null;

        try {
            dir = resourceIndex.getFile();
        } catch (IOException e) {
            throw new RuntimeException("Resource File Exception : " + e.getMessage());
        }

        // 지정된 디렉토리에서 규격에 맞는 파일을 피터링하여 파일목록을 추출한다.
        File[] fileInfoList = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(indexFileHeader);
            }
        });
        Boolean isEmpty = ArrayUtils.isEmpty(fileInfoList);
        if (isEmpty || fileInfoList.length == 0) {
            throw new RuntimeException("The file resource not found in '" + resourceDirectory + "'");
        }
        List<String> indexFileList = new ArrayList<String>();
        // 추출된 파일목록중 디렉토리인경우 제외시킨다.
        for (File file : fileInfoList) {
            if (file.isFile()) {
                // 파일이 있다면 파일 이름 출력
                indexFileList.add(file.getName());
            } else if (file.isDirectory()) {
                LOGGER.debug("### EgovIndexFileReader RconfigureReaderIndexResouce() Direcotry : {}", file.getName());
            }
        }

        Collections.sort(indexFileList);
        if (indexFileList.isEmpty()) {
            throw new RuntimeException("The file resource not found in '" + resourceDirectory + "'. example : 'FileName'+'_NDX(number)'");
        }

        for (int i = 0; i < indexFileList.size(); i++) {
            LOGGER.debug("### EgovIndexFileReader RconfigureReaderIndexResouce() Sort File ({}) : {}", i, indexFileList.get(i));
        }

        int indexKeyNo = Integer.parseInt(indexKey);
        if (indexKeyNo > 0) {
            throw new RuntimeException("can not use '+'index on the ItemReader. (" + indexKey + ")");
        }
        if (indexFileList.size() + indexKeyNo <= 0) {
            throw new RuntimeException("The file specified by the index does not exist. Please check the range. (" + indexKey + ")");
        }

        String nominatedFileName = indexFileList.get(indexFileList.size() + indexKeyNo - 1);

        Pattern patternFile = Pattern.compile("^[a-zA-Z0-9_]+" + INDEX_FILE_UNIQUE_KEY + "_" + "\\d{" + INDEX_NUMBER_LENGTH + "}[a-zA-Z0-9_.]*$");
        Matcher matcherFile = patternFile.matcher(nominatedFileName);

        if (!matcherFile.find()) {
            LOGGER.debug("### EgovIndexFileReader RconfigureReaderIndexResouce() Result : index file type - FAIL");
            throw new RuntimeException("The rules for filenames that ItemReader should read are not correct. example : 'FileName'+'_NDX_'+'YYYYMMDDhhmmss'");
        } else {
            LOGGER.debug("### EgovIndexFileReader RconfigureReaderIndexResouce() Result : index file type - OK");
        }

        String convertResource = org.apache.commons.io.FilenameUtils.concat(resourceDirectory, nominatedFileName);
        LOGGER.debug("### EgovIndexFileReader configureReaderIndexResouce() Convert Resource Path : {}", convertResource);

        this.resource = resourceLoader.getResource(convertResource);
    }

}

