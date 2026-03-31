-- ====================================
-- DROP TABLES (if they exist)
-- ====================================
DROP TABLE IF EXISTS db_log;
DROP TABLE IF EXISTS logging_event_property;
DROP TABLE IF EXISTS logging_event_exception;
DROP TABLE IF EXISTS logging_event;
COMMIT;

-- ====================================
-- CREATE TABLE: db_log
-- ====================================
CREATE TABLE db_log
(
    eventDate TIMESTAMP NOT NULL,
    level     VARCHAR(254),
    logger    VARCHAR(254),
    message   VARCHAR(4000),
    exception VARCHAR(4000)
);
COMMIT;

-- ====================================
-- CREATE TABLE: logging_event
-- ====================================
CREATE TABLE logging_event
(
    sequence_number  BIGINT        NOT NULL,
    timestamp        BIGINT        NOT NULL,
    rendered_message VARCHAR(4000) NOT NULL,
    logger_name      VARCHAR(254)  NOT NULL,
    level_string     VARCHAR(254)  NOT NULL,
    ndc              VARCHAR(4000),
    thread_name      VARCHAR(254),
    reference_flag   SMALLINT,
    caller_filename  VARCHAR(254)  NOT NULL,
    caller_class     VARCHAR(254)  NOT NULL,
    caller_method    VARCHAR(254)  NOT NULL,
    caller_line      VARCHAR(10)   NOT NULL,
    event_id         INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1) PRIMARY KEY
);
COMMIT;

-- ====================================
-- CREATE TABLE: logging_event_property
-- ====================================
CREATE TABLE logging_event_property
(
    event_id     INTEGER      NOT NULL,
    mapped_key   VARCHAR(254) NOT NULL,
    mapped_value VARCHAR(1024)
);
COMMIT;

-- ====================================
-- CREATE TABLE: logging_event_exception
-- ====================================
CREATE TABLE logging_event_exception
(
    event_id   INTEGER  NOT NULL,
    i          SMALLINT NOT NULL,
    trace_line VARCHAR(4000)
);
COMMIT;

-- ====================================
-- ADD FOREIGN KEYS
-- ====================================
ALTER TABLE logging_event_property
    ADD CONSTRAINT fk_event_prop FOREIGN KEY (event_id) REFERENCES logging_event (event_id);

ALTER TABLE logging_event_exception
    ADD CONSTRAINT fk_event_ex FOREIGN KEY (event_id) REFERENCES logging_event (event_id);
COMMIT;

-- ====================================
-- CREATE INDEXES
-- ====================================
CREATE INDEX idx_logger_name ON logging_event (logger_name);
CREATE INDEX idx_event_id_property ON logging_event_property (event_id);
CREATE INDEX idx_event_id_exception ON logging_event_exception (event_id);
COMMIT;
