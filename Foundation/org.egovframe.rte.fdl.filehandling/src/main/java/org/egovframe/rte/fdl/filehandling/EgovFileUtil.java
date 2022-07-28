/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package org.egovframe.rte.fdl.filehandling;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.egovframe.rte.fdl.string.EgovStringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 파일 서비스을 제공하는 유틸 클래스.
 * 
 * <p><b>NOTE:</b> 파일 서비스를 제공하기 위해 구현한 클래스이다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종				최초 생성
 * 2014.05.14	이기하				vfs -> vfs2로 패키지 변경
 * 2017.02.28	장동한				시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
 * 2017.02.28	장동한				시큐어코딩(ES)-부적절한 자원 해제[CWE-404]
 * </pre>
 */
public class EgovFileUtil {

	/**
	 * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EgovFileUtil.class);
	
	private static final int BUFFER_SIZE = 1024;
	private static FileObject basefile;
	private static FileSystemManager manager;

	static {
		try {
			manager = VFS.getManager();
			basefile = manager.resolveFile(System.getProperty("user.dir"));
			LOGGER.debug("basefile : {}", basefile);
		} catch (FileSystemException e) {
			LOGGER.error("EgovFileUtil : {}", e.getMessage());
		}
	}

	/**
	 * 지정한 위치의 파일 및 디렉토리를 삭제한다.
	 * @param cmd <code>String</code>
	 * @return 결과 값
	 * @throws FileSystemException
	 */
	public static int rm(final String cmd) throws FileSystemException {
		int result = -1;
		try {
			final FileObject file = manager.resolveFile(basefile, cmd);
			result = file.delete(Selectors.SELECT_SELF_AND_CHILDREN);
			LOGGER.debug("result is {}", result);
		} catch (FileSystemException e) {
			LOGGER.error(e.toString());
			throw new FileSystemException(e);
		}
		return result;
	}

	/**
	 * 지정한 위치의 파일을 대상 위치로 복사한다.
	 * @param source <code>String</code>
	 * @param target <code>String</code>
	 * @throws Exception
	 */
	public static void cp(String source, String target) throws Exception {
		try {
			final FileObject src = manager.resolveFile(basefile, source);
			FileObject dest = manager.resolveFile(basefile, target);
			if (dest.exists() && dest.getType() == FileType.FOLDER) {
				dest = dest.resolveFile(src.getName().getBaseName());
			}
			dest.copyFrom(src, Selectors.SELECT_ALL);
		} catch (FileSystemException fse) {
			LOGGER.error(fse.toString());
			throw new FileSystemException(fse);
		}
	}
	/**
	 * 지정한 위치의 파일을 대상 위치로 이동한다.
	 * @param source <code>String</code>
	 * @param target <code>String</code>
	 * @throws Exception
	 */
	public static void mv(String source, String target) throws Exception {
		try {
			final FileObject src = manager.resolveFile(basefile, source);
			FileObject dest = manager.resolveFile(basefile, target);
			if (dest.exists() && dest.getType() == FileType.FOLDER) {
				dest = dest.resolveFile(src.getName().getBaseName());
			}
			src.moveTo(dest);
		} catch (FileSystemException fse) {
			LOGGER.error(fse.toString());
			throw new FileSystemException(fse);
		}
	}

	/**
	 * 현재 작업위치를 리턴한다.
	 * @return 현재 위치
	 */
	public static FileName pwd() {
		return basefile.getName();
	}

	/**
	 * 파일의 일시를 현재 일시로 변경한다.
	 * @param filepath <code>String</code>
	 * @return
	 * @throws Exception
	 */
	public static long touch(final String filepath) throws Exception {
		long currentTime = 0;
		final FileObject file = manager.resolveFile(basefile, filepath);
		if (!file.exists()) {
			file.createFile();
		}

		currentTime = System.currentTimeMillis();
		file.getContent().setLastModifiedTime(currentTime);
		return currentTime;
	}

	/**
	 * 현재 작업공간의 위치를 지정한 위치로 이동한다.
	 * @param changDirectory <code>String</code>
	 * @throws Exception
	 */
	public static void cd(final String changDirectory) throws Exception {
		final String path;
		if (!EgovStringUtil.isNull(changDirectory)) {
			path = changDirectory;
		} else {
			path = System.getProperty("user.home");
		}

		FileObject tmp = manager.resolveFile(basefile, path);
		if (tmp.exists()) {
			basefile = tmp;
		} else {
			LOGGER.info("Folder does not exist: {}", tmp.getName());
		}
		LOGGER.info("Current folder is {}", basefile.getName());
	}

