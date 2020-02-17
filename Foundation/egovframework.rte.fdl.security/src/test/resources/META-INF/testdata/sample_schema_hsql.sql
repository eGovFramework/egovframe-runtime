CREATE TABLE SAMPLE (ID VARCHAR(16) NOT NULL PRIMARY KEY,NAME VARCHAR(50),DESCRIPTION VARCHAR(100),USE_YN CHAR(1),REG_USER VARCHAR(10));

INSERT INTO SAMPLE VALUES('SAMPLE-00002','Sample Test','This is initial test data.','Y','test');
INSERT INTO SAMPLE VALUES('SAMPLE-00083','test Name','test Desc','Y','test');


CREATE TABLE USERS(USER_ID VARCHAR(20) NOT NULL,USER_NAME VARCHAR(50) NOT NULL,PASSWORD VARCHAR(30) NOT NULL,ENABLED INTEGER,SSN VARCHAR(13),SL_YN CHAR(1),BIRTH_DAY VARCHAR(8),AGE NUMERIC(3),CELL_PHONE VARCHAR(14),ADDR VARCHAR(100),EMAIL VARCHAR(50),EMAIL_YN CHAR(1),IMAGE_FILE VARCHAR(100),REG_DATE DATE,CONSTRAINT PK_USERS PRIMARY KEY(USER_ID));

INSERT INTO USERS VALUES('user','Hong Gil-dong','7hHLsZBS5AsHqsDKBgwj7g==',1,'8006041227717','Y','19800603',29,'010-9949-6484','Sinsa-Dong, Gangnam-Gu, Seoul-Si','bbnydory@google.com','Y','','2008-06-04');
INSERT INTO USERS VALUES('buyer','Lee, Man-hong','eUqtJMvVhGEBHtkJS3+iEg==',1,'1234567890123','Y','19701231',39,'010-9290-9283','Yeouido-dong, Yeongdeungpo-gu, Seoul','manhong@naver.com','Y','','2008-06-24');
INSERT INTO USERS VALUES('test','Kim, Young-Su','CY9rzUYh03PK3k6DJie09g==',1,'1234567890123','Y','19800604',29,'010-6456-4492','Gumi-Dong, Bundang-Gu, Seongnam-Si, Gyeonggi-Do','test@empal.com','Y','','2008-03-13');
INSERT INTO USERS VALUES('jimi','jimi test','lMoR6BOUb15HW6Lm4F/VsA==',1,'1234567890123','Y','19800604',29,'010-6456-4492','Gumi-Dong, Bundang-Gu, Seongnam-Si, Gyeonggi-Do','test@empal.com','Y','','2008-03-13');


CREATE TABLE ROLES(AUTHORITY VARCHAR(50) NOT NULL,ROLE_NAME VARCHAR(50),DESCRIPTION VARCHAR(100),CREATE_DATE DATE,MODIFY_DATE DATE,CONSTRAINT PK_ROLES PRIMARY KEY(AUTHORITY));
CREATE TABLE ROLES_HIERARCHY(PARENT_ROLE VARCHAR(50) NOT NULL,CHILD_ROLE VARCHAR(50) NOT NULL,CONSTRAINT PK_ROLES_HIERARCHY PRIMARY KEY(PARENT_ROLE,CHILD_ROLE),CONSTRAINT FK_ROLES1 FOREIGN KEY(PARENT_ROLE) REFERENCES ROLES(AUTHORITY),CONSTRAINT FK_ROLES2 FOREIGN KEY(CHILD_ROLE) REFERENCES ROLES (AUTHORITY));
CREATE TABLE SECURED_RESOURCES(RESOURCE_ID VARCHAR(10) NOT NULL,RESOURCE_NAME VARCHAR(50),RESOURCE_PATTERN VARCHAR(300) NOT NULL,DESCRIPTION VARCHAR(100),RESOURCE_TYPE VARCHAR(10),SORT_ORDER INTEGER,CREATE_DATE DATE,MODIFY_DATE DATE,CONSTRAINT PK_RECURED_RESOURCES PRIMARY KEY(RESOURCE_ID));
CREATE TABLE SECURED_RESOURCES_ROLE(RESOURCE_ID VARCHAR(10) NOT NULL,AUTHORITY VARCHAR(50) NOT NULL,CONSTRAINT PK_SECURED_RESOURCES_ROLE PRIMARY KEY(RESOURCE_ID,AUTHORITY),CONSTRAINT FK_SECURED_RESOURCES FOREIGN KEY(RESOURCE_ID) REFERENCES SECURED_RESOURCES(RESOURCE_ID),CONSTRAINT FK_ROLES4 FOREIGN KEY (AUTHORITY) REFERENCES ROLES(AUTHORITY));

