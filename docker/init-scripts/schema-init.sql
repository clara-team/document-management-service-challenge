--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA document_schema;
SET SCHEMA 'document_schema';

CREATE TABLE document_schema.document (
     id integer GENERATED ALWAYS AS IDENTITY NOT NULL,
     username varchar(200) NOT NULL,
     filename varchar(200) NOT NULL,
     file_path varchar(200) NOT NULL,
     file_size bigint NULL,
     file_type varchar(80) NULL,
     created_at timestamp with time zone NOT NULL,
     CONSTRAINT tb_documento_pk PRIMARY KEY (id)
);

CREATE TABLE document_schema.document_tag (
    id integer GENERATED ALWAYS AS IDENTITY NOT NULL,
    id_document integer NOT NULL,
    tag_name varchar(50) NOT NULL,
    CONSTRAINT tb_tag_pk PRIMARY KEY (id),
    CONSTRAINT tb_tag_tb_document_fk FOREIGN KEY (id_document) REFERENCES document_schema.document(id)
);
