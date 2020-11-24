drop table LOBTEST;

drop table TYPETEST;

create table LOBTEST (
    id number(10,0) not null,
    blob_type blob,
    clob_type clob,
    primary key (id)
);

create table TYPETEST (
    id number(10,0) not null,
    bigdecimal_type number(19,2),
    boolean_type number(1,0), 
    byte_type number(3,0),
    char_type char(1),
    double_type double precision,
    float_type float,
    int_type number(10,0),
    long_type number(19,0),
    short_type number(5,0),
    string_type varchar2(255),
    date_type date,
	sql_date_type date,
    sql_time_type timestamp,
    sql_timestamp_type timestamp,
    calendar_type timestamp,
    primary key (id)
);