-- roles ;
insert into roles(authority, role_name, description, create_date) values ('IS_AUTHENTICATED_ANONYMOUSLY', '익명 사용자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('IS_AUTHENTICATED_REMEMBERED', 'REMEMBERED 사용자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('IS_AUTHENTICATED_FULLY', '인증된 사용자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('ROLE_RESTRICTED', '제한된 사용자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('ROLE_USER', '일반 사용자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('ROLE_ADMIN', '관리자', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('ROLE_A', 'A 업무', '', '2008-11-10');
insert into roles(authority, role_name, description, create_date) values ('ROLE_B', 'B 업무', '', '2008-11-10');

-- roles hierarchy ;
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_ADMIN', 'ROLE_USER');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_USER', 'ROLE_RESTRICTED');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_RESTRICTED', 'IS_AUTHENTICATED_FULLY');
insert into roles_hierarchy(child_role, parent_role) values ('IS_AUTHENTICATED_FULLY', 'IS_AUTHENTICATED_REMEMBERED');
insert into roles_hierarchy(child_role, parent_role) values ('IS_AUTHENTICATED_REMEMBERED', 'IS_AUTHENTICATED_ANONYMOUSLY');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_ADMIN', 'ROLE_A');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_ADMIN', 'ROLE_B');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_A', 'ROLE_RESTRICTED');
insert into roles_hierarchy(child_role, parent_role) values ('ROLE_B', 'ROLE_RESTRICTED');

CREATE TABLE AUTHORITIES(USER_ID VARCHAR(20) NOT NULL,AUTHORITY VARCHAR(50) NOT NULL,CONSTRAINT PK_AUTHORITIES PRIMARY KEY(USER_ID,AUTHORITY),CONSTRAINT FK_USERS FOREIGN KEY(USER_ID) REFERENCES USERS(USER_ID),CONSTRAINT FK_ROLES3 FOREIGN KEY(AUTHORITY) REFERENCES ROLES(AUTHORITY));

INSERT INTO AUTHORITIES(USER_ID, AUTHORITY) VALUES('user','ROLE_USER');
INSERT INTO AUTHORITIES(USER_ID, AUTHORITY) VALUES('buyer','ROLE_RESTRICTED');
INSERT INTO AUTHORITIES(USER_ID, AUTHORITY) VALUES('test','ROLE_ADMIN');
INSERT INTO AUTHORITIES(USER_ID, AUTHORITY) VALUES('jimi','ROLE_ADMIN');

-- regex type ;
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('web-000001', 'test_resource_1', '\A/test\.do\Z', '', 'url', 1, '2009-03-16');
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('web-000002', 'test_resource_2', '\A/sale/.*\.do\Z', '', 'url', 100, '2009-03-16');
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('web-000003', 'test_resource_3', '\A/cvpl/((?!EgovCvplLogin\.do).)*\Z', '', 'url', 1000, '2009-03-16');
--insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('web-000004', 'test_resource_4', '\A/reloadAuthMapping\.do.*\Z', '', 'url', 10, '2009-03-16');

-- resource mapping ;
insert into secured_resources_role(resource_id, authority) values ('web-000001', 'ROLE_USER');
insert into secured_resources_role(resource_id, authority) values ('web-000002', 'ROLE_RESTRICTED');
insert into secured_resources_role(resource_id, authority) values ('web-000003', 'IS_AUTHENTICATED_ANONYMOUSLY');
--insert into secured_resources_role(resource_id, authority) values ('web-000004', 'ROLE_ADMIN');

-- multi Role test ;
--insert into secured_resources_role(resource_id, authority) values ('web-000001', 'ROLE_ADMIN');

-- method ;
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('mtd-000001', 'test_resource_1', 'egovframework.rte.fdl.security.web.CategoryController.selectCategoryList', '', 'method', 1, '2009-03-16');
--insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('mtd-000002', 'test_resource_2', 'egovframework.rte.fdl.security.service.EgovSampleService.deleteSample', '', 'method', 100, '2009-03-16');
insert into secured_resources (resource_id, resource_name, resource_pattern, description, resource_type, sort_order, create_date) values ('mtd-000003', 'test_resource_3', 'execution(* egovframework.rte.security..service.*Service.insert*(..))', '', 'pointcut', 1000, '2009-03-16');

-- resource mapping ;
insert into secured_resources_role(resource_id, authority) values ('mtd-000001', 'ROLE_USER');
--insert into secured_resources_role(resource_id, authority) values ('mtd-000002', 'ROLE_ADMIN');

-- multi Role test ;
--insert into secured_resources_role(resource_id, authority) values ('mtd-000001', 'ROLE_ADMIN');

-- pointcut test ;
insert into secured_resources_role(resource_id, authority) values ('mtd-000003', 'ROLE_USER');

commit;