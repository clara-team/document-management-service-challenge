# 📄 Document Management API Challenge

## Description

In this challenge, you will build a backend API service to manage **large PDF documents**. The service must allow users to upload, search, and download PDF documents while efficiently handling resources, given a **memory limitation of 50MB assigned to the document management service container**. This challenge is designed for a mid-senior engineer to demonstrate advanced skills in **Spring Boot, Java, REST API development, testing, containerization, and cloud storage integration**.

## Defining env variables

Create a .env file in the root directory of the project and add the following variables:

```
PG_HOST=[host]
PG_PORT=[port]
PG_DATABASE=[db_name]
PG_USER=[user]
PG_PASSWORD=[password]
MINIO_ENDPOINT=[minio_endpoint]
MINIO_ACCESS_KEY=[minio_access_key]
MINIO_SECRET_KEY=[minio_secret_key]
MINIO_BUCKET_NAME=[minio_bucket_name]
```

## Testing the application

To test the application, use the following command:

```
docker-compose -f docker/docker-compose.yml --env-file .env up --build
```

## Swagger

The Swagger documentation is available at http://localhost:8080/swagger-ui/index.html.
