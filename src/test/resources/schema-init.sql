CREATE SCHEMA IF NOT EXISTS document_schema;
SET search_path TO document_schema;

CREATE TABLE documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name   VARCHAR(255) NOT NULL,
    name        VARCHAR(500) NOT NULL,
    minio_path  VARCHAR(1000) NOT NULL,
    file_size   BIGINT NOT NULL,
    file_type   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE document_tags (
    document_id UUID        NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    tag         VARCHAR(255) NOT NULL,
    PRIMARY KEY (document_id, tag)
);

-- Indexes for search filters
CREATE INDEX idx_documents_user    ON documents(user_name);
CREATE INDEX idx_documents_name    ON documents(name);
CREATE INDEX idx_documents_created ON documents(created_at DESC);
CREATE INDEX idx_document_tags_tag ON document_tags(tag);