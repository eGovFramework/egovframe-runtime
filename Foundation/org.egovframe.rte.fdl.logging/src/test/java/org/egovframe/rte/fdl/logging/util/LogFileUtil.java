package org.egovframe.rte.fdl.logging.util;

import java.io.*;

public class LogFileUtil {

	public static String getLastLine(File logFile) throws Exception {
		return getTailLines(logFile, 1)[0];
	}

	public static String[] getTailLines(File logFile, int paramLines)
			throws Exception {
		int lines = paramLines; // PMD 바보
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
				// 로그 파일의 마지막 라인에 dummy new line 이 들어있음
				if (file.read() == '\n') {
					if (isDummy) {
						isDummy = false;
						// 거꾸로 scan 하며 dummy가 아닌 뉴라인을 만나면 그 위치에서 readLine 을 하면
						// 원하는 해당 라인 데이터 임
					} else {
						if (lines > 0) {
							tailLines[--lines] = file.readLine();
						}
					}
				}
				// 뉴라인을 만날때 까지 뒤에서 부터 한 바이트씩 늘려가며 포인터 이동. cf.) seek 는 파일시작지점부터의
				// offset 임에 유의
				file.seek(fileLength - i);
			}
			// 위에서 최초 로그파일에 1행만 존재하는 경우 dummy 라인만 있고 그 위에 new line 이 없음.
			// 이 때는 파일 포인터가 파일 시작위치에 왔을 것이므로 그냥 readLine 하면 됨.
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
		BufferedReader in = null;
		int numLines = 0;
		try {
			FileReader fileReader = new FileReader(fileName);
			in = new BufferedReader(fileReader);
			numLines = countLines(in);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
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
		BufferedReader in = null;
		Boolean bool = false;
		try {
			FileReader fileReader = new FileReader(fileName);
			in = new BufferedReader(fileReader);
			bool = contains(str, in);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
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
		BufferedReader in = null;
		int numWords = 0;
		try {
			FileReader fileReader = new FileReader(fileName);
			in = new BufferedReader(fileReader);
			numWords = countWords(str, in);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return numWords;
	}
}
