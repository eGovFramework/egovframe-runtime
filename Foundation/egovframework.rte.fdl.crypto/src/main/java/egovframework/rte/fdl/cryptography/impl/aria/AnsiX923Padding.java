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
package egovframework.rte.fdl.cryptography.impl.aria;

public class AnsiX923Padding implements CryptoPadding {
	/** 패딩 규칙 이름 */
	private String name = "ANSI-X.923-Padding";

	private static final byte PADDING_VALUE = 0x00;

	/**
	 * 요청한 Block Size를 맞추기 위해 Padding을 추가한다.
	 *
	 * @param source byte[] 패딩을 추가할 bytes
	 * @param blockSize int block size
	 * @return byte[] 패딩이 추가 된 결과 bytes
	 */
	public byte[] addPadding(byte[] source, int blockSize) {
		int paddingCnt = source.length % blockSize;
		byte[] paddingResult = null;

		if (paddingCnt != 0) {
			paddingResult = new byte[source.length + (blockSize - paddingCnt)];

			System.arraycopy(source, 0, paddingResult, 0, source.length);

			// 패딩해야 할 갯수 - 1 (마지막을 제외)까지 0x00 값을 추가한다.
			int addPaddingCnt = blockSize - paddingCnt;
			for (int i = 0; i < addPaddingCnt; i++) {
				paddingResult[source.length + i] = PADDING_VALUE;
			}

			// 마지막 패딩 값은 패딩 된 Count를 추가한다.
			paddingResult[paddingResult.length - 1] = (byte) addPaddingCnt;
		} else {
			paddingResult = source;
		}

		//print(paddingResult);

		return paddingResult;
	}

	/**
	 * 요청한 Block Size를 맞추기 위해 추가 된 Padding을 제거한다.
	 *
	 * @param source byte[] 패딩을 제거할 bytes
	 * @param blockSize int block size
	 * @return byte[] 패딩이 제거 된 결과 bytes
	 */
	public byte[] removePadding(byte[] source, int blockSize) {
		byte[] paddingResult = null;
		boolean isPadding = false;

		// 패딩 된 count를 찾는다.
		int lastValue = source[source.length - 1];
		if (lastValue < (blockSize - 1)) {
			int zeroPaddingCount = lastValue - 1;

			for (int i = 2; i < (zeroPaddingCount + 2); i++) {
				if (source[source.length - i] != PADDING_VALUE) {
					isPadding = false;
					break;
				}
			}

			isPadding = true;
		} else {
			// 마지막 값이 block size 보다 클 경우 패딩 된것이 없음.
			isPadding = false;
		}
		
		//padding length 1에 대한 보정 2019.01.02 modify by jdh
		if (lastValue != 0) {
			if((blockSize%lastValue) == 1) {
				isPadding = true;
			} else if (isPadding && lastValue <= 0) { // minus, 0 and 기존 1 skip(padding length +1에 대한 보정 )  2019.01.02 modify by jdh
				isPadding = false;
			}
		}

		if (isPadding) {
			for (int index = source.length - lastValue; index < source.length - 1; index++) {
				if (source[index] != (byte) 0) {
					isPadding = false;
					break;
				}
			}
		}

		if (isPadding) {
			paddingResult = new byte[source.length - lastValue];
			try {
				System.arraycopy(source, 0, paddingResult, 0, paddingResult.length);
			} catch (ArrayIndexOutOfBoundsException ex) {
				System.out.println("removePadding Exception.....");
				return source;
			}
		} else {
			paddingResult = source;
		}

		return paddingResult;
	}

	public String getName() {
		return name;
	}

	public void print(byte[] data) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[").append(data.length).append("] ");

		for (int i = 0; i < data.length; i++) {
			buffer.append(data[i]).append(" ");
		}

		System.out.println(buffer.toString());
	}
}
