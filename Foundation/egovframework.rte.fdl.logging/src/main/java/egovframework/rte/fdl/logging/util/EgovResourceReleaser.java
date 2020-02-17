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
package egovframework.rte.fdl.logging.util;

import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Wrapper;
/**
 * Utility class  to support to close resources
 * @author Vincent Han
 * @since 2015.02.05
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일        수정자       수정내용
 *  -------       --------    ---------------------------
 *   2015.02.05	표준프레임워크센터	최초 생성
 *
 * </pre>
 */
public final class EgovResourceReleaser {
	private EgovResourceReleaser() {
		// no-op
	}
	
	/**
	 * Resource close 처리.
	 * @param resources
	 */
	public static void close(Closeable  ... resources) {
		for (Closeable resource : resources) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Exception ignore) {
					EgovJdkLogger.ignore("Occurred Exception to close resource is ingored!!");
				}
			}
		}
	}
	
	/**
	 * JDBC 관련 resource 객체 close 처리
	 * @param objects
	 */
	public static void closeDBObjects(Wrapper ... objects) {
		for (Object object : objects) {
			if (object != null) {
				if (object instanceof ResultSet) {
					try {
						((ResultSet) object).close();
					} catch (Exception ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ingored!!");
					}
				} else if (object instanceof Statement) {
					try {
						((Statement) object).close();
					} catch (Exception ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ingored!!");
					}
				} else if (object instanceof Connection) {
					try {
						((Connection) object).close();
					} catch (Exception ignore) {
						EgovJdkLogger.ignore("Occurred Exception to close resource is ingored!!");
					}
				} else {
					throw new IllegalArgumentException("Wrapper type is not found : " + object.toString());
				}
			}
		}
	}
	
	/**
	 * Socket 관련 resource 객체 close 처리
	 * @param objects
	 */
	public static void closeSocketObjects(Socket socket, ServerSocket server) {
		if (socket != null) {
			try {
				socket.shutdownOutput();
			} catch (Exception ignore) {
				EgovJdkLogger.ignore("Occurred Exception to shutdown ouput is ignored!!");
			}
			
			try {
				socket.close();
			} catch (Exception ignore) {
				EgovJdkLogger.ignore("Occurred Exception to close resource is ignored!!");
			}
		}
		
		if (server != null) {
			try {
				server.close();
			} catch (Exception ignore) {
				EgovJdkLogger.ignore("Occurred Exception to close resource is ignored!!");
			}
		}
	}
	
	/**
	 *  Socket 관련 resource 객체 close 처리
	 *  
	 * @param sockets
	 */
	public static void closeSockets(Socket ... sockets) {
		for (Socket socket : sockets) {
			if (socket != null) {
				try {
					socket.shutdownOutput();
				} catch (Exception ignore) {
					EgovJdkLogger.ignore("Occurred Exception to shutdown ouput is ignored!!");
				}
				
				try {
					socket.close();
				} catch (Exception ignore) {
					EgovJdkLogger.ignore("Occurred Exception to close resource is ignored!!");
				}
			}
		}
	}
}
