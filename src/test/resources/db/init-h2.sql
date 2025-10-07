CREATE SCHEMA IF NOT EXISTS document_schema;
SET SCHEMA document_schema;

CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         user_id BIGINT NOT NULL,
                                         document_name VARCHAR(255) NOT NULL,
    tags VARCHAR(255),
    minio_path VARCHAR(512),
    document_url VARCHAR(512),
    file_size BIGINT,
    file_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(255),
    status VARCHAR(50) DEFAULT 'active'
    );

CREATE INDEX IF NOT EXISTS idx_documents_checksum ON documents (checksum);
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents (user_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents (status);
CREATE INDEX IF NOT EXISTS idx_documents_docname ON documents (document_name);
