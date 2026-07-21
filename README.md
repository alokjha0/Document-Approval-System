# Document Approval System (DAS)

A production-ready **Office Document Clearance & Approval Workflow System** developed using **Spring Boot**, **PostgreSQL**, **Docker**, and **Thymeleaf**.

---

# Features

- Multi-Level Approval Workflow
- Employee / Reviewer / Approver Roles
- Document Versioning
- Audit Logging
- PDF Generation
- PDF Upload
- Rich Text Editor
- Spring Security Authentication
- Department Based Workflow
- Dockerized Deployment
- PostgreSQL Integration

---

# Technology Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Thymeleaf
- Bootstrap
- Docker
- Docker Compose
- Maven

---

# Project Structure

```
Document-Approval-System
│
├── src
├── Dockerfile
├── compose.yaml
├── .env.example
├── .dockerignore
├── pom.xml
└── uploads
```

---

# Prerequisites

Install:

- Docker
- Docker Compose

No need to install:

- Java
- Maven
- PostgreSQL

Everything runs inside Docker.

---

# Setup

## 1 Clone Repository

```bash
git clone https://github.com/alokjha0/Document-Approval-System.git

cd Document-Approval-System
```

---

## 2 Create Environment File

Copy

```
.env.example
```

to

```
.env
```

Fill your PostgreSQL credentials.

---

## 3 Build Image

```bash
docker build -t das-app:v1 .
```

---

## 4 Start Application

```bash
docker compose up -d
```

---

Application

```
http://localhost:8080
```

---

PostgreSQL

```
localhost:5433
```

(Default host mapping)

---

# Database Backup & Restore

The project supports PostgreSQL backup and restore using

```
pg_dump
```

and

```
pg_restore
```

This allows migrating an existing local database into the Docker PostgreSQL container.

---

# Docker Architecture

```
Spring Boot

↓

Docker Image

↓

Docker Container

↓

Docker Network

↓

PostgreSQL Container

↓

Docker Volume
```

---

# Author

**Alok Ranjan Jha**

Full Stack Java Developer

Spring Boot | PostgreSQL | Docker | DevOps