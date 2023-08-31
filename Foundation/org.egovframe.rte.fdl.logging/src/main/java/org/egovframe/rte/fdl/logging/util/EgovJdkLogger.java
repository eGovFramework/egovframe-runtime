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

	private static final Level ALL_INFO_LEVEL = Level.ALL;
	private static final Level CONFIG_INFO_LEVEL = Level.CONFIG;
	private static final Level FINE_INFO_LEVEL = Level.FINE;
	private static final Level FINER_INFO_LEVEL = Level.FINER;
	private static final Level DEBUG_INFO_LEVEL = Level.FINEST;
	private static final Level INFO_INFO_LEVEL = Level.INFO;
	private static final Level IGNORE_INFO_LEVEL = Level.OFF;
	private static final Level SEVERE_INFO_LEVEL = Level.SEVERE;
	private static final Level WARNING_INFO_LEVEL = Level.WARNING;

	private static final Logger ALL_LOGGER = Logger.getLogger("all");
	private static final Logger CONFIG_LOGGER = Logger.getLogger("config");
	private static final Logger FINE_LOGGER = Logger.getLogger("fine");
	private static final Logger FINER_LOGGER = Logger.getLogger("finer");
	private static final Logger DEBUG_LOGGER = Logger.getLogger("debug");
	private static final Logger INFO_LOGGER = Logger.getLogger("info");
	private static final Logger IGNORE_LOGGER = Logger.getLogger("ignore");
	private static final Logger SEVERE_LOGGER = Logger.getLogger("severe");
	private static final Logger WARNING_LOGGER = Logger.getLogger("warning");
	
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
	 * 일반적인 정보를 기록하는 경우 사용.
	 */
	public static void info(String message, Exception exception) {
		if (exception == null) {
			INFO_LOGGER.log(INFO_INFO_LEVEL, message);
		} else {
			INFO_LOGGER.log(INFO_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 일반적인 정보를 기록하는 경우 사용.
	 */
	public static void info(String message) {
		INFO_LOGGER.log(INFO_INFO_LEVEL, message);
	}

	/**
	 * 모든 정보를 기록하는 경우 사용.
	 */
	public static void all(String message, Exception exception) {
		if (exception == null) {
			ALL_LOGGER.log(ALL_INFO_LEVEL, message);
		} else {
			ALL_LOGGER.log(ALL_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 모든 정보를 기록하는 경우 사용.
	 */
	public static void all(String message) {
		ALL_LOGGER.log(ALL_INFO_LEVEL, message);
	}


	/**
	 * 정적인 구성 메세지 정보를 기록하는 경우 사용.
	 */
	public static void config(String message, Exception exception) {
		if (exception == null) {
			CONFIG_LOGGER.log(CONFIG_INFO_LEVEL, message);
		} else {
			CONFIG_LOGGER.log(CONFIG_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 정적인 구성 메세지 정보를 기록하는 경우 사용.
	 */
	public static void config(String message) {
		CONFIG_LOGGER.log(CONFIG_INFO_LEVEL, message);
	}

	/**
	 * 트레이스 정보 메세지 정보를 기록하는 경우 사용.
	 */
	public static void fine(String message, Exception exception) {
		if (exception == null) {
			FINE_LOGGER.log(FINE_INFO_LEVEL, message);
		} else {
			FINE_LOGGER.log(FINE_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 트레이스 정보 메세지 정보를 기록하는 경우 사용.
	 */
	public static void fine(String message) {
		FINE_LOGGER.log(FINE_INFO_LEVEL, message);
	}

	/**
	 * 상세한 트레이스 메세지 정보를 기록하는 경우 사용.
	 */
	public static void finer(String message, Exception exception) {
		if (exception == null) {
			FINER_LOGGER.log(FINER_INFO_LEVEL, message);
		} else {
			FINER_LOGGER.log(FINER_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 상세한 트레이스 메세지 정보를 기록하는 경우 사용.
	 */
	public static void finer(String message) {
		FINER_LOGGER.log(FINER_INFO_LEVEL, message);
	}

	/**
	 * 중대한 장해 메세지 정보를 기록하는 경우 사용.
	 */
	public static void severe(String message, Exception exception) {
		if (exception == null) {
			SEVERE_LOGGER.log(SEVERE_INFO_LEVEL, message);
		} else {
			SEVERE_LOGGER.log(SEVERE_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 중대한 장해 메세지 정보를 기록하는 경우 사용.
	 */
	public static void sever(String message) {
		SEVERE_LOGGER.log(SEVERE_INFO_LEVEL, message);
	}

	/**
	 * 잠재적인 문제 메세지 정보를 기록하는 경우 사용.
	 */
	public static void warning(String message, Exception exception) {
		if (exception == null) {
			WARNING_LOGGER.log(WARNING_INFO_LEVEL, message);
		} else {
			WARNING_LOGGER.log(WARNING_INFO_LEVEL, message, exception);
		}
	}

	/**
	 * 잠재적인 문제 메세지 정보를 기록하는 경우 사용.
	 */
	public static void warning(String message) {
		WARNING_LOGGER.log(WARNING_INFO_LEVEL, message);
	}
}
