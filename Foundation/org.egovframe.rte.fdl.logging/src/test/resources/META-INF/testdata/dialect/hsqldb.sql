-- 2014.01.13 추가 log4j 2 testdata
-- This SQL script creates the required tables by org.apache.logging.log4j.core.appender.db.jdbc.JDBCAppender
--
-- It is intended for HSQLDB. 

DROP TABLE db_log IF EXISTS;
COMMIT;

CREATE TABLE db_log (
  eventDate timestamp NOT NULL,
  level VARCHAR(254),
  logger VARCHAR(254),
  message LONGVARCHAR,
  exception LONGVARCHAR
);
COMMIT;
