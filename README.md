# Exam Management System (EMS)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)](https://www.postgresql.org/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.4-emerald.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-3178C6.svg)](https://www.typescriptlang.org/)
[![Vuetify](https://img.shields.io/badge/Vuetify-3.6-1867C0.svg)](https://vuetifyjs.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

A full-stack web platform designed to streamline academic exam workflows. The system automates document processing, anonymous (blind) grading, equitable task distribution among evaluators, and grade dispute resolution with strict Role-Based Access Control (RBAC).

---

## Key Features

- **Anonymous (Blind) Grading:** Student identifiers are decoupled from submissions. Questions/fragments are evaluated anonymously to eliminate grading bias.
- **Automated Task Distribution:** Distributes exam fragments equitably among available teachers, balancing workload across evaluation rounds.
- **Role-Based Access Control (RBAC):** Fine-grained permission model separating roles: `ADMIN`, `STAFF`, `TEACHER`, and `STUDENT`.
- **Exam & Review Life Cycle:** Automated transitions between drafting, distribution, active grading, result publication, and review periods.
- **Auditable Security:** Stateless authentication via signed JWTs, password hashing using BCrypt, and externalized environment configurations.
- **Automated Document Handling:** Server-side PDF parsing and storage for exam sheets and digitized answer papers.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL |
| **Frontend** | Vue 3, TypeScript, Vuetify, Pinia |
| **DevOps & Tools** | Docker, Docker Compose, Maven |
---
## Getting Started

### Prerequisites

- Docker and Docker Compose

---

### Quick Start

1. **Clone the repository:**
   ```bash
    git clone https://github.com/brunofontenele/ems.git
    cd ems
   ```

2. **Configure environment variables (optional):**
   ```bash
    cp .env.example .env
   ```

3. **Build and start all containers:**
   ```bash
    docker compose up --build
   ```

4. **Access the services:**

- Frontend UI: http://localhost:8081
- Backend API: http://localhost:8080
- PostgreSQL Database: localhost:7654 (db: emsdb, user: postgres)


## Default Seed Credentials

For evaluation and demonstration purposes, the database is pre-populated with accounts for each role:

| Role | Email / Username | Password | Scope / Context |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@teste.com` | `admin123` | System management & platform administration |
| **School Staff** | `staff_esl@teste.pt` | `staff123` | School operations & exam batch processing |
| **Teacher (Mathematics)** | `prof1@teste.pt` | `proff123` | Math A evaluator (grading & review handling) |
| **Teacher (Physics)** | `prof2@teste.pt` | `proff123` | Physics evaluator (grading & review handling) |
| **Student** | `aluno2@teste.pt` | `aluno123` | Published exam review, scores & notifications |

> **Note:** Additional accounts are available in seeders (`aluno1` to `aluno10` with password `aluno123`).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

