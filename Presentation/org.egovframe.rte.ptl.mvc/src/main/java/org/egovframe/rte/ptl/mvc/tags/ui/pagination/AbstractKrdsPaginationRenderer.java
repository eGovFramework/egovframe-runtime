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
package org.egovframe.rte.ptl.mvc.tags.ui.pagination;

import java.text.MessageFormat;

/**
 * AbstractKrdsPaginationRenderer.java
 * <p/><b>NOTE:</b><pre> 인터페이스 PaginationRenderer의 구현 추상클래스.
 * 기본적인 페이징 기능이 구현되어 있으며, 화면에서 아래와 같이 display 된다.
 *
 * 0. 이전 페이지 링크 추가 (항상 표시, 1페이지일 때는 비활성화)
 * 1. 페이지가 1개인 경우: <이전 1 다음>
 * 2. 페이지가 8개 이하인 경우: <이전 1 2 3 4 5 6 7 8 다음>
 * 3. 페이지가 8개 초과인 경우
 * 3-1. 현재 페이지가 처음 4페이지 이내인 경우: <이전 1 2 3 4 5 6 7 ... 9 다음>
 * 3-2. 현재 페이지가 마지막 4페이지 이내인 경우: <이전 1 ... 3 4 5 6 7 8 9 다음>
 * 3-3. 현재 페이지가 중간에 있는 경우: <이전 1 ... 3 4 5 6 7 ... 9 다음>
 * 4. 다음 페이지 링크 추가 (항상 표시, 마지막 페이지일 때는 비활성화)
 *
 * 클래스 변수값을 AbstractKrdsPaginationRenderer를 상속받은 하위 클래스에서 주게 되면,
 * 페이징 포맷만 프로젝트 UI에 맞춰 커스터마이징 할 수 있다.
 * 자세한 사항은 개발자 메뉴얼의 개발프레임워크 실행환경/화면처리/MVC/View/Pagination Tag를 참고하라.
 * </pre>
 *
 * @author 실행환경 개발팀 함철
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2025.06.01   유지보수            최초 생성
 * </pre>
 * @since 2025.06.01
 */
public class AbstractKrdsPaginationRenderer implements PaginationRenderer {

    protected String firstPageLabel;
    protected String previousPageLabel;
    protected String previousPageDisabledLabel;
    protected String currentPageLabel;
    protected String otherPageLabel;
    protected String nextPageLabel;
    protected String nextPageDisabledLabel;
    protected String lastPageLabel;
    protected String dotPageLabel;

    public String renderPagination(PaginationInfo paginationInfo, String jsFunction) {

        StringBuilder stringBuilder = new StringBuilder();

        int firstPageNo = paginationInfo.getFirstPageNo();          // 첫 페이지 번호
        int totalPageCount = paginationInfo.getTotalPageCount();    // 전체 페이지 수
        int currentPageNo = paginationInfo.getCurrentPageNo();      // 현재 페이지 번호

        // 0. 이전 페이지 링크 추가 (항상 표시, 1페이지일 때는 비활성화)
        if (currentPageNo == firstPageNo) {
            stringBuilder.append(previousPageDisabledLabel);
        } else {
            stringBuilder.append(MessageFormat.format(previousPageLabel, jsFunction, Integer.toString(currentPageNo - 1)));
        }

        stringBuilder.append("<div class=\"page-links\">");

        // 1. 페이지가 1개인 경우: <이전 1 다음>
        if (totalPageCount == 1) {
            stringBuilder.append(MessageFormat.format(currentPageLabel, Integer.toString(1)));
        }

        // 2. 페이지가 8개 이하인 경우: <이전 1 2 3 4 5 6 7 8 다음>
        else if (totalPageCount <= 8) {
            for (int i = 1; i <= totalPageCount; i++) {
                if (i == currentPageNo) {
                    stringBuilder.append(MessageFormat.format(currentPageLabel, Integer.toString(i)));
                } else {
                    stringBuilder.append(MessageFormat.format(otherPageLabel, jsFunction, Integer.toString(i), Integer.toString(i)));
                }
            }
        }
        // 3. 페이지가 8개 초과인 경우
        else {
            // 3-1. 현재 페이지가 처음 4페이지 이내인 경우: <이전 1 2 3 4 5 6 7 ... 9 다음>
            if (currentPageNo <= 4) {
                for (int i = 1; i <= 7; i++) {
                    if (i == currentPageNo) {
                        stringBuilder.append(MessageFormat.format(currentPageLabel, Integer.toString(i)));
                    } else {
                        stringBuilder.append(MessageFormat.format(otherPageLabel, jsFunction, Integer.toString(i), Integer.toString(i)));
                    }
                }
                stringBuilder.append(dotPageLabel);
                stringBuilder.append(MessageFormat.format(lastPageLabel, jsFunction, Integer.toString(totalPageCount), Integer.toString(totalPageCount)));
            }
            // 3-2. 현재 페이지가 마지막 4페이지 이내인 경우: <이전 1 ... 3 4 5 6 7 8 9 다음>
            else if (currentPageNo >= totalPageCount - 3) {
                stringBuilder.append(MessageFormat.format(firstPageLabel, jsFunction, Integer.toString(firstPageNo), Integer.toString(firstPageNo)));
                stringBuilder.append(dotPageLabel);
                for (int i = totalPageCount - 6; i <= totalPageCount; i++) {
                    if (i == currentPageNo) {
                        stringBuilder.append(MessageFormat.format(currentPageLabel, Integer.toString(i)));
                    } else {
                        stringBuilder.append(MessageFormat.format(otherPageLabel, jsFunction, Integer.toString(i), Integer.toString(i)));
                    }
                }
            }
            // 3-3. 현재 페이지가 중간에 있는 경우: <이전 1 ... 3 4 5 6 7 ... 9 다음>
            else {
                stringBuilder.append(MessageFormat.format(firstPageLabel, jsFunction, Integer.toString(firstPageNo), Integer.toString(firstPageNo)));
                stringBuilder.append(dotPageLabel);
                for (int i = currentPageNo - 2; i <= currentPageNo + 2; i++) {
                    if (i == currentPageNo) {
                        stringBuilder.append(MessageFormat.format(currentPageLabel, Integer.toString(i)));
                    } else {
                        stringBuilder.append(MessageFormat.format(otherPageLabel, jsFunction, Integer.toString(i), Integer.toString(i)));
                    }
                }
                stringBuilder.append(dotPageLabel);
                stringBuilder.append(MessageFormat.format(lastPageLabel, jsFunction, Integer.toString(totalPageCount), Integer.toString(totalPageCount)));
            }
        }

        stringBuilder.append("</div>");

        // 4. 다음 페이지 링크 추가 (항상 표시, 마지막 페이지일 때는 비활성화)
        if (currentPageNo == totalPageCount) {
            stringBuilder.append(nextPageDisabledLabel);
        } else {
            stringBuilder.append(MessageFormat.format(nextPageLabel, jsFunction, Integer.toString(currentPageNo + 1)));
        }

        return stringBuilder.toString();
    }

}
