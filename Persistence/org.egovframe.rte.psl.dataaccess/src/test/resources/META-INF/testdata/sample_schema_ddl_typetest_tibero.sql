drop table LOBTEST;

drop table TYPETEST;

create table LOBTEST (
    id numeric(10,0) not null,
    blob_type BLOB,
    clob_type CLOB,
    primary key (id)
);

create table TYPETEST (
    id numeric(10,0) not null,
    bigdecimal_type number(19,2),
    boolean_type number(1), 
    byte_type number(3),
    char_type char(1),
    double_type double precision,
    float_type float,
    int_type integer,
    long_type integer, /* integer 가 bigint 의 범위까지 포함함 */
    short_type smallint,
    string_type varchar(255),
    date_type date,
	sql_date_type date,
    sql_time_type date,	/* time, timestamp 으로 설정 시 문제발생 */
    sql_timestamp_type timestamp,
    calendar_type timestamp,
    primary key (id)
);
