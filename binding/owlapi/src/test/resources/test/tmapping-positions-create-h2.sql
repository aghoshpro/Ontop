
CREATE TABLE TABLE1 (
    uri character varying(100) NOT NULL,
    val1 integer NOT NULL,
    val2 integer NOT NULL,
    val3 integer NOT NULL
);


INSERT INTO TABLE1 VALUES ('http://example.org/1', 1, 0, 0);
INSERT INTO TABLE1 VALUES ('http://example.org/2', 0, 1, 0);
INSERT INTO TABLE1 VALUES ('http://example.org/3', 0, 0, 1);

ALTER TABLE TABLE1
    ADD CONSTRAINT table1_pkey PRIMARY KEY (uri);

CREATE TABLE TABLE2 (
    URI2 character varying(100) NOT NULL,
    VAL integer NOT NULL
);

ALTER TABLE TABLE2
    ADD CONSTRAINT table2_pkey PRIMARY KEY (URI2);

CREATE TABLE TABLE3 (
    URI3 character varying(100) NOT NULL,
    VAL integer NOT NULL
);

ALTER TABLE TABLE3
    ADD CONSTRAINT table3_pkey PRIMARY KEY (URI3);
    