	/**
	 * 지정한 위치의 파일목록을 조회한다.
	 * @param cmd <code>String[]</code>
	 * @return 조회된 파일 목록
	 * @throws FileSystemException
	 */
	public List<?> ls(final String[] cmd) throws FileSystemException {
		List<Object> list = new ArrayList<Object>();
		int pos = 1;
		final boolean recursive;
		if (cmd.length > pos && cmd[pos].equals("-R")) {
			recursive = true;
			pos++;
		} else {
			recursive = false;
		}

		final FileObject file;
		if (cmd.length > pos) {
			file = manager.resolveFile(basefile, cmd[pos]);
		} else {
			file = basefile;
		}

		if (file.getType() == FileType.FOLDER) {
			LOGGER.info("Contents of {}", file.getName());
			LOGGER.info("info: {}", listChildren(file, recursive, ""));
		} else {
			LOGGER.info("info: {}", file.getName());
			final FileContent content = file.getContent();
			LOGGER.info("Size: {}bytes.", content.getSize());
			final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			final String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
			LOGGER.info("Last modified: {}", lastMod);
		}

		return list;
	}

	/**
	 * 지정한 위치의 하위 디렉토리 목록을 가져온다.
	 * @param dir <code>FileObject</code>
	 * @param recursive <code>boolean</code>
	 * @param prefix <code>String</code>
	 * @return 디렉토리 목록
	 * @throws FileSystemException
	 */
	private StringBuffer listChildren(final FileObject dir, final boolean recursive, final String prefix) throws FileSystemException {
		StringBuffer line = new StringBuffer();
		final FileObject[] children = dir.getChildren();
		for (int i = 0; i < children.length; i++) {
			final FileObject child = children[i];
			line.append(prefix).append(child.getName().getBaseName());
			if (child.getType() == FileType.FOLDER) {
				line.append("/");
				if (recursive) {
					line.append(listChildren(child, recursive, prefix + "    "));
				}
			} else {
				line.append("");
			}
		}

		return line;
	}

	/**
	 * 파일을 읽는다.
	 * @param file <code>File</code>
	 * @return 결과 값
	 * @throws IOException
	 */
	public static String readFile(File file) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		String sResult = "";
		try {
			sResult = readFileContent(in);
		} catch(IllegalArgumentException e) {
			LOGGER.error("[IllegalArgumentException] Try/Catch...readFile Runing : "+ e.getMessage());
		} finally {
			in.close();
		}

