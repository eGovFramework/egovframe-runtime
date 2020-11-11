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

package egovframework.rte.bat.core.item.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Byte단위로 아이템을 읽어들임 
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 *      <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 10.20  배치실행개발팀    최초 생성
 * 
 * </pre>
 */
public class EgovByteReader extends InputStreamReader {

	InputStream ein;
	
	public EgovByteReader(InputStream in, String charset) throws IOException {
		super(in, charset);
		ein = in;
	}
	

    /**
     * 아이템을 byte[]형태로 읽어들임(offset과 length옵션 추가)
     *
     * @param      cbuf     Destination buffer
     * @param      offset   Offset at which to start storing characters
     * @param      length   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the 
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read(byte[] cbuf, int offset, int length) throws IOException {
    	return ein.read(cbuf, offset, length);
    }
    
    /**
     * 아이템을 byte[]형태로 읽어들임
     * 
     * @param cbuf
     * @return
     * @throws IOException
     */
    public int read(byte[] cbuf) throws IOException {
    	return ein.read(cbuf);
    }

}
