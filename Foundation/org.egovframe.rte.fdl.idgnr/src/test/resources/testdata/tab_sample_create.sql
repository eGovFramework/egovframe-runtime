DROP TABLE idttest IF EXISTS CASCADE;

DROP TABLE ids IF EXISTS CASCADE;

CREATE TABLE IF NOT EXISTS idttest ( table_name varchar(16) NOT NULL,
					   next_id DECIMAL(30) NOT NULL,
					   PRIMARY KEY (table_name));
				   
INSERT INTO idttest VALUES('id','0');

CREATE TABLE IF NOT EXISTS ids ( table_name varchar(16) NOT NULL,
					   next_id DECIMAL(30) NOT NULL,
					   PRIMARY KEY (table_name));
				   
INSERT INTO ids VALUES('id','0');
