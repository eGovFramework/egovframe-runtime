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

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

/**
 * Utility class  to support to close resources
 *
 * @author Vincent Han
 * @since 2015.02.05
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2015.02.05	ESFC				최초 생성
 * 2020.08.31	ESFC				ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 */
public final class EgovResourceReleaser {

	private EgovResourceReleaser() {
	}

	/**
	 * Resource close 처리.
	 */
	public static void close(Closeable  ... resources) {
		for (Closeable resource : resources) {
			if (resource != null) {
				try {
					resource.close();
				// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
				} catch (IOException ignore) {
					EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
				}
			}
		}
	}

	/**
	 * JDBC 관련 resource 객체 close 처리
	 */
	public static void closeDBObjects(Wrapper... objects) {
		for (Object object : objects) {
			if (object != null) {
				if (object instanceof ResultSet) {
					try {
						((ResultSet) object).close();
					// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
					} catch (SQLException ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
					}
				} else if (object instanceof Statement) {
					try {
						((Statement) object).close();
					// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
					} catch (SQLException ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
					}
				} else if (object instanceof Connection) {
					try {
						((Connection) object).close();
					// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
					} catch (SQLException ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
					}
				} else {
					throw new IllegalArgumentException("Wrapper type is not found : " + object.toString());
				}
			}
		}
	}

	/**
	 * Socket 관련 resource 객체 close 처리
	 */
	public static void closeSocketObjects(Socket socket, ServerSocket server) {
		exceptionHandling(socket);

		if (server != null) {
			try {
				server.close();
			// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
			} catch (IOException ignore) {
				EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
			}
		}
	}

	/**
	 *  Socket 관련 resource 객체 close 처리
	 */
	public static void closeSockets(Socket ... sockets) {
		for (Socket socket : sockets) {
			exceptionHandling(socket);
		}
	}

	/**
	 * Exception 처리
	 */
	protected static void exceptionHandling(Socket socket) {
		if (socket != null) {
			try {
				socket.shutdownOutput();
			// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
			} catch (IOException ignore) {
				EgovJdkLogger.ignore("Occurred Exception to shutdown output is ignored.");
			}

			try {
				socket.close();
			// 2020.08.31 ESFC ES-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
			} catch (IOException ignore) {
				EgovJdkLogger.ignore("Occurred Exception to close resource is ignored.");
			}
		}
	}

}
