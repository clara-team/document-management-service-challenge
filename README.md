#  Document Management Service

## Overview

This service provides a REST API to upload, search, and download large PDF documents using:

- Java 17 + Spring Boot
- PostgreSQL
- MinIO (S3-compatible storage)
- Docker

The system is designed to handle large file uploads (up to 500MB) under a strict **50MB memory constraint**, ensuring efficient resource utilization.

---

##  Key Design Decisions

### Memory Management
- File uploads are handled using streaming (`InputStream`)
- Files are **never fully loaded into memory**
- JVM memory is constrained using:

```
-Xmx50m
```

This ensures compliance with the 50MB memory requirement.

---

### Storage Strategy
- Documents are stored in MinIO using the structure:

```
document-bucket/
  ├─ {user}/
      ├─ {documentName}.pdf
```

- Metadata is stored in PostgreSQL
- Tags are normalized in a separate table (`document_tags`)

---

### Scalability Considerations
- Stateless service design
- Supports concurrent uploads (handled by Spring Boot thread pool)
- Efficient DB indexing on search fields

---

##  Configuration

The application supports environment-based configuration:

```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

MINIO_ENDPOINT
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
MINIO_BUCKET
```

Defaults are provided via `application.yml`.

---

##  Database

Schema is initialized automatically:

```
docker/init-scripts/schema-init.sql
```

Tables:
- `documents`
- `document_tags`

---

##  How to Run

### Docker (Recommended)

```bash
./mvnw.cmd clean package -DskipTests
cd docker
docker-compose up -d --build
```

Services:
- API: http://localhost:8080
- MinIO Console: http://localhost:9001
- PostgreSQL: localhost:5433

---

### Local Execution

```bash
./mvnw.cmd spring-boot:run
```

---

##  API Endpoints

### Upload

```
POST /documents
```

Form-data:
- file (PDF)
- userName
- documentName
- tags

---

### Search

```
GET /documents
```

Filters:
- userName
- documentName
- tag

Supports pagination and sorting by `createdAt DESC`.

---

### Download

```
GET /documents/{id}/download
```

Returns a pre-signed URL for secure access.

---

## ⚠️ Validations

- Only PDF files are accepted
- Required fields validated
- Global exception handling implemented

---

##  Testing

Run tests:

```bash
./mvnw.cmd test
```

Basic tests included for:
- Application context
- Validation flows

---

##  Code Quality

Run:

```bash
./mvnw.cmd clean verify
```

Coverage:

```bash
./mvnw.cmd jacoco:report
```

Formatting:

```bash
./mvnw.cmd spotless:apply
```

---

##  Architecture

```
controller → service → repository → database
                        ↓
                     MinIO
```

---

## 📌 Notes

- Uses pre-signed URLs for secure file access
- Bucket must exist in MinIO
- Designed for containerized environments

---

## 👤 Author

Andrea Carvajal
