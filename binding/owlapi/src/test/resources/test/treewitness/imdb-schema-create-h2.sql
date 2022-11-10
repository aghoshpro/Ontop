CREATE TABLE NAME
(
  ID INT NOT NULL,
  NAME TEXT NOT NULL,
  IMDB_INDEX VARCHAR(12),
  IMDB_ID INTEGER,
  NAME_PCODE_CF VARCHAR(5),
  NAME_PCODE_NF VARCHAR(5),
  SURNAME_PCODE VARCHAR(5),
  CONSTRAINT NAME_PKEY PRIMARY KEY (ID)
);

CREATE TABLE INFO_TYPE
(
  ID INT NOT NULL,
  INFO VARCHAR(32) NOT NULL,
  CONSTRAINT INFO_TYPE_PKEY PRIMARY KEY (ID),
  CONSTRAINT INFO_TYPE_INFO_KEY UNIQUE (INFO)
);

CREATE TABLE KIND_TYPE
(
  ID INTEGER NOT NULL,
  KIND VARCHAR(15) NOT NULL,
  CONSTRAINT KIND_TYPE_PKEY PRIMARY KEY (ID),
  CONSTRAINT KIND_TYPE_KIND_KEY UNIQUE (KIND)
);


CREATE TABLE COMPANY_TYPE
(
  ID INT NOT NULL,
  KIND VARCHAR(32) NOT NULL,
  CONSTRAINT COMPANY_TYPE_PKEY PRIMARY KEY (ID),
  CONSTRAINT COMPANY_TYPE_KIND_KEY UNIQUE (KIND)
);



CREATE TABLE ROLE_TYPE
(
  ID INT NOT NULL,
  ROLE VARCHAR(32) NOT NULL,
  CONSTRAINT ROLE_TYPE_PKEY PRIMARY KEY (ID),
  CONSTRAINT ROLE_TYPE_ROLE_KEY UNIQUE (ROLE)
);


CREATE TABLE CHAR_NAME
(
  ID INT NOT NULL,
  NAME TEXT NOT NULL,
  IMDB_INDEX VARCHAR(12),
  IMDB_ID INTEGER,
  NAME_PCODE_NF VARCHAR(5),
  SURNAME_PCODE VARCHAR(5),
  CONSTRAINT CHAR_NAME_PKEY PRIMARY KEY (ID)
);


CREATE TABLE TITLE
(
  ID INT NOT NULL,
  TITLE TEXT NOT NULL,
  IMDB_INDEX VARCHAR(12),
  KIND_ID INTEGER NOT NULL,
  PRODUCTION_YEAR INTEGER,
  IMDB_ID INTEGER,
  PHONETIC_CODE VARCHAR(5),
  EPISODE_OF_ID INTEGER,
  SEASON_NR INTEGER,
  EPISODE_NR INTEGER,
  SERIES_YEARS VARCHAR(49),
  CONSTRAINT TITLE_PKEY PRIMARY KEY (ID),
  CONSTRAINT EPISODE_OF_ID_EXISTS FOREIGN KEY (EPISODE_OF_ID)
      REFERENCES TITLE (ID)
--  CONSTRAINT KIND_ID_EXISTS FOREIGN KEY (KIND_ID)
--      REFERENCES KIND_TYPE (ID)
);

CREATE TABLE "CAST_INFO"
(
  ID INT NOT NULL,
  PERSON_ID INTEGER NOT NULL,
  MOVIE_ID INTEGER NOT NULL,
  PERSON_ROLE_ID INTEGER,
  NOTE TEXT,
  NR_ORDER INTEGER,
  ROLE_ID INTEGER NOT NULL,
  CONSTRAINT CAST_INFO_PKEY PRIMARY KEY (ID),
  CONSTRAINT MOVIE_ID_EXISTS FOREIGN KEY (MOVIE_ID)
      REFERENCES TITLE (ID),
  CONSTRAINT CAST_INFO_PERSON_ID_EXISTS FOREIGN KEY (PERSON_ID)
      REFERENCES NAME (ID)
--  CONSTRAINT PERSON_ROLE_ID_EXISTS FOREIGN KEY (PERSON_ROLE_ID)
--      REFERENCES CHAR_NAME (ID),
--  CONSTRAINT ROLE_ID_EXISTS FOREIGN KEY (ROLE_ID)
--      REFERENCES ROLE_TYPE (ID) 
);


