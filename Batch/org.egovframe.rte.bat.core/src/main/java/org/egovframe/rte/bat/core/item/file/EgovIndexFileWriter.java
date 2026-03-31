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
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;

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
 * 2017.11.02	신용호				최초 생성
 * </pre>
 * @since 2017.11.02
 */
public class EgovIndexFileWriter<T> implements ItemStreamWriter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovIndexFileWriter.class);
    private static final String INDEX_FILE_UNIQUE_KEY = "_NDX";
    private static final int INDEX_NUMBER_LENGTH = 14;
    private ItemWriter<T> writer;
    private String stepName;

    private ResourceLoader resourceLoader;
    private JobParameters jobParameters;
    private String writerResourceType;
    private Resource resource;
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
    public void beforeStep(StepExecution stepExecution) throws ClassNotFoundException {
        this.stepName = stepExecution.getStepName();
        configureWriterIndexResouce();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // ItemReader 생성
        makeIndexItemWriter();
        if (this.writer instanceof ItemStream) {
            ((ItemStream) this.writer).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (this.writer instanceof ItemStream) {
            ((ItemStream) this.writer).update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (this.writer instanceof ItemStream) {
            ((ItemStream) this.writer).close();
        }
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        this.writer.write(chunk);
    }

    // 2026.02.28 KISA 보안취약점 조치
    // Index 파일용 단일 Writer 생성 - Writer 생성 실패 시 자원 해제
    private void makeIndexItemWriter() throws ItemStreamException {
        this.writer = new FlatFileItemWriter<T>();
        try {
            ((FlatFileItemWriter<T>) this.writer).setResource((WritableResource) this.resource);
            ((FlatFileItemWriter<T>) this.writer).setLineAggregator(lineAggregator);
            ((FlatFileItemWriter<T>) this.writer).afterPropertiesSet();
        } catch (IllegalStateException e) {
            closeWriterOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] FlatFileItemWriter 설정이 완료되지 않았습니다. : " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            closeWriterOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] FlatFileItemWriter 설정값이 올바르지 않습니다. : " + e.getMessage(), e);
        } catch (Exception e) {
            closeWriterOnFailure();
            throw new ItemStreamException("[" + e.getClass().getSimpleName() + "] File을 write 하기 위한 FlatFileItemWriter 생성에 실패 하였습니다. : " + e.getMessage(), e);
        }
    }

    // 2026.03.24 KISA 보안취약점 조치
    // makeIndexItemWriter 초기화 실패 시 생성된 writer의 자원을 해제한다.
    private void closeWriterOnFailure() {
        if (this.writer != null) {
            try {
                if (this.writer instanceof ItemStream) {
                    ((ItemStream) this.writer).close();
                }
            } catch (ItemStreamException e) {
                LOGGER.debug("[" + e.getClass().getSimpleName() + "] Writer close failed during cleanup after init failure : {}", e.getMessage());
            } catch (Exception e) {
                LOGGER.debug("[" + e.getClass().getSimpleName() + "] Writer close failed during cleanup after init failure : {}", e.getMessage());
            } finally {
                this.writer = null;
            }
        }
    }

    private void configureWriterIndexResouce() {
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

        Resource resourceIndex = null;

        // 파일 시스템 경로인지 확인 (절대 경로이거나 상대 경로)
        if (resourceDirectory.startsWith("/") || resourceDirectory.startsWith("./") ||
                resourceDirectory.startsWith("target/") || resourceDirectory.contains(":\\")) {
            // 파일 시스템 경로
            resourceIndex = new FileSystemResource(resourceDirectory);
        } else {
            // classpath 리소스
            resourceIndex = resourceLoader.getResource(resourceDirectory);
        }

        File dir = null;
        LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() resourceLoader.getResource() : {}", resourceIndex);

        try {
            dir = resourceIndex.getFile();
        } catch (IOException e) {
            LOGGER.debug("[{}] EgovIndexFileWriter configureWriterIndexResouce() : {}", e.getClass().getName(), e.getMessage());
            throw new RuntimeException("Resource File Exception : " + e.getMessage());
        }

        // 디렉토리가 존재하지 않으면 생성
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + resourceDirectory);
            }
            LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() Created directory : {}", resourceDirectory);
        }

        // 지정된 디렉토리에서 규격에 맞는 파일을 피터링하여 파일목록을 추출한다.
        File[] fileInfoList = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(indexFileHeader);
            }
        });

        int indexKeyNo = Integer.parseInt(indexKey);

        // (+1) 옵션이고 기존 파일이 없는 경우 초기 파일명 생성
        if (indexKeyNo == 1 && (ArrayUtils.isEmpty(fileInfoList) || fileInfoList.length == 0)) {
            LOGGER.debug("### EgovIndexFileWriter No existing index files found, creating initial filename for (+1) option");
            String initialFileName = generateInitialIndexFilename(resourceFileName);
            String convertResource = org.apache.commons.io.FilenameUtils.concat(resourceDirectory, initialFileName);
            LOGGER.debug("### EgovIndexFileWriter Convert Resource Path : {}", convertResource);

            // 파일 시스템 경로인지 확인하여 적절한 리소스 생성
            if (resourceDirectory.startsWith("/") || resourceDirectory.startsWith("./") ||
                    resourceDirectory.startsWith("target/") || resourceDirectory.contains(":\\")) {
                // 파일 시스템 경로
                this.resource = new FileSystemResource(convertResource);
            } else {
                // classpath 리소스
                this.resource = resourceLoader.getResource(convertResource);
            }
            return;
        }

        Boolean isEmpty = ArrayUtils.isEmpty(fileInfoList);
        if (isEmpty || fileInfoList.length == 0) {
            throw new RuntimeException("The file resource not found in '" + resourceDirectory + "'");
        }
        List<String> indexFileList = new ArrayList<String>();
        // 추출된 파일목록중 디렉토리인경우 제외시킨다.
        for (File file : fileInfoList) {
            if (file.isFile()) {
                // 파일이 있다면 파일 이름 출력
                LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() File : {}", file.getName());
                indexFileList.add(file.getName());
            } else if (file.isDirectory()) {
                LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() Direcotry : {}", file.getName());
            }
        }

        Collections.sort(indexFileList);

        if (indexFileList.isEmpty()) {
            throw new RuntimeException("The file resource not found in '" + resourceDirectory + "'. example : 'FileName'+'_NDX(number)'");
        }

        for (String s : indexFileList) {
            LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() Sort File {}", s);
        }

        if (indexFileList.size() + indexKeyNo <= 0) {
            throw new RuntimeException("The file specified by the index does not exist. Please check the range. (" + indexKey + ")");
        }

        String nominatedFileName = "";
        if (indexKeyNo <= 0) {
            nominatedFileName = indexFileList.get(indexFileList.size() + indexKeyNo - 1);
        } else if (indexKeyNo == 1) {
            // 파일명 생성 규칙 검증후 새로운 파일명 생성
            nominatedFileName = indexFileList.get(indexFileList.size() - 1);
        } else {
            throw new RuntimeException("The file specified by the index does not exist. Please check the range. (" + indexKey + ")");
        }

        Pattern patternFile = Pattern.compile("^[a-zA-Z0-9_]+" + INDEX_FILE_UNIQUE_KEY + "_" + "\\d{" + INDEX_NUMBER_LENGTH + "}[a-zA-Z0-9_.]*$");
        Matcher matcherFile = patternFile.matcher(nominatedFileName);

        if (!matcherFile.find()) {
            LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() Result : Index File Type - FAIL");
            throw new RuntimeException("The rules for filenames that ItemReader should read are not correct. example : 'FileName'+'_NDX_'+'YYYYMMDDhhmmss'");
        } else {
            LOGGER.debug("### EgovIndexFileWriter configureWriterIndexResouce() Result : Index File Type - OK");
        }

        if (indexKeyNo == 1) {
            nominatedFileName = generateNewIndexFilename(nominatedFileName);
        }

        String convertResource = org.apache.commons.io.FilenameUtils.concat(resourceDirectory, nominatedFileName);

        // 파일 시스템 경로인지 확인하여 적절한 리소스 생성
        if (resourceDirectory.startsWith("/") || resourceDirectory.startsWith("./") || resourceDirectory.startsWith("target/") || resourceDirectory.contains(":\\")) {
            // 파일 시스템 경로
            this.resource = new FileSystemResource(convertResource);
        } else {
            // classpath 리소스
            this.resource = resourceLoader.getResource(convertResource);
        }
    }

    // (+1) 옵션이고 기존 파일이 없는 경우 초기 파일명을 생성한다.
    private String generateInitialIndexFilename(String resourceFileName) {
        // 파일명에서 확장자 추출 (괄호 이전 부분에서)
        String baseFileName = resourceFileName;
        int parenIndex = resourceFileName.indexOf('(');
        if (parenIndex > 0) {
            baseFileName = resourceFileName.substring(0, parenIndex);
        }

        String extension = "";
        int lastDotIndex = baseFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = baseFileName.substring(lastDotIndex);
        } else {
            // 확장자가 없으면 기본적으로 .csv 사용
            extension = ".csv";
        }

        // 현재 시간을 기반으로 초기 인덱스 생성 (YYYYMMDDhhmmss 형식)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        String initialIndex = sdf.format(new java.util.Date());

        return indexFileHeader + "_" + initialIndex + extension;
    }

    // resource에서 (+1)로 지정되었으면
    // 최근 파일(0)에서 YYYYMMDDhhmmss +1을 하여 파일명을 새로 생성한다.
    private String generateNewIndexFilename(String nominatedFilenameOld) {
        String nominatedFilenameNew = "";
        String[] arrFileNamePart = nominatedFilenameOld.split(INDEX_FILE_UNIQUE_KEY + "_");

        String indexValue = arrFileNamePart[1].substring(0, INDEX_NUMBER_LENGTH);
        long indexValueNew = Long.parseLong(indexValue) + 1;

        String extValue = "";
        if (arrFileNamePart[1].length() > INDEX_NUMBER_LENGTH) {
            extValue = arrFileNamePart[1].substring(INDEX_NUMBER_LENGTH);
        }

        nominatedFilenameNew = arrFileNamePart[0] + INDEX_FILE_UNIQUE_KEY + "_" + indexValueNew + extValue;

        return nominatedFilenameNew;
    }

}
