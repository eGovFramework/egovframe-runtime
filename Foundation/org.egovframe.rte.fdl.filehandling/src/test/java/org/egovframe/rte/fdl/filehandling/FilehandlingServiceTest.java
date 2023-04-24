/*
 * Copyright 2002-2008 MOPAS(Ministry of Public Administration and Security).
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

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * FileServiceTest is TestCase of File Handling Service
 * @author Seongjong Yoon
 */
public class FilehandlingServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilehandlingServiceTest.class);
	private String filename = "";
	private String text = "";
	private String tmppath = "";
	private String absoluteFilePath = "";

    @Before
    public void onSetUp() throws Exception {
    	filename = "test.txt";
    	text = "test입니다.";
    	tmppath = "tmp";

    	LOGGER.debug("System's temporary directory : {}", EgovFileUtil.getTmpDirectory());

    	absoluteFilePath = EgovFileUtil.getTmpDirectory() + "/testing.txt";
    	EgovFileUtil.cd(System.getProperty("user.dir"));
    }

    /**
     * 특정 위치에 파일을 생성하고 필요에 따라 생성한 파일을 캐싱한다.
     */
    @Test
    public void testCeateFile() throws Exception {
    	FileSystemManager manager = VFS.getManager();
    	FileObject baseDir = manager.resolveFile(System.getProperty("user.dir"));
    	final FileObject file = manager.resolveFile(baseDir, "testfolder/file1.txt");

    	// 모든 파일 삭제
    	file.delete(Selectors.SELECT_FILES);
    	assertFalse(file.exists());

    	// 파일 생성
    	file.createFile();
    	assertTrue(file.exists());
    }

    /**
     * 특정 위치에 존재하는 파일에 접근하여 파일 내용을 수정한다.
     * 파일 위치는 절대 경로 또는 상대 경로 등 다양한 형식을 지원한다.
     */
    @Test
    public void testAccessFile() throws IOException {
    	FileSystemManager manager = VFS.getManager();
    	FileObject baseDir = manager.resolveFile(System.getProperty("user.dir"));
    	FileObject file = manager.resolveFile(baseDir, "testfolder/file1.txt");

    	// 모든 파일 삭제
    	file.delete(Selectors.SELECT_FILES);
    	assertFalse(file.exists());

    	// 파일 생성
    	file.createFile();
    	assertTrue(file.exists());

    	FileContent fileContent = file.getContent();
    	assertEquals(0, fileContent.getSize());

    	// 파일 쓰기
    	String string = "test입니다.";
    	OutputStream os = fileContent.getOutputStream();
    	try {
	    	os.write(string.getBytes());
	    	os.flush();
    	} finally {
			EgovResourceReleaser.close(os);
    	}
    	assertNotSame(0, fileContent.getSize());

    	// 파일 읽기
    	StringBuilder sb = new StringBuilder();
    	FileObject writtenFile = manager.resolveFile(baseDir, "testfolder/file1.txt");
    	FileContent writtenContents = writtenFile.getContent();
    	InputStream is = writtenContents.getInputStream();
    	try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    	String line;
	    	while ((line = reader.readLine()) != null) {
	    		sb.append(line);
	    	}
    	} finally {
			EgovResourceReleaser.close(is);
    	}
    	assertEquals(sb.toString(), string);
    }

    /**
     * 캐싱 기능을 사용하여, 생성하거나 수정할 파일을 메모리 상에 로딩함으로써
     * 파일 접근 시에 소요되는 시간을 단축한다.
     */
    @Test
    public void testCaching() throws IOException {
    	String path = FilehandlingServiceTest.class.getResource("").getPath();
    	String testFolder = path + "/testfolder";
    	FileSystemManager manager = VFS.getManager();
    	FileObject scratchFolder = manager.resolveFile(testFolder);

    	// testfolder 내의 모든 파일 삭제
        scratchFolder.delete(Selectors.EXCLUDE_SELF);

        FileObject file = manager.resolveFile(path +  "/testfolder/dummy.txt");
        file.createFile();

        // 캐싱 Manager 생성
        DefaultFileSystemManager fs = new DefaultFileSystemManager();
	    fs.setFilesCache(manager.getFilesCache());
	    if (!fs.hasProvider("file")) {
	        fs.addProvider("file", new DefaultLocalFileProvider());
	    }
	    fs.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fs.init();

        // 캐싱 객체 생성
        FileObject foBase2 = fs.resolveFile(testFolder);
        LOGGER.debug("## scratchFolder.getName().getPath() : {}", scratchFolder.getName().getPath());
        FileObject cachedFolder = foBase2.resolveFile(scratchFolder.getName().getPath());

        // 파일이 존재하지 않음
        FileObject[] fos = cachedFolder.getChildren();
        assertFalse(contains(fos, "file1.txt"));

        // 파일생성
        scratchFolder.resolveFile("file1.txt").createFile();

        // 파일 존재, cachedFolder 에는 파일이 존재하지 않음
        fos = cachedFolder.getChildren();
        assertFalse(contains(fos, "file1.txt"));

        // 새로고침
        cachedFolder.refresh();

        // 파일이 존재함
        fos = cachedFolder.getChildren();
        assertTrue(contains(fos, "file1.txt"));
    }

    /**
     * 파일 생성 테스트.
     */
    @Test
    public void testWriteFile() throws IOException {
    	if (EgovFileUtil.isExistsFile(filename)) {
    		EgovFileUtil.delete(new File(filename));
    	}

    	EgovFileUtil.writeFile(filename, text, "UTF-8");
    	assertTrue(EgovFileUtil.isExistsFile(filename));
    }
    
    /**
     * 파일 생성 테스트.
     */
    @Test
    public void testWriteFileWithAbsolutePath() throws IOException {
    	if (EgovFileUtil.isExistsFile(absoluteFilePath)) {
    		EgovFileUtil.delete(new File(absoluteFilePath));
    	}

    	EgovFileUtil.writeFile(absoluteFilePath, text, "UTF-8");
    	assertTrue(EgovFileUtil.isExistsFile(absoluteFilePath));
    }

	/**
	 * 파일 읽기 테스트.
	 */
	@Test
    public void testReadFile() throws IOException {
    	if (!EgovFileUtil.isExistsFile(filename)) {
    		EgovFileUtil.writeFile(filename, text, "UTF-8");
    	}
    	assertEquals(EgovFileUtil.readFile(new File(filename), "UTF-8"), text);

    	List<String> lines = FileUtils.readLines(new File(filename), "UTF-8");
    	String string = lines.get(0);
    	assertEquals(text, string);
    }
	
	/**
	 * 파일 읽기 테스트.
	 */
	@Test
    public void testReadFileWithAbsolutePath() throws IOException {
    	if (!EgovFileUtil.isExistsFile(absoluteFilePath)) {
    		EgovFileUtil.writeFile(absoluteFilePath, text, "UTF-8");
    	}
    	assertEquals(EgovFileUtil.readFile(new File(absoluteFilePath), "UTF-8"), text);

    	List<String> lines = FileUtils.readLines(new File(absoluteFilePath), "UTF-8");
    	String string = lines.get(0);
    	assertEquals(text, string);
    }

    /**
     * 파일 복사 테스트.
     */
    @Test
    public void testCp() throws IOException {
    	if (!EgovFileUtil.isExistsFile(filename)) {
    		EgovFileUtil.writeFile(filename, text);
    	}

    	EgovFileUtil.cp(filename, tmppath + "/" + filename);

    	assertEquals(
    			EgovFileUtil.readFile(new File(filename), "UTF-8"),
    			EgovFileUtil.readFile(new File(tmppath + "/" + filename), "UTF-8")
    	);
    }

    /**
     * 파일 복사 테스트.
     */
    @Test
    public void testCpWithAbsolutePath() throws IOException {
    	if (!EgovFileUtil.isExistsFile(absoluteFilePath)) {
    		EgovFileUtil.writeFile(absoluteFilePath, text);
    	}

    	EgovFileUtil.cp(absoluteFilePath, tmppath + "/" + filename);

    	assertEquals(
    			EgovFileUtil.readFile(new File(absoluteFilePath), "UTF-8"),
    			EgovFileUtil.readFile(new File(tmppath + "/" + filename), "UTF-8")
    	);
    }

    /**
     * 파일 이동 테스트.
     */
    @Test
    public void testMv() throws IOException {
    	if (!EgovFileUtil.isExistsFile(tmppath + "/" + filename)) {
    		EgovFileUtil.writeFile(tmppath + "/" + filename, text);
    	}

    	EgovFileUtil.mv(tmppath + "/" + filename, tmppath + "/movedfile.txt");

    	assertEquals(
    			EgovFileUtil.readFile(new File(filename), "UTF-8"),
    			EgovFileUtil.readFile(new File(tmppath + "/movedfile.txt"), "UTF-8")
    	);
    }

    /**
     * 파일 터치 테스트.
     */
    @Test
    public void testTouch() throws IOException {
		String path = tmppath + "/movedfile.txt";
		FileObject file = EgovFileUtil.getFileObject(path);
		long lastModifyTime = file.getContent().getLastModifiedTime();

		long setTime = EgovFileUtil.touch(path);

		assertNotEquals(lastModifyTime, setTime);
    }

    /**
     * 파일 확장자 처리 테스트.
     */
    @Test
    public void testGetFileExtension() {
    	assertTrue(EgovFileUtil.isExistsFile(filename));
    	assertEquals(EgovFileUtil.getFileExtension(filename), "txt");
    }

    /**
     * 파일 존재 유무 테스트.
     */
    @Test
    public void testIsExistsFile() {
       	assertTrue(EgovFileUtil.isExistsFile(filename));
    }

    /**
     * 파일명 확인 테스트.
     */
    @Test
    public void testStripFilename() {
    	assertTrue(EgovFileUtil.isExistsFile(filename));
    	assertEquals("test", EgovFileUtil.stripFilename(filename));
    }

    /**
     * 파일 삭제 테스트.
     */
    @Test
    public void testRm() throws IOException {
    	String tmptarget = tmppath;
    	if (!EgovFileUtil.isExistsFile(tmptarget)) {
    		EgovFileUtil.writeFile(tmptarget, text);
    	}

    	int result = EgovFileUtil.rm(tmptarget);

    	assertTrue(result > 0);
    	assertFalse(EgovFileUtil.isExistsFile(tmptarget));
    }

    /**
     * 디렉토리 변경 테스트.
     */
    @Test
    public void testCd() throws IOException {
    	String path = "/Users/EGOV/";
    	FileName foldername = EgovFileUtil.getFileObject(path).getName();
    	EgovFileUtil.cd("");

    	String uri = EgovFileUtil.pwd().getURI();
    	assertFalse(foldername.getURI().equals(uri));

    	EgovFileUtil.cd(path);
    	uri = EgovFileUtil.pwd().getURI();
    	LOGGER.debug("EgovFileUtil.pwd().getURI() : {}", uri);
    	LOGGER.debug("foldername.getURI() : {}", foldername.getURI());
    	assertEquals(foldername.getURI(), EgovFileUtil.pwd().getURI());
    }

    /**
     * Stream 테스트.
     */
    @Test
    public void testIOUtils() throws IOException {
		InputStream in = new URL("https://commons.apache.org/").openStream();
		try {
			assertNotEquals("", IOUtils.toString(in, StandardCharsets.UTF_8));
		} finally {
			IOUtils.closeQuietly(in);
		}
    }

    /**
     * FileSystemUtils 테스트.
     */
	@Test
    public void testFileSystemUtils() throws IOException {
   		long freeSpace = FileSystemUtils.freeSpaceKb("C:/");
   		assertTrue(freeSpace > 0);
    }

    /**
     * GREP 테스트.
     */
    @Test
    public void testGrep() throws IOException {
		String[] search = {"abcdefg", "efghijklmn", "12", "3"};
		List<String> lists = EgovFileUtil.grep(search, "\\d{1,2}");
		for (Iterator<String> it = lists.iterator(); it.hasNext();) {
			LOGGER.info(it.next());
		}

		lists = EgovFileUtil.grep(new File("pom.xml"), "org.egovframe.rte");
		for (Iterator<String> it = lists.iterator(); it.hasNext();) {
			LOGGER.info(it.next());
		}
    }

    /**
     * Line iterator 테스트.
     */
    @Test
    public void testLineIterator() throws IOException {
    	String[] string = {
			"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"",
			"         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
			"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/maven-v4_0_0.xsd\">",
			"    <modelVersion>4.0.0</modelVersion>",
			"    <groupId>org.egovframe.rte</groupId>",
			"    <artifactId>org.egovframe.rte.fdl.filehandling</artifactId>",
			"    <version>4.2.0</version>",
			"    <packaging>jar</packaging>",
			"    <name>org.egovframe.rte.fdl.filehandling</name>"
    	};

		File file = new File("pom.xml");
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");

		try {
			for (int i = 0; i < string.length; i++) {
				String line = it.nextLine();
				assertEquals(string[i], line);
			}
		 } finally {
			LineIterator.closeQuietly(it);
		 }
    }

    @Test
    public void testUserAuthentication() throws IOException {
    	StaticUserAuthenticator auth = new StaticUserAuthenticator(null, "username", "password");
    	FileSystemOptions opts = new FileSystemOptions();
    	DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

    	FileSystemManager manager = VFS.getManager();
    	FileObject baseDir = manager.resolveFile(System.getProperty("user.dir"));
    	FileObject file = manager.resolveFile(baseDir, "testfolder/file1.txt");
    	FileObject fo = manager.resolveFile(file.getName().getPath(), opts);
    	fo.createFile();
    }

    @Test
    public void testCaching1() throws IOException {
    	String testFolder = FilehandlingServiceTest.class.getResource(".").getPath();
    	LOGGER.debug("testFolder = {}", testFolder);
    	FileSystemManager manager = VFS.getManager();
    	EgovFileUtil.writeFile(testFolder + "/file1.txt", text, "UTF-8");

	    /*
	     * 캐싱 Manager 생성
	     * CacheStrategy.MANUAL : Deal with cached data manually. Call FileObject.refresh() to refresh the object data.
	     * CacheStrategy.ON_RESOLVE : Refresh the data every time you request a file from FileSystemManager.resolveFile
	     * CacheStrategy.ON_CALL : Refresh the data every time you call a method on the fileObject. 
	     * 	You'll use this only if you really need the latest info as this setting is a major performance loss.
	     */
        DefaultFileSystemManager fs = new DefaultFileSystemManager();
	    fs.setFilesCache(manager.getFilesCache());
	    if (!fs.hasProvider("file")) {
	        fs.addProvider("file", new DefaultLocalFileProvider());
	    }
	    fs.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fs.init();

        // 캐싱 객체 생성
        LOGGER.debug("####1");
        FileObject cachedFile = fs.toFileObject(new File(testFolder + "/file1.txt"));
        LOGGER.debug("####2");
        FilesCache filesCache = fs.getFilesCache();
        LOGGER.debug("####3");
        filesCache.putFile(cachedFile);
        FileObject obj = filesCache.getFile(cachedFile.getFileSystem(), cachedFile.getName());

        EgovFileUtil.delete(new File(testFolder + "/file1.txt"));
        LOGGER.debug("#########file is {}", obj.exists());

        fs.close();
    }

	@Test
    public void testCaching3() throws IOException {
    	FileSystemManager manager = VFS.getManager();
    	String testFolder = FilehandlingServiceTest.class.getResource(".").getPath();
    	FileObject scratchFolder = manager.resolveFile(testFolder + "/testfolder");

    	// releaseable
        FileObject dir1 = scratchFolder.resolveFile("file1.txt");

        // avoid cache removal
        FileObject dir2 = scratchFolder.resolveFile("file2.txt");
        dir2.getContent();

        // check if the cache still holds the right instance
		FileObject dir22 = scratchFolder.resolveFile("file2.txt");
		assertSame(dir2, dir22);
    }

	private boolean contains(FileObject[] fos, String string) {
		for (FileObject fo : fos) {
			if (string.equals(fo.getName().getBaseName())) {
				LOGGER.debug("# {}", string);
				return true;
			}
		}
		LOGGER.debug("# {} should be seen", string);
		return false;
	}
}
