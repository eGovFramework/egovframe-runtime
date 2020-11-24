drop table dual IF EXISTS;

CREATE TABLE dual (
    dummy        VARCHAR(1)
);

INSERT INTO dual VALUES ('X');

commit;