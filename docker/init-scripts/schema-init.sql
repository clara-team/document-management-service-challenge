--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA IF NOT EXISTS challenge;

CREATE TABLE IF NOT EXISTS challenge.document (
    id SERIAL,
    user_id BIGINT,
    name VARCHAR(255),
    minio_path VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS challenge.user (
    id SERIAL,
    name VARCHAR(255) UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS challenge.tag (
    id SERIAL,
    document_id BIGINT,
    name VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE challenge.document DROP CONSTRAINT IF EXISTS document_user_user_id_fkey;

ALTER TABLE challenge.document
    ADD CONSTRAINT document_user_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES challenge.user(id);

ALTER TABLE challenge.tag DROP CONSTRAINT IF EXISTS tag_document_document_id_fkey;

ALTER TABLE challenge.tag
    ADD CONSTRAINT tag_document_document_id_fkey
    FOREIGN KEY (document_id) REFERENCES challenge.document(id);

CREATE INDEX IF NOT EXISTS index_document_name ON challenge.document(name);
CREATE INDEX IF NOT EXISTS index_user_name ON challenge.user(name);
CREATE INDEX IF NOT EXISTS index_tag_name ON challenge.tag(name);

GRANT ALL PRIVILEGES ON SCHEMA challenge TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA challenge TO admin;