CREATE TABLE AKA_NAME
(
  ID INT NOT NULL,
  PERSON_ID INTEGER NOT NULL,
  NAME TEXT NOT NULL,
  IMDB_INDEX VARCHAR(12),
  NAME_PCODE_CF VARCHAR(5),
  NAME_PCODE_NF VARCHAR(5),
  SURNAME_PCODE VARCHAR(5),
  CONSTRAINT AKA_NAME_PKEY PRIMARY KEY (ID),
  CONSTRAINT AKA_NAME_PERSON_ID_EXISTS FOREIGN KEY (PERSON_ID)
      REFERENCES NAME (ID) 
);

CREATE TABLE COMPANY_NAME
(
  ID INT NOT NULL,
  NAME TEXT NOT NULL,
  COUNTRY_CODE VARCHAR(255),
  IMDB_ID INTEGER,
  NAME_PCODE_NF VARCHAR(5),
  NAME_PCODE_SF VARCHAR(5),
  CONSTRAINT COMPANY_NAME_PKEY PRIMARY KEY (ID)
);


CREATE TABLE MOVIE_COMPANIES
(
  ID INT NOT NULL,
  MOVIE_ID INTEGER NOT NULL,
  COMPANY_ID INTEGER NOT NULL,
  COMPANY_TYPE_ID INTEGER NOT NULL,
  NOTE TEXT,
  CONSTRAINT MOVIE_COMPANIES_PKEY PRIMARY KEY (ID),
  CONSTRAINT COMPANY_ID_EXISTS FOREIGN KEY (COMPANY_ID)
      REFERENCES COMPANY_NAME (ID),
--  CONSTRAINT COMPANY_TYPE_ID_EXISTS FOREIGN KEY (COMPANY_TYPE_ID)
--      REFERENCES COMPANY_TYPE (ID) ,
  CONSTRAINT MOVIE_COMPANIES_MOVIE_ID_EXISTS FOREIGN KEY (MOVIE_ID)
      REFERENCES TITLE (ID) 
);


CREATE TABLE PERSON_INFO
(
  ID INT NOT NULL,
  PERSON_ID INTEGER NOT NULL,
  INFO_TYPE_ID INTEGER NOT NULL,
  INFO TEXT NOT NULL,
  NOTE TEXT,
  CONSTRAINT PERSON_INFO_PKEY PRIMARY KEY (ID),
--  CONSTRAINT INFO_TYPE_ID_EXISTS FOREIGN KEY (INFO_TYPE_ID)
--      REFERENCES INFO_TYPE (ID),
  CONSTRAINT PERSON_ID_EXISTS FOREIGN KEY (PERSON_ID)
      REFERENCES NAME (ID)
);


CREATE TABLE MOVIE_INFO_IDX
(
  ID INT NOT NULL,
  MOVIE_ID INTEGER NOT NULL,
  INFO_TYPE_ID INTEGER NOT NULL,
  INFO TEXT NOT NULL,
  NOTE TEXT,
  CONSTRAINT MOVIE_INFO_IDX_PKEY PRIMARY KEY (ID),
--  CONSTRAINT MOVIE_INFO_IDX_INFO_TYPE_ID_EXISTS FOREIGN KEY (INFO_TYPE_ID)
--      REFERENCES INFO_TYPE (ID),
  CONSTRAINT MOVIE_INFO_IDX_MOVIE_ID_EXISTS FOREIGN KEY (MOVIE_ID)
      REFERENCES TITLE (ID)
);

CREATE TABLE MOVIE_INFO
(
  ID INT NOT NULL,
  MOVIE_ID INTEGER NOT NULL,
  INFO_TYPE_ID INTEGER NOT NULL,
  INFO TEXT NOT NULL,
  NOTE TEXT,
  CONSTRAINT MOVIE_INFO_PKEY PRIMARY KEY (ID),
--  CONSTRAINT MOVIE_INFO_INFO_TYPE_ID_EXISTS FOREIGN KEY (INFO_TYPE_ID)
--      REFERENCES INFO_TYPE (ID),
  CONSTRAINT MOVIE_INFO_MOVIE_ID_EXISTS FOREIGN KEY (MOVIE_ID)
      REFERENCES TITLE (ID) 
);

