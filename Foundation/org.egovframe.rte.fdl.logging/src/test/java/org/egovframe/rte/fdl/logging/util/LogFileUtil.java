package org.egovframe.rte.fdl.logging.util;

import java.io.*;

public class LogFileUtil {

    public static String getLastLine(File logFile) throws Exception {
        return getTailLines(logFile, 1)[0];
    }

    public static String[] getTailLines(File logFile, int paramLines) throws Exception {
        int lines = paramLines;
        String[] tailLines = new String[lines];

        // RandomAccessFile 을 사용하면 임의의 위치로 seek 하여 포인터를 이동할 수 있음, read 모드
        RandomAccessFile file = new RandomAccessFile(logFile, "r");
        long fileLength = file.length();
        boolean isDummy = true;
        // 마지막 위치로 포인터 이동
        file.seek(fileLength);

        // 파일의 마지막부터 원하는 라인수 만큼
        while (lines > 0) {
            for (int i = 1; i <= fileLength; i++) {
                // 로그 파일 마지막 라인에 dummy new line 포함
                if (file.read() == '\n') {
                    if (isDummy) {
                        isDummy = false;
                        // 역방향 scan 중 dummy 가 아닌 뉴라인 위치에서 readLine 하면 해당 라인 데이터
                    } else {
                        if (lines > 0) {
                            tailLines[--lines] = file.readLine();
                        }
                    }
                }
                // 뉴라인까지 뒤에서부터 한 바이트씩 포인터 이동 (seek 는 파일 시작 기준 offset)
                file.seek(fileLength - i);
            }
            // 로그 파일이 1행뿐이면 위쪽 new line 이 없으므로 파일 시작 위치에서 readLine
            if (lines > 0) {
                tailLines[--lines] = file.readLine();
            }
        }

        file.close();

        return tailLines;
    }

    // 파일의 라인수 리턴
    public static int countLines(BufferedReader in) throws IOException {
        int numLines = 0;
        String line;
        do {
            line = in.readLine();
            if (line != null) {
                numLines++;
            }
        } while (line != null);
        return numLines;
    }

    // 파일의 라인수 리턴
    public static int countLines(String fileName) {
        int numLines = 0;
        try (
            FileReader fileReader = new FileReader(fileName);
            BufferedReader in = new BufferedReader(fileReader)
        ) {
            numLines = countLines(in);
        } catch (IOException e) {
            e.getMessage();
        }

        return numLines;
    }

    // 단어 포함여부 확인
    public static Boolean contains(String str, BufferedReader in)
            throws IOException {
        String line;
        Boolean bool = false;
        do {
            line = in.readLine();
            if (line != null && line.contains(str)) {
                bool = true;
            }
        } while (line != null);
        return bool;
    }

    // 단어 포함여부 확인
    public static Boolean contains(String fileName, String str) {
        Boolean bool = false;
        try (
            FileReader fileReader = new FileReader(fileName);
            BufferedReader in = new BufferedReader(fileReader)
        ) {
            bool = contains(str, in);
        } catch (IOException e) {
            e.getMessage();
        }

        return bool;
    }

    // 단어수 리턴
    public static int countWords(String str, BufferedReader in)
            throws IOException {
        int numWords = 0;
        String line;
        do {
            line = in.readLine();
            if (line != null && line.contains(str)) {
                numWords++;
            }
        } while (line != null);

        return numWords;
    }

    // 단어수 리턴
    public static int countWords(String fileName, String str) {
        int numWords = 0;
        try (
            FileReader fileReader = new FileReader(fileName);
            BufferedReader in = new BufferedReader(fileReader)
        ) {
            numWords = countWords(str, in);
        } catch (IOException e) {
            e.getMessage();
        }

        return numWords;
    }
}
