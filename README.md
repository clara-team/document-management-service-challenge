# 📄 Document Management API Challenge

## Overview 🚀

In this challenge, you will build a backend API service to manage large PDF documents. The service must allow users to upload, search, and download PDF documents while efficiently handling resources given a memory limitation of 50MB. This challenge is designed for a mid-senior engineer to demonstrate advanced skills in Spring Boot, Java, REST API development, testing, containerization, and cloud storage integration.

## Functional Requirements ✅

### 1. Upload Endpoint ⬆️

- **Functionality:**  
  Allow uploading a PDF document along with the following metadata:
    - **User:** A string identifying the user associated with the document.
    - **Document Name:** The name provided in the request will be used as the file name.
    - **Tags:** A list of tags associated with the document.

- **Technical Constraints:**
    - The service must handle PDF uploads of up to 500MB.
    - The uploaded PDF should be stored in an S3 bucket (simulated via LocalStack) with the following directory structure:
      ```
      document-bucket/
        ├─ user1/
        │  ├─ doc1.pdf
        │  ├─ doc2.pdf
        ├─ user2/
        │  ├─ doc3.pdf
      ```
    - Metadata must be persisted in a PostgreSQL database with the following fields:
        - **User** (string)
        - **Document Name**
        - **Tags**
        - **S3 Path**
        - **File Size**
        - **File Type**
        - **Created At**

### 2. Search Endpoint 🔍

- **Functionality:**  
  Allow querying documents with optional filters:
    - **Filters:** User, Document Name, and Tags.
    - If no filters are provided, return all documents.
    - Results should be ordered by `created_at` in descending order.
    - The endpoint must support pagination using `page` and `size` parameters.

- **Note:**  
  This endpoint should not return any download URL.

### 3. Download Endpoint ⬇️

- **Functionality:**  
  Allow downloading a document using its ID.

- **Implementation:**  
  Generate a temporary download URL using the AWS SDK’s pre-signed URL functionality.

## Technical Requirements ⚙️

- **Memory Limitation:**  
  The service memory is limited to 50MB. You must design your solution to efficiently manage memory during file upload and processing, even when handling uploads of files up to 500MB.

- **Concurrent Uploads:**  
  The system must be capable of handling up to 10 documents being uploaded in parallel, with each document having a size of up to 500MB.

- **Provided Artifacts:**
    - Controller signatures for the three endpoints.
    - A docker-compose stack that includes PostgreSQL, LocalStack, and the Document Management Service.
    - Integrated tools:
      - **Spring Boot:** The project is pre-configured with Spring Boot.
      - **Spring Data JPA:** For database operations.
      - **Lombok:** For reducing boilerplate code.
      - **JUnit 5:** For unit and integration testing.
      - **Mockito:** For mocking dependencies in tests.
      - **AssertJ:** For fluent assertions in tests.
      - **LocalStack configuration:** For simulating AWS services locally.
      - **Jacoco:** for code coverage (run `./mvnw jacoco:report` to generate the report).
      - **Spotless:** for code formatting (run `./mvnw spotless:apply` to format your code).

- **Java Version:**  
  The project is configured with Java 17, but you may restrict your solution to features available in Java 8 if necessary.

- **Schema Management:**  
  Provide a script for creating the database schema, ensuring efficient handling of multiple tags per document.

- **Documentation:**  
  (Optional) Include OpenAPI documentation for the API endpoints.

## Implementation Instructions 🛠️

1. Use this repository as the starting point for your solution.
2. Configure an AWS S3 client.
3. Configure a connection to PostgreSQL (do not use default values).
4. The controller method signatures with mock responses are provided; please do not modify them unless implementing the functionality.
5. Read the `how-to-load-db-script.md` document for instructions on loading your database schema into the PostgreSQL container provided in the docker-compose stack.
6. Create the Dockerfile for the `document-management-service-challenge`.
7. Once your functionality is ready, validate it using Postman or the web client included in the docker-compose stack. Please note that you must start the stack using docker-compose. Refer to the `client-web.md` document for usage instructions.
8. Commit your changes. It is recommended to maintain a clean commit history, ideally using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0-beta.4/).
9. Push your changes to a personal GitHub account and share the URL of your solution.

## Evaluation Criteria 🏆

- **Database Schema and Indexing:**  
  Evaluate the efficiency of your database schema, including the creation of indices and the management of multiple tags per document.

- **Design Patterns and Best Practices:**  
  Assess the use of design patterns (e.g., Controller-Service-Repository or Hexagonal Architecture) and adherence to SOLID principles and clean code practices.

- **Code Quality:**  
  Review for readability, maintainability, proper exception handling, and overall coding standards.

- **Testing:**  
  Evaluate the quality and coverage of unit and integration tests. While no specific coverage percentage is required, tests should cover the most critical functionalities and edge cases.

- **Spring Boot and Java Proficiency:**  
  Demonstrate effective use of Spring Boot features and Java (preferably Java 17, though Java 8+ is acceptable).

- **Additional Considerations:**
    - Overall robustness and efficiency under concurrent file uploads.
    - Validations on models and DTOs (e.g., non-null constraints).
    - (Optional) OpenAPI documentation.

## Challenge Priorities 🎯

1. **Upload Service:**
    - Primary focus on implementing a robust upload endpoint that efficiently handles large file uploads within the 50MB memory constraint.

2. **Search Service:**
    - Implement a flexible and efficient search endpoint with filtering, sorting, and pagination.

3. **Download Service:**
    - Provide document download functionality via temporary AWS S3 URLs.

> **Note:** It is acceptable to implement a subset of the endpoints. However, the more complete your solution, the better.

## Submission Instructions 📤

Ensure that your solution includes the Dockerfile and database schema script, and that it adheres to the challenge requirements.

---
