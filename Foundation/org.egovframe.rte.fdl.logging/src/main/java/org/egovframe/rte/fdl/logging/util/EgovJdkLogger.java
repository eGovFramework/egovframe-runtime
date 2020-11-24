/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.fdl.logging.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class  to support to logging information
 *
 * @author Vincent Han
 * @since 2015.02.05
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2015.02.05	EGSF				최초 생성
 * </pre>
 */
public final class EgovJdkLogger {

	private static final Level IGNORE_INFO_LEVEL = Level.OFF;
	private static final Level DEBUG_INFO_LEVEL = Level.FINEST;
	private static final Level INFO_INFO_LEVEL = Level.INFO;

	private static final Logger IGNORE_LOGGER = Logger.getLogger("ignore");
	private static final Logger DEBUG_LOGGER = Logger.getLogger("debug");
	private static final Logger INFO_LOGGER = Logger.getLogger("info");
	
	private EgovJdkLogger() {
	}

	/**
	 * 기록이나 처리가 불필요한 경우 사용.
	 */
	public static void ignore(String message, Exception exception) {
		if (exception == null) {
			IGNORE_LOGGER.log(IGNORE_INFO_LEVEL, message);
		} else {
			IGNORE_LOGGER.log(IGNORE_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 기록이나 처리가 불필요한 경우 사용.
	 */
	public static void ignore(String message) {
		ignore(message, null);
	}

	/**
	 * 디버그 정보를 기록하는 경우 사용.
	 */
	public static void debug(String message, Exception exception) {
		if (exception == null) {
			DEBUG_LOGGER.log(DEBUG_INFO_LEVEL, message);
		} else {
			DEBUG_LOGGER.log(DEBUG_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 디버그 정보를 기록하는 경우 사용.
	 */
	public static void debug(String message) {
		debug(message, null);
	}

	/**
	 * 일반적이 정보를 기록하는 경우 사용.
	 */
	public static void info(String message) {
		INFO_LOGGER.log(INFO_INFO_LEVEL, message);
	}

}
