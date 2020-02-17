package egovframework.rte.fdl.logging.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.springframework.stereotype.Component;

@SuppressWarnings("deprecation")
@Component("markerFilterTestService")
public class MarkerFilterTestSample {
		// logger: level=DEBUG, appender=File
	private static Logger logger = LogManager.getLogger("markerFilterLogger");

		private static final Marker SQL_MARKER = MarkerManager.getMarker("SQL");

		// 아래 모든 MARKER의 부모 마커는 SQL
		private static final Marker SELECT_MARKER = MarkerManager.getMarker("SELECT", SQL_MARKER);
		private static final Marker INSERT_MARKER = MarkerManager.getMarker("INSERT", SQL_MARKER);
		private static final Marker UPDATE_MARKER = MarkerManager.getMarker("UPDATE", SQL_MARKER);
		private static final Marker DELETE_MARKER = MarkerManager.getMarker("DELETE", SQL_MARKER);

		public void doSelectUser(String userId) throws Exception {
			logger.debug(SQL_MARKER, "SQL - selectUser - gernerated userId : {} ", userId);
			logger.debug(SELECT_MARKER, "SELECT - selectUser - gernerated userId : {} ", userId);
		}

		public void doInsertUser(String userId) throws Exception {
			logger.debug(SQL_MARKER, "SQL - insertUser - gernerated userId : {} ", userId);
			logger.debug(INSERT_MARKER, "INSERT - insertUser - gernerated userId : {} ", userId);
		}

	    public void doUpdateUser(String userId) throws Exception {
	    	logger.debug(SQL_MARKER, "SQL - updateUser - gernerated userId : {} ", userId);
			logger.debug(UPDATE_MARKER, "UPDATE - updateUser - gernerated userId : {} ", userId);
	    }

	    public void doDeleteUser(String userId) throws Exception {
	    	logger.debug(SQL_MARKER, "SQL - deleteUser - gernerated userId : {} ", userId);
	    	logger.debug(DELETE_MARKER, "DELETE - deleteUser - gernerated userId : {} ", userId);
	    }
}
