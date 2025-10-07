--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.
CREATE SCHEMA IF NOT EXISTS document_schema;
SET SCHEMA 'document_schema';
CREATE TABLE IF NOT EXISTS documents (
                                         id UUID PRIMARY KEY,
                                         user_id BIGINT NOT NULL,
                                         document_name VARCHAR(255) NOT NULL,
                                         tags TEXT,
                                         minio_path VARCHAR(512),
                                         document_url VARCHAR(512),
                                         file_size BIGINT,
                                         file_type VARCHAR(50),
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         checksum VARCHAR(255),
                                         status VARCHAR(50) DEFAULT 'active'
);

DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'document_schema'
              AND table_name = 'documents'
              AND udt_name = '_text'
        ) THEN
            EXECUTE 'ALTER TABLE document_schema.documents
                 ALTER COLUMN tags TYPE TEXT
                 USING array_to_string(tags, '','');';
        END IF;
    END $$;

CREATE INDEX IF NOT EXISTS idx_documents_checksum ON document_schema.documents (checksum);
CREATE INDEX IF NOT EXISTS uq_documents_checksum ON document_schema.documents (checksum);
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON document_schema.documents (user_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON document_schema.documents (status);
-- search by document_name
CREATE INDEX IF NOT EXISTS idx_documents_docname ON document_schema.documents (document_name);