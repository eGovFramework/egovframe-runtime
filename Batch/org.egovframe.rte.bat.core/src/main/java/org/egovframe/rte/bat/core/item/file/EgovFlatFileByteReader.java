/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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

import org.egovframe.rte.bat.core.item.file.mapping.EgovByteLineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.NonTransientFlatFileException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Flat File을 Byte단위로 읽어들임 
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.10.20	배치실행개발팀		최초 생성
 * 2017.02.15	장동한			시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754
 * </pre>
 */
public class EgovFlatFileByteReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements ResourceAwareItemReaderItemStream<T>, InitializingBean {

	// slf4J logger 로 변경 : 2014.04.30
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovFlatFileByteReader.class);
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

	public static int LINE_CRLF = 2;
	private static final int UNIX_CRLF = 1;
	private static final int WINDOWS_CRLF = 2;

	private RecordSeparatorPolicy recordSeparatorPolicy = new SimpleRecordSeparatorPolicy();
	private EgovByteReaderFactory bufferedReaderFactory = new EgovByteReaderFactory();
	private Resource resource;
	private EgovByteReader reader;
	private EgovByteLineMapper<T> lineMapper;

	private int lineCount = 0;
	private int offset = 0;
	private int length = 0;
	private String[] comments = new String[] { "#" };
	private boolean noInput = false;
	private String encoding = DEFAULT_CHARSET;
	private boolean strict = true;
	byte[] b = null;

	public EgovFlatFileByteReader() {
		setName(ClassUtils.getShortName(FlatFileItemReader.class));
	}

	/**
	 * In strict mode the reader will throw an exception on
	 * {@link #open(org.springframework.batch.item.ExecutionContext)} if the
	 * input resource does not exist.
	 * @param strict false by default
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * Setter for line mapper. This property is required to be set.
	 * @param lineMapper maps line to item
	 */
	public void setLineMapper(EgovByteLineMapper<T> lineMapper) {
		this.lineMapper = lineMapper;
	}

	/**
	 * Setter for the encoding for this input source. Default value is
	 * {@link #DEFAULT_CHARSET}.
	 * @param encoding a properties object which possibly contains the encoding for this input file;
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * lengh
	 */
	public void setLength(int length) {
		this.length = length;
	}
		
	/**
	 * osType
	 */
	public void setOsType(String osType) {
		if (!osType.equalsIgnoreCase("WINDOWS")) {
			LINE_CRLF = UNIX_CRLF;
		} else {
			LINE_CRLF = WINDOWS_CRLF;
		}
	}

	/**
	 * Factory for the {@link BufferedReader} that will be used to extract lines
	 * from the file. The default is fine for plain text files, but this is a
	 * useful strategy for binary files where the standard BufferedReaader from
	 * java.io is limiting.
	 * @param bufferedReaderFactory the bufferedReaderFactory to set
	 */
	public void setBufferedReaderFactory(EgovByteReaderFactory bufferedReaderFactory) {
		this.bufferedReaderFactory = bufferedReaderFactory;
	}

	/**
	 * Setter for comment prefixes. Can be used to ignore header lines as well
	 * by using e.g. the first couple of column names as a prefix.
	 * @param comments an array of comment line prefixes.
	 */
	public void setComments(String[] comments) {
		this.comments = new String[comments.length];
		System.arraycopy(comments, 0, this.comments, 0, comments.length);
	}

	/**
	 * Public setter for the input resource.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Public setter for the recordSeparatorPolicy. Used to determine where the
	 * line endings are and do things like continue over a line ending if inside
	 * a quoted string.
	 * @param recordSeparatorPolicy the recordSeparatorPolicy to set
	 */
	public void setRecordSeparatorPolicy(RecordSeparatorPolicy recordSeparatorPolicy) {
		this.recordSeparatorPolicy = recordSeparatorPolicy;
	}

	/**
	 * Byte단위로 읽어들인 한 라인을 ValueObject로 매핑
	 * @return string corresponding to logical record according to
	 *         {@link #setRecordSeparatorPolicy(RecordSeparatorPolicy)} (might
	 *         span multiple lines in file).
	 */
	@Override
	protected T doRead() throws Exception {
		if (noInput) {
			return null;
		}
		byte[] line = readLine();
		if (line == null) {
			return null;
		} else {
			return lineMapper.mapLine(line, lineCount);
		}
	}

	/**
	 * OS타입에 따라 다른 인코딩 타입을 가진 파일을 읽어들이는 Method
	 * @return
	 */
	private byte[] readLine() {
		if (b == null) {
			b = new byte[length + LINE_CRLF];
		}

		int line = 0;

		try {
			line = this.reader.read(b, offset, length + LINE_CRLF);
			if (line < 0) {
				return null;
			}
			lineCount++;
		} catch (IOException e) {
			noInput = true;
			throw new NonTransientFlatFileException("Unable to read from resource: [" + resource + "]", e, line + "", lineCount);
		}

		return b;
	}

	@Override
	protected void doClose() throws Exception {		
		lineCount = 0;
		if (reader != null) {
			reader.close();
		}
	}

	@Override
	protected void doOpen() throws Exception {
		Assert.notNull(resource, "Input resource must be set");
		Assert.notNull(recordSeparatorPolicy, "RecordSeparatorPolicy must be set");

		noInput = true;

		if (!resource.exists()) {
			if (strict) {
				throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + resource);
			}
			// slf4J logger 로 변경 : 2014.04.30
			LOGGER.warn("Input resource does not exist {} ",  resource.getDescription());
			return;
		}

		if (!resource.isReadable()) {
			if (strict) {
				throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): " + resource);
			}
			// slf4J logger 로 변경 : 2014.04.30
			LOGGER.warn("Input resource is not readable {} ", resource.getDescription());
			return;
		}

		reader = bufferedReaderFactory.create(resource, encoding);
		noInput = false;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(lineMapper, "LineMapper is required");
	}

	@Override
	protected void jumpToItem(int itemIndex) throws Exception {
		for (int i = 0; i < itemIndex; i++) {
			readLine();
		}
	}

}
