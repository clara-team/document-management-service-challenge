--The script to initialize the schema was sourced from the Spring Batch Core dependency: org.springframework.batch.core.

CREATE SCHEMA document_schema;
SET SCHEMA 'document_schema';

CREATE TABLE user_app (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL
);

CREATE TABLE document (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  path VARCHAR(255) NOT NULL,
  size BIGINT NOT NULL,
  type VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_app(id)
);

CREATE TABLE document_tags (
  document_id BIGINT NOT NULL,
  tag VARCHAR(255) NOT NULL,
  PRIMARY KEY (document_id, tag),
  FOREIGN KEY (document_id) REFERENCES document(id)
);

INSERT INTO user_app (id, name, username, email) VALUES (1, 'John Doe', 'johndoe', 'johndoe@example.com');
INSERT INTO user_app (id, name, username, email) VALUES (2, 'Jane Doe', 'janedoe', 'janedoe@example.com');
INSERT INTO user_app (id, name, username, email) VALUES (3, 'Bob Smith', 'bobsmith', 'bobsmith@example.com');

INSERT INTO document (id, user_id, name, path, size, type, created_at) VALUES (1, 1, 'Document 1','/path/to/document1.pdf', 1024, 'application/pdf', '2023-01-01 00:00:00');
INSERT INTO document (id, user_id, name, path, size, type, created_at) VALUES (2, 1, 'Document 2', '/path/to/document2.pdf', 2048, 'application/pdf', '2023-01-02 00:00:00');
INSERT INTO document (id, user_id, name, path, size, type, created_at) VALUES (3, 2, 'Document 3', '/path/to/document3.pdf', 4096, 'application/pdf', '2023-01-03 00:00:00');

INSERT INTO document_tags (document_id, tag) VALUES (1, 'tag1');
INSERT INTO document_tags (document_id, tag) VALUES (1, 'tag2');
INSERT INTO document_tags (document_id, tag) VALUES (2, 'tag3');
INSERT INTO document_tags (document_id, tag) VALUES (2, 'tag4');
INSERT INTO document_tags (document_id, tag) VALUES (3, 'tag5');
INSERT INTO document_tags (document_id, tag) VALUES (3, 'tag6');

