/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.excel.util;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.egovframe.rte.fdl.cmmn.exception.BaseRuntimeException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 엑셀 워크북을 열고 만들고 저장하는 유틸 — <b>xls·xlsx 자동 감지</b>와 <b>대용량 스트리밍 쓰기</b>.
 *
 * <p>기존 엑셀 로딩 API 는 xls(HSSF)가 기본이고 xlsx(XSSF)는 타입 구분용 워크북을 인자로 넘겨 가르는 구조라 호출자가
 * 형식을 미리 알아야 한다. 이 유틸은 POI {@link WorkbookFactory} 로 <b>확장자가 아닌 내용</b>으로 형식을 감지해
 * 하나의 API 로 연다.</p>
 *
 * <ul>
 *   <li>{@link #open(InputStream)}/{@link #open(File)} — xls·xlsx 자동 감지 로딩</li>
 *   <li>{@link #createStreaming()}/{@link #createStreaming(int)} — SXSSF 스트리밍 워크북(행 윈도만 메모리에 유지)</li>
 *   <li>{@link #write(Workbook, File)} — 부모 디렉터리 생성, 저장, SXSSF 임시파일 정리까지 한 번에</li>
 * </ul>
 *
 * <p>실패는 {@link BaseRuntimeException}(unchecked, 원인 보존)으로 알린다. 돌려받은 {@link Workbook} 을 닫는 책임은
 * 호출자에게 있다(try-with-resources 권장).</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public final class EgovWorkbooks {

    /** SXSSF 기본 메모리 행 윈도 크기 */
    public static final int DEFAULT_ROW_ACCESS_WINDOW = 100;

    /** POI 의 zip bomb 방어 기본 비율. 다른 코드가 완화했더라도 신뢰할 수 없는 입력을 열기 전에 복원한다. */
    private static final double POI_DEFAULT_MIN_INFLATE_RATIO = 0.01d;

    private EgovWorkbooks() {
    }

    /**
     * 스트림에서 워크북을 연다 — xls·xlsx 를 내용으로 자동 감지한다.
     * mark/reset 을 지원하지 않는 스트림은 내부에서 버퍼링한다.
     *
     * @param in 엑셀 입력 스트림(닫는 책임은 호출자)
     * @return 열린 워크북(닫는 책임은 호출자)
     * @throws IllegalArgumentException in 이 null 인 경우
     * @throws BaseRuntimeException     엑셀이 아니거나 읽을 수 없는 경우
     */
    public static Workbook open(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("input stream must not be null");
        }
        try {
            ZipSecureFile.setMinInflateRatio(POI_DEFAULT_MIN_INFLATE_RATIO);
            InputStream streamToUse = in.markSupported() ? in : new BufferedInputStream(in);
            return WorkbookFactory.create(streamToUse);
        } catch (IOException e) {
            throw new BaseRuntimeException("Failed to open workbook from stream (xls/xlsx auto-detect)", e);
        }
    }

    /**
     * 파일에서 워크북을 연다 — 확장자가 아닌 내용으로 xls·xlsx 를 감지한다.
     *
     * @param file 엑셀 파일
     * @return 열린 워크북(닫는 책임은 호출자)
     * @throws IllegalArgumentException file 이 null 인 경우
     * @throws BaseRuntimeException     파일이 없거나 엑셀이 아닌 경우
     */
    public static Workbook open(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        try (InputStream in = new FileInputStream(file)) {
            return open(in);
        } catch (IOException e) {
            throw new BaseRuntimeException("Failed to open workbook file: " + file, e);
        }
    }

    /**
     * 대용량 쓰기용 SXSSF 스트리밍 워크북을 만든다(행 윈도 {@value #DEFAULT_ROW_ACCESS_WINDOW}).
     * {@link #write(Workbook, File)} 로 저장하면 임시파일 정리까지 함께 처리된다.
     *
     * @return SXSSF 워크북
     */
    public static SXSSFWorkbook createStreaming() {
        return createStreaming(DEFAULT_ROW_ACCESS_WINDOW);
    }

    /**
     * 대용량 쓰기용 SXSSF 스트리밍 워크북을 만든다.
     *
     * @param rowAccessWindowSize 메모리에 유지할 행 수(초과분은 임시파일로 밀어낸다). -1 은 무제한
     * @return SXSSF 워크북
     */
    public static SXSSFWorkbook createStreaming(int rowAccessWindowSize) {
        return new SXSSFWorkbook(rowAccessWindowSize);
    }

    /**
     * 워크북을 파일로 저장한다. 부모 디렉터리가 없으면 만들고, SXSSF 워크북이면 저장 뒤 임시파일을 정리한다.
     * 워크북을 닫는 책임은 호출자에게 있다.
     *
     * @param workbook 저장할 워크북
     * @param target   대상 파일
     * @throws IllegalArgumentException 인자가 null 인 경우
     * @throws BaseRuntimeException     디렉터리를 만들지 못하거나 쓰기에 실패한 경우
     */
    public static void write(Workbook workbook, File target) {
        if (workbook == null || target == null) {
            throw new IllegalArgumentException("workbook and target must not be null");
        }
        File parent = target.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new BaseRuntimeException("Failed to create parent directory: " + parent);
        }
        try (OutputStream out = new FileOutputStream(target)) {
            workbook.write(out);
        } catch (IOException e) {
            throw new BaseRuntimeException("Failed to write workbook file: " + target, e);
        } finally {
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        }
    }

}
