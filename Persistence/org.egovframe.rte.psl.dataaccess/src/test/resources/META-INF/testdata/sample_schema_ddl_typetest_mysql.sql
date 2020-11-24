DROP TABLE IF EXISTS LOBTEST;

DROP TABLE IF EXISTS TYPETEST;

create table LOBTEST (
    id numeric(10,0) not null,
    blob_type longblob,
    clob_type longtext,
    primary key (id)
);

create table TYPETEST (
    id numeric(10,0) not null,
    bigdecimal_type numeric(19,2),
    boolean_type boolean, 
    byte_type tinyint,
    char_type char(1),
    double_type double,
    float_type float,
    int_type integer,
    long_type bigint,
    short_type smallint,
    string_type varchar(255),
    date_type date,
	sql_date_type datetime,
    sql_time_type time,
    sql_timestamp_type timestamp,
    calendar_type timestamp,
    primary key (id)
);
