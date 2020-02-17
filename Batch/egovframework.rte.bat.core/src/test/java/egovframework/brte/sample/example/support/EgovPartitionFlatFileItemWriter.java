/*
 * Copyright 2006-2007 the original author or authors.
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

package egovframework.brte.sample.example.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.util.ExecutionContextUserSupport;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.batch.support.transaction.TransactionAwareBufferedWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Partition 작업 실행시 여러 쓰레드에서 공유하여 하나의 target파일에 Write함. writer 설정시 scope=step 을
 * 삭제하여 사용함.
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 * 
 *      <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 * 
 * </pre>
 */

public class EgovPartitionFlatFileItemWriter<T> extends ExecutionContextUserSupport implements ResourceAwareItemWriterItemStream<T>, InitializingBean {

	private static final boolean DEFAULT_TRANSACTIONAL = true;

	protected static final Log logger = LogFactory.getLog(EgovPartitionFlatFileItemWriter.class);

	private static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");

	private static final String WRITTEN_STATISTICS_NAME = "written";

	private static final String RESTART_DATA_NAME = "current.count";

	private Resource resource;

	private OutputState state = null;

	private LineAggregator<T> lineAggregator;

	private boolean saveState = true;

	// 같은 이름의 파일이 존재하면 삭제할지의 여부에 대한 초기값
	private boolean shouldDeleteIfExists = true;

	// 파일에 아무 내용도 쓰여지지 않았을 경우 삭제할지의 여부에 대한 초기값
	private boolean shouldDeleteIfEmpty = false;

	private String encoding = OutputState.DEFAULT_CHARSET;

	private FlatFileHeaderCallback headerCallback;

	private FlatFileFooterCallback footerCallback;

	private String lineSeparator = DEFAULT_LINE_SEPARATOR;

	private boolean transactional = DEFAULT_TRANSACTIONAL;

	private boolean append = false;

	// fileCount : file 이 열리고 닫힘에 다라 Counting
	// 0 일경우 정상적인 Closing이 가능
	private int fileCount = 0;

	// fileOpenTime : file이 최초로 open 될 때의 시간
	private long fileOpenTime = 0;

	// fileOpenTime : file이 최종으로 close 될 때의 시간
	private long fileCloseTime = 0;

	public EgovPartitionFlatFileItemWriter() {
		setName(ClassUtils.getShortName(EgovPartitionFlatFileItemWriter.class));
	}

	private boolean forceSync = false;
	
	/**
	 * 설정파일의 프로퍼티를 셋팅
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(lineAggregator, "A LineAggregator must be provided.");

		if (append) {
			shouldDeleteIfExists = false;
		}
	}

	/**
	 * lineSeparator 셋팅
	 * 
	 * @param lineSeparator
	 */
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	/**
	 * lineAggregator 셋팅
	 * 
	 * @param lineAggregator
	 */
	public void setLineAggregator(LineAggregator<T> lineAggregator) {
		this.lineAggregator = lineAggregator;
	}

	/**
	 * fileCount 셋팅
	 * 
	 * @param fileCount
	 */
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	/**
	 * resource 셋팅
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * encoding 셋팅
	 */
	public void setEncoding(String newEncoding) {
		this.encoding = newEncoding;
	}

