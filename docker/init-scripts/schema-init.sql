CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         user_name VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    minio_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS document_tags (
                                             id BIGSERIAL PRIMARY KEY,
                                             document_id UUID NOT NULL,
                                             tag VARCHAR(100) NOT NULL,
    CONSTRAINT fk_document_tags_document
    FOREIGN KEY (document_id)
    REFERENCES documents(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_documents_user_name ON documents(user_name);
CREATE INDEX IF NOT EXISTS idx_documents_document_name ON documents(document_name);
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_document_tags_document_id ON document_tags(document_id);
CREATE INDEX IF NOT EXISTS idx_document_tags_tag ON document_tags(tag);