		return sResult;
	}

	/**
	 * String 형으로 파일의 내용을 읽는다.
	 * @param in file input stream <code>InputStrea</code>
	 * @return 파일 내용
	 * @throws IOException
	 */
	public static String readFileContent(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		for (int i = in.read(); i != -1; i = in.read()) {
			buf.append((char) i);
		}
		return buf.toString();
	}

	/**
	 * String 영으로 파일의 내용을 읽는다.
	 * @param file <code>File</code>
	 * @param encoding <code>String</code>
	 * @return 파일 내용
	 * @throws IOException
	 */
	public static String readFile(File file, String encoding) throws IOException {
		StringBuffer sb = new StringBuffer();
		List<String> lines = FileUtils.readLines(file, encoding);
		for (Iterator<String> it = lines.iterator();;) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append("");
			} else {
				break;
			}
		}

		return sb.toString();
	}

	/**
	 * 텍스트 내용을 파일로 쓴다.
	 * @param file <code>File</code>
	 * @param text <code>String</code>
	 */
	public static void writeFile(File file, String text) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(text);
		} catch (IOException e) {
			LOGGER.error("Error creating File: {} : {}", file.getName(), e);
		return;
		} finally {
			EgovResourceReleaser.close(writer);
		}
	}

	/**
	 * 텍스트 내용을 파일로 쓴다.
	 * @param fileName <code>String</code>
	 * @param text <code>String</code>
	 */
	public static void writeFile(String fileName, String text) {
		writeFile(new File(fileName), text);
	}

	public static void writeFile(String fileName, String data, String encoding) throws IOException {
		FileUtils.writeStringToFile(new File(fileName), data, encoding);
	}

	/**
	 * byte형으로 파일의 내용을 읽어온다.
	 * @param file <code>FileObject</code>
	 * @return 파일 내용
	 * @throws IOException
	 */
	public static byte[] getContent(final FileObject file) throws IOException {
		final FileContent content = file.getContent();
		final int size = (int) content.getSize();
		final byte[] buf = new byte[size];
		final InputStream in = content.getInputStream();
		try {
			int read = 0;
			for (int pos = 0; pos < size && read >= 0; pos += read) {
				read = in.read(buf, pos, size - pos);
			}
		} finally {
			EgovResourceReleaser.close(in);
		}

		return buf;
	}

	/**
	 * 내용을 파일에 OutputStream 으로 저장한다.
	 * @param file
	 * @param outstr
	 * @throws IOException
	 */
	public static void writeContent(final FileObject file, final OutputStream outstr) throws IOException {
		final InputStream instr = file.getContent().getInputStream();
		try {
			final byte[] buffer = new byte[BUFFER_SIZE];
			while (true) {
				final int nread = instr.read(buffer);
				if (nread < 0) {
					break;
				}
				outstr.write(buffer, 0, nread);
			}
		} finally {
			EgovResourceReleaser.close(instr);
		}
	}

	/**
	 * 파일 객체를 대상 파일객체로 복사한다.
	 * @param srcFile <code>FileObject</code>
	 * @param destFile <code>FileObject</code>
	 * @throws IOException
	 */
	public static void copyContent(final FileObject srcFile, final FileObject destFile) throws IOException {
		final OutputStream outstr = destFile.getContent().getOutputStream();
		try {
			writeContent(srcFile, outstr);
		} finally {
			EgovResourceReleaser.close(outstr);
		}
	}

	/**
	 * 파일의 확장자를 가져온다.
	 * @param filename <code>String</code>
	 * @return 파일확장자
	 */
	public static String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	/**
	 * 파일의 존재여부를 확인한다.
	 * @param filename <code>String</code>
	 * @return 존재여부
	 */
	public static boolean isExistsFile(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	/**
	 * 디렉토리명을 제외한 파일명을 가져온다.
	 * @param filename <code>String</code>
	 * @return
	 */
	public static String stripFilename(String filename) {
		return FilenameUtils.getBaseName(filename);
	}

	/**
	 * 저정한 파일을 삭제한다.
	 * @param file <code>File</code>
	 * @throws IOException
	 */
	public static void delete(File file) throws IOException {
		//2017.02.28 장동한 시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
		if (file != null) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				for (int i = 0; i < children.length; i++) {
					delete(children[i]);
				}
			}

			if (!file.delete()) {
				throw new IOException("Unable to delete " + file.getPath());
			}
		}
	}

	/**
	 * 텍스트 파일을 읽어온다.
	 * @param fileName <code>String</code>
	 * @param newline <code>boolean</code>
	 * @return 파일 내용
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static StringBuffer readTextFile(String fileName, boolean newline) throws FileNotFoundException, IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		StringBuffer buf = new StringBuffer();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				buf.append(str);
				if (newline) {
					buf.append(System.getProperty("line.separator"));
				}
			}
		} finally {
			EgovResourceReleaser.close(in);
		}

		return buf;
	}

	/**
	 * 특정위치의 파일 객체를 가져온다.
	 * @param filepath <code>String</code>
	 * @return 파일 객체
	 * @throws Exception
	 */
	public static FileObject getFileObject(final String filepath) throws Exception {
		FileSystemManager mgr = VFS.getManager();
		return mgr.resolveFile(mgr.resolveFile(System.getProperty("user.dir")), filepath);
	}

	/**
	 * 특정 패턴이 존재하는 파일을 검색한다.
	 * @param search <code>Object[]</code>
	 * @param pattern <code>String</code>
	 * @return 파일 목록
	 * @throws Exception
	 */
	public static List<String> grep(final Object[] search, final String pattern) throws Exception {
		Pattern searchPattern = Pattern.compile(pattern);
		String[] strings = searchPattern.split(Arrays.toString(search));
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < strings.length; i++) {
			list.add(strings[i]);
		}
		return list;
	}

	/**
	 * 특정 패턴이 존재하는 파일을 검색한다.
	 * @param file <code>File</code>
	 * @param pattern <code>String</code>
	 * @return 파일 목록
	 * @throws Exception
	 */
	public static List<String> grep(final File file, final String pattern) throws Exception {
		Pattern searchPattern = Pattern.compile(pattern);
		List<String> lists = FileUtils.readLines(file);
		Object[] search = lists.toArray();
		String[] strings = searchPattern.split(Arrays.toString(search));
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < strings.length; i++) {
			list.add(strings[i]);
		}
		return list;
	}
	
	/**
	 * 시스템 임시 디렉토리를 얻어온다.
	 * @return
	 * @throws IOException
	 */
	public static String getTmpDirectory() throws IOException {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		return baseDir.getCanonicalPath();
	}

}