	/**
	 * shouldDeleteIfExists 셋팅
	 */
	public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
		this.shouldDeleteIfExists = shouldDeleteIfExists;
	}

	/**
	 *
	 */
	public void setAppendAllowed(boolean append) {
		this.append = append;
		this.shouldDeleteIfExists = false;
	}

	/**
	 * 
	 * @param shouldDeleteIfEmpty
	 *            the flag value to set
	 */
	public void setShouldDeleteIfEmpty(boolean shouldDeleteIfEmpty) {
		this.shouldDeleteIfEmpty = shouldDeleteIfEmpty;
	}

	/**
	 * saveState 셋팅
	 */
	public void setSaveState(boolean saveState) {
		this.saveState = saveState;
	}

	/**
	 * headerCallback will be called before writing the first item to file.
	 * Newline will be automatically appended after the header is written.
	 */
	public void setHeaderCallback(FlatFileHeaderCallback headerCallback) {
		this.headerCallback = headerCallback;
	}

	/**
	 * footerCallback will be called after writing the last item to file, but
	 * before the file is closed.
	 */
	public void setFooterCallback(FlatFileFooterCallback footerCallback) {
		this.footerCallback = footerCallback;
	}

	/**
	 * transactional 셋팅
	 */
	public void setTransactional(boolean transactional) {
		this.transactional = transactional;
	}

	/**
	 * Write 수행 
	 * 기존의 state 의 상태를 얻어와 이어서 Write 수행 
	 * 여러 쓰레드에서 접근하게 되므로 synchronized 로 선점권 보장
	 * 
	 * @param items output Stream 에 쓰여실 itmes 리스트
	 */
	public synchronized void write(List<? extends T> items) throws Exception {

		if (!getOutputState().isInitialized()) {
			throw new WriterNotOpenException("Writer must be open before it can be written to");
		}

		if (logger.isDebugEnabled()) {
			logger.info("Writing to flat file with " + items.size() + " items.");
		}

		OutputState state = getOutputState();

		StringBuilder lines = new StringBuilder();
		int lineCount = 0;

		for (T item : items) {
			lines.append(lineAggregator.aggregate(item) + lineSeparator);
			lineCount++;
		}

		try {

			state.write(lines.toString());

		} catch (IOException e) {
			throw new WriteFailedException("Could not write data.  The file may be corrupt.", e);
		}
		state.linesWritten += lineCount;
	}

	/**
	 * Close 수행 
	 * state 의 상태와 fileCount 상태를 판단 후 최종 state를 Close 함
	 * 여러 쓰레드에서 접근하게 되므로 synchronized 로 선점권 보장
	 * @see ItemStream#close()
	 */
	public synchronized void close() {
		fileCount--; // increment for close Counting

		if (state != null && fileCount == 0) {
			try {

				if (footerCallback != null && state.outputBufferedWriter != null) {
					footerCallback.writeFooter(state.outputBufferedWriter);
					state.outputBufferedWriter.flush();
				}

			} catch (IOException e) {
				throw new ItemStreamException("Failed to write footer before closing", e);
			} finally {

				state.close();
				try {
					fileCloseTime = resource.getFile().lastModified();

				} catch (IOException e1) {
					logger.debug(e1);
				}

				if (state.linesWritten == 0 && shouldDeleteIfEmpty) {
					try {
						resource.getFile().delete();
					} catch (IOException e) {
						throw new ItemStreamException("Failed to delete empty file on close", e);
					}
				}

				state = null;

			}
		}
	}

	/**
	 * open 수행 
	 * 파일을 쓰기 위해서 state 상태를 판단 후 존재하지 않으면  doOpen 호출
	 * 여러 쓰레드에서 접근하게 되므로 synchronized 로 선점권 보장
	 * @see ItemStream#close()
	 */
	public synchronized void open(ExecutionContext executionContext) throws ItemStreamException {
		Assert.notNull(resource, "The resource must be set");

		fileCount++;

		if (!getOutputState().isInitialized()) {
			doOpen(executionContext);
		}
	}

	/**
	 * 실질적인 open 이 일어남
	 * state를 생성하고 BufferedWriter를 초기화 함
	 * @see ItemStream#close()
	 */
	private void doOpen(ExecutionContext executionContext) throws ItemStreamException {

		OutputState outputState = getOutputState();

		if (executionContext.containsKey(getKey(RESTART_DATA_NAME))) {
			outputState.restoreFrom(executionContext);
		}
		try {
			outputState.initializeBufferedWriter();
			fileOpenTime = resource.getFile().lastModified();
			// 일부 Thread 수행이 먼저 완료 되면 Close 수행되면서 Stream 이 닫히는 현상을 방지하기 위한 조건 
			if ((fileOpenTime - fileCloseTime) / 1000.0 < 1) {

				throw new IOException("Failed to initialize writer");
			}
		} catch (IOException ioe) {
			throw new ItemStreamException("Failed to initialize writer", ioe);
		}
		if (outputState.lastMarkedByteOffsetPosition == 0 && !outputState.appending) {
			if (headerCallback != null) {
				try {
					headerCallback.writeHeader(outputState.outputBufferedWriter);
					outputState.write(lineSeparator);
				} catch (IOException e) {
					throw new ItemStreamException("Could not write headers.  The file may be corrupt.", e);
				}
			}
		}
	}

	/**
	 * state의 상태를 갱신하고 다음에 쓰여질 position을 지정
	 * @see ItemStream#update(ExecutionContext)
	 */
	public void update(ExecutionContext executionContext) {
		if (state == null) {
			throw new ItemStreamException("ItemStream not open or already closed.");
		}

		Assert.notNull(executionContext, "ExecutionContext must not be null");

		if (saveState) {

			try {

				executionContext.putLong(getKey(RESTART_DATA_NAME), state.position());
			} catch (IOException e) {
				throw new ItemStreamException("ItemStream does not return current position properly", e);
			}

			executionContext.putLong(getKey(WRITTEN_STATISTICS_NAME), state.linesWritten);
		}
	}

	/**
	 * 파일을 쓰기 위한 OutputState 의 상태를 설정하여 를 넘겨줌
	 * @return
	 */
	private OutputState getOutputState() {
		if (state == null) {
			File file;
			try {
				file = resource.getFile();
			} catch (IOException e) {
				throw new ItemStreamException("Could not convert resource to file: [" + resource + "]", e);
			}
			Assert.state(!file.exists() || file.canWrite(), "Resource is not writable: [" + resource + "]");
			state = new OutputState();
			state.setDeleteIfExists(shouldDeleteIfExists);
			state.setAppendAllowed(append);
			state.setEncoding(encoding);
		}
		return (OutputState) state;
	}

	/**
	 * 
	 * Data Write 처리를 위한 OutputState
	 * OutputState를 기반으로 파일의 open,update,Write,Close 가 일어남
	 * 
	 * @author 배치실행개발팀
	 * @since 2012. 07.30
	 * @version 1.0
	 * @see
	 * 
	 */
	private class OutputState {

		// 기본 인코딩은 UTF-8.
		private static final String DEFAULT_CHARSET = "UTF-8";

		// FileOutputStream
		private FileOutputStream os;

		// bufferedWriter
		Writer outputBufferedWriter;

		// 파일연결 채널
		FileChannel fileChannel;

		// 기본 인코딩은 UTF-8.
		String encoding = DEFAULT_CHARSET;

		// restart 플래그
		boolean restarted = false;

		// Position을 셋팅하기 위한 초기값
		long lastMarkedByteOffsetPosition = 0;

		// 쓰여진 라인수의 초기값
		long linesWritten = 0;

		// 기존에 같은 이름의 파일이 존재하면 삭제여부 설정
		boolean shouldDeleteIfExists = true;

		boolean initialized = false;

		private boolean append = false;

		private boolean appending = false;

		/**
		 * 파일을 쓸 위치를 정하는 position을 구한다.
		 */
		public long position() throws IOException {
			long pos = 0;

			if (fileChannel == null) {
				return 0;
			}

			outputBufferedWriter.flush();
			pos = fileChannel.position();

			if (transactional) {
				pos += ((TransactionAwareBufferedWriter) outputBufferedWriter).getBufferSize();
			}

			return pos;

		}

		/**
		 * AppendAllowed을 세팅한다.
		 * 
		 * @param append
		 */
		public void setAppendAllowed(boolean append) {
			this.append = append;
		}

		/**
		 * restart를 위해 "RESTART_DATAㄴ_NAME"의 키값으로 되어있는 정보를 가져온다.
		 * 
		 * @param executionContext
		 */
		public void restoreFrom(ExecutionContext executionContext) {
			lastMarkedByteOffsetPosition = executionContext.getLong(getKey(RESTART_DATA_NAME));
			restarted = true;
		}

		/**
		 * DeleteIfExists설정을 세팅한다.
		 * 
		 * @param shouldDeleteIfExists
		 */
		public void setDeleteIfExists(boolean shouldDeleteIfExists) {
			this.shouldDeleteIfExists = shouldDeleteIfExists;
		}

		/**
		 * 인코딩을 세팅한다.
		 * 
		 * @param encoding
		 */
		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		/**
		 * outputBufferedWriter를 닫는다
		 * 
		 */
		public void close() {

			initialized = false;
			restarted = false;
			try {

				if (outputBufferedWriter != null) {
					outputBufferedWriter.close();
				}
			} catch (IOException ioe) {
				throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
			} finally {
				if (!transactional) {
					closeStream();
				}
			}
		}

		/**
		 * 파일의 연결 및 FileOutputStream을 닫는다.
		 */
		private void closeStream() {
			try {
				if (fileChannel != null) {
					fileChannel.close();
				}
			} catch (IOException ioe) {
				throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (IOException ioe) {
					throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
				}
			}
		}

		/**
		 * line을 write 하고 flush 처리한다.
		 * 
		 * @param line
		 * @throws IOException
		 */
		public void write(String line) throws IOException {

			if (!initialized) {
				initializeBufferedWriter();
			}
			outputBufferedWriter.write(line);

			outputBufferedWriter.flush();
		}

		/**
		 * file 연결을 끊고, 다음에 쓰여질 위치를 지정한다.
		 * 
		 * @throws IOException
		 */
		public void truncate() throws IOException {
			fileChannel.truncate(lastMarkedByteOffsetPosition);
			fileChannel.position(lastMarkedByteOffsetPosition);
		}

		/**
		 * BufferedWriter 초기 설정한다.
		 * 
		 * @throws IOException
		 */
		private void initializeBufferedWriter() throws IOException {
			File file = resource.getFile();

			FileUtils.setUpOutputFile(file, restarted, append, shouldDeleteIfExists);

			os = new FileOutputStream(file.getAbsolutePath(), true);
			fileChannel = os.getChannel();

			outputBufferedWriter = getBufferedWriter(fileChannel, encoding);
			outputBufferedWriter.flush();

			if (append) {
				if (file.length() > 0) {
					appending = true;
				}
			}

			Assert.state(outputBufferedWriter != null);
			if (restarted) {
				checkFileSize();
				truncate();
			}

			initialized = true;
			linesWritten = 0;
		}

		/**
		 * initialized 상태를 알려준다.
		 */
		public boolean isInitialized() {
			return initialized;
		}

		/**
		 * BufferedWriter를 가져 온다.
		 */
		/*private Writer getBufferedWriter(FileChannel fileChannel, String encoding) {
			try {
				Writer writer = Channels.newWriter(fileChannel, encoding);
				if (transactional) {
					return new TransactionAwareBufferedWriter(writer, new Runnable() {
						public void run() {
							closeStream();
						}
					});
				} else {
					return new BufferedWriter(writer);
				}
			} catch (UnsupportedCharsetException ucse) {
				throw new ItemStreamException("Bad encoding configuration for output file " + fileChannel, ucse);
			}
		}*/
		private Writer getBufferedWriter(FileChannel fileChannel, String encoding) {
			try {
				final FileChannel channel = fileChannel;
				if (transactional) {
					TransactionAwareBufferedWriter writer = new TransactionAwareBufferedWriter(channel, new Runnable() {
						public void run() {
							closeStream();
						}
					});

					writer.setEncoding(encoding);
					writer.setForceSync(forceSync);
					return writer;
				}
				else {
					Writer writer = new BufferedWriter(Channels.newWriter(fileChannel, encoding)) {
						@Override
						public void flush() throws IOException {
							super.flush();
							if (forceSync) {
								channel.force(false);
							}
						}
					};

					return writer;
				}
			}
			catch (UnsupportedCharsetException ucse) {
				throw new ItemStreamException("Bad encoding configuration for output file " + fileChannel, ucse);
			}
		}		

		/**
		 * fileSize를 가져온다.
		 * 
		 * @throws IOException
		 */
		private void checkFileSize() throws IOException {
			long size = -1;

			outputBufferedWriter.flush();
			size = fileChannel.size();

			if (size < lastMarkedByteOffsetPosition) {
				throw new ItemStreamException("Current file size is smaller than size at last commit");
			}
		}

	}

}
