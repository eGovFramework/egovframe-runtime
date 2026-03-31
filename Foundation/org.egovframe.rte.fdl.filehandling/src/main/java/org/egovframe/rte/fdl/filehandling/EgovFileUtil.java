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
package org.egovframe.rte.fdl.filehandling;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;
import org.egovframe.rte.fdl.logging.util.EgovResourceReleaser;
import org.egovframe.rte.fdl.string.EgovStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.FileNotFoundException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 파일 서비스를 제공하기 위해 구현한 클래스이다.
 * <p>
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	윤성종				최초 생성
 * 2014.05.14	이기하				vfs -> vfs2로 패키지 변경
 * 2017.02.28	장동한				시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
 * 2017.02.28	장동한				시큐어코딩(ES)-부적절한 자원 해제[CWE-404]
 */
public class EgovFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovFileUtil.class);
    private static final int BUFFER_SIZE = 1024;
    private static FileObject basefile;
    private static FileSystemManager manager;

    static {
        try {
            manager = VFS.getManager();
            basefile = manager.resolveFile(System.getProperty("user.dir"));
        } catch (FileSystemException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 지정한 위치의 파일 및 디렉토리를 삭제한다.
     */
    public static int rm(final String cmd) {
        int result = -1;
        try {
            final FileObject file = manager.resolveFile(basefile, cmd);
            result = file.delete(Selectors.SELECT_SELF_AND_CHILDREN);
        } catch (FileSystemException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        }
        return result;
    }

    /**
     * 지정한 위치의 파일을 대상 위치로 복사한다.
     */
    public static void cp(String source, String target) {
        try {
            final FileObject src = manager.resolveFile(basefile, source);
            FileObject dest = manager.resolveFile(basefile, target);
            if (dest.exists() && dest.getType() == FileType.FOLDER) {
                dest = dest.resolveFile(src.getName().getBaseName());
            }
            dest.copyFrom(src, Selectors.SELECT_ALL);
        } catch (FileSystemException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 지정한 위치의 파일을 대상 위치로 이동한다.
     */
    public static void mv(String source, String target) {
        try {
            final FileObject src = manager.resolveFile(basefile, source);
            FileObject dest = manager.resolveFile(basefile, target);
            if (dest.exists() && dest.getType() == FileType.FOLDER) {
                dest = dest.resolveFile(src.getName().getBaseName());
            }
            src.moveTo(dest);
        } catch (FileSystemException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 현재 작업위치를 리턴한다.
     */
    public static FileName pwd() {
        return basefile.getName();
    }

    /**
     * 파일의 일시를 현재 일시로 변경한다.
     */
    public static long touch(final String filepath) throws FileSystemException {
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
     */
    public static void cd(final String changDirectory) throws FileSystemException {
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
            LOGGER.debug("### EogvFileUtil cd() Folder does not exist: {}", tmp.getName());
        }
        LOGGER.debug("### EogvFileUtil cd() Current folder is {}", basefile.getName());
    }

    /**
     * 파일을 읽는다.
     */
    public static String readFile(File file) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        String sResult = "";

        try {
            sResult = readFileContent(in);
        } catch (IllegalArgumentException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        } finally {
            in.close();
        }

        return sResult;
    }

    /**
     * String 형으로 파일의 내용을 읽는다.
     */
    public static String readFileContent(InputStream in) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int i = in.read(); i != -1; i = in.read()) {
            buf.append((char) i);
        }
        return buf.toString();
    }

    /**
     * String 영으로 파일의 내용을 읽는다.
     */
    public static String readFile(File file, String encoding) throws IOException {
        StringBuffer sb = new StringBuffer();
        List<String> lines = readTextLines(file, encoding);

        for (Iterator<String> it = lines.iterator(); ; ) {
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
     * 텍스트 라인을 읽는다. UTF-8 지정 시 잘못된 바이트 시퀀스는 대체 문자로 처리한다.
     */
    private static List<String> readTextLines(File file, String encoding) throws IOException {
        if (encoding != null && ("UTF-8".equalsIgnoreCase(encoding) || "UTF8".equalsIgnoreCase(encoding))) {
            return readUtf8LinesLenient(file);
        }
        return FileUtils.readLines(file, encoding);
    }

    private static List<String> readUtf8LinesLenient(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 텍스트 내용을 파일로 쓴다.
     */
    public static void writeFile(File file, String text) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(text);
        } catch (IOException e) {
            LOGGER.debug("[{}] EogvFileUtil : {}", e.getClass().getName(), e.getMessage());
        } finally {
            EgovResourceReleaser.close(writer);
        }
    }

    /**
     * 텍스트 내용을 파일로 쓴다.
     */
    public static void writeFile(String fileName, String text) {
        writeFile(new File(fileName), text);
    }

    public static void writeFile(String fileName, String data, String encoding) throws IOException {
        FileUtils.writeStringToFile(new File(fileName), data, encoding);
    }

    /**
     * byte형으로 파일의 내용을 읽어온다.
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
     */
    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    /**
     * 파일의 존재여부를 확인한다.
     */
    public static boolean isExistsFile(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    /**
     * 디렉토리명을 제외한 파일명을 가져온다.
     */
    public static String stripFilename(String filename) {
        return FilenameUtils.getBaseName(filename);
    }

    /**
     * 저정한 파일을 삭제한다.
     */
    public static void delete(File file) throws IOException {
        //2017.02.28 장동한 시큐어코딩(ES)-Null Pointer 역참조[CWE-476]
        if (file != null) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
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
     */
    public static StringBuffer readTextFile(String fileName, boolean newline) throws IOException {
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
                    buf.append(System.lineSeparator());
                }
            }
        } finally {
            EgovResourceReleaser.close(in);
        }

        return buf;
    }

    /**
     * 특정위치의 파일 객체를 가져온다.
     */
    public static FileObject getFileObject(final String filepath) throws FileSystemException {
        FileSystemManager mgr = VFS.getManager();
        return mgr.resolveFile(mgr.resolveFile(System.getProperty("user.dir")), filepath);
    }

    /**
     * 특정 패턴이 존재하는 파일을 검색한다.
     */
    public static List<String> grep(final Object[] search, final String pattern) {
        Pattern searchPattern = Pattern.compile(pattern);
        String[] strings = searchPattern.split(Arrays.toString(search));
        return new ArrayList<>(Arrays.asList(strings));
    }

    /**
     * 특정 패턴이 존재하는 파일을 검색한다.
     */
    public static List<String> grep(final File file, final String pattern) throws IOException {
        Pattern searchPattern = Pattern.compile(pattern);
        List<String> lists = readTextLines(file, "UTF-8");
        Object[] search = lists.toArray();
        String[] strings = searchPattern.split(Arrays.toString(search));
        List<String> list = new ArrayList<>();
        Collections.addAll(list, strings);
        return list;
    }

    /**
     * 시스템 임시 디렉토리를 얻어온다.
     */
    public static String getTmpDirectory() throws IOException {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        return baseDir.getCanonicalPath();
    }

    /**
     * 지정한 위치의 파일목록을 조회한다.
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
            LOGGER.debug("### EogvFileUtil ls() Contents of {}", file.getName());
            LOGGER.debug("### EogvFileUtil ls() info : {}", listChildren(file, recursive, ""));
        } else {
            LOGGER.debug("### EogvFileUtil ls() info : {}", file.getName());
            final FileContent content = file.getContent();
            LOGGER.debug("### EogvFileUtil ls() Size : {}bytes.", content.getSize());
            final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            final String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
            LOGGER.debug("### EogvFileUtil ls() Last modified : {}", lastMod);
        }

        return list;
    }

    /**
     * 지정한 위치의 하위 디렉토리 목록을 가져온다.
     */
    private StringBuffer listChildren(final FileObject dir, final boolean recursive, final String prefix) throws FileSystemException {
        StringBuffer line = new StringBuffer();
        final FileObject[] children = dir.getChildren();

        for (final FileObject child : children) {
            line.append(prefix).append(child.getName().getBaseName());
            if (child.getType() == FileType.FOLDER) {
                line.append("/");
                if (recursive) {
                    line.append(listChildren(child, recursive, prefix + "    "));
                }
            }
        }

        return line;
    }

}
