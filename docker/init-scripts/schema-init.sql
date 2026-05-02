CREATE SCHEMA IF NOT EXISTS document_schema;


CREATE TABLE IF NOT EXISTS document_schema.documents
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unique (user_id, document_name)
);

CREATE TABLE IF NOT EXISTS document_schema.document_tags
(
    document_id BIGINT NOT NULL REFERENCES document_schema.documents (id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (document_id, tag)
);

CREATE INDEX idx_documents_user_id ON document_schema.documents (user_id);
CREATE INDEX idx_documents_document_name ON document_schema.documents (document_name);
CREATE INDEX idx_documents_user_id_document_name ON document_schema.documents (user_id, document_name);
CREATE INDEX idx_document_tags_tag ON document_schema.document_tags (tag);

