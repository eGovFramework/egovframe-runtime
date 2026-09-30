-- EgovReactiveBaseEntity 감사 필드 테스트용 테이블(H2)
DROP TABLE audited_sample IF EXISTS;
COMMIT;

CREATE TABLE audited_sample
(
    id            INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(50),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP
);
COMMIT;
