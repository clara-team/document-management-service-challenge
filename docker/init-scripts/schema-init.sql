--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.
CREATE SCHEMA IF NOT EXISTS document_schema;
SET SCHEMA 'document_schema';
CREATE TABLE IF NOT EXISTS document_schema.documents (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         user_id BIGINT NOT NULL,
                                                         document_name VARCHAR(255) NOT NULL,
                                                         tags TEXT, -- o JSONB si prefieres
                                                         minio_path VARCHAR(512),
                                                         file_size BIGINT,
                                                         file_type VARCHAR(50),
                                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                         checksum VARCHAR(255),
                                                         status VARCHAR(50)
);
