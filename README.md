# WorkSphere — Cloud-Native Task Management Platform

WorkSphere is a full-stack task management application built with **React, Spring Boot, and MySQL**, containerized using Docker Compose and deployed on **AWS EC2**. It provides authenticated task management with a REST API and an Nginx reverse proxy.

## Features

- **User Authentication:** User registration and login using JWT-based authentication.
- **Task Management:** Create, view, update, and delete tasks.
- **Task Organization:** Filter tasks by status and search for specific tasks.
- **Persistent Storage:** MySQL database for storing application data.
- **Containerization:** Docker and Docker Compose for managing application services.
- **Reverse Proxy:** Nginx serves the React frontend and routes API requests to the Spring Boot backend.
- **Cloud Deployment:** Hosted on an AWS EC2 Linux instance.

## Tech Stack

| Component        | Technologies           |
| ---------------- | ---------------------- |
| Frontend         | React                  |
| Backend          | Java, Spring Boot      |
| API              | REST                   |
| Authentication   | JWT                    |
| Database         | MySQL 8.4              |
| Containerization | Docker, Docker Compose |
| Web Server       | Nginx                  |
| Cloud            | AWS EC2                |
| Version Control  | Git, GitHub            |

## Architecture

```text
User / Browser
      |
      v
   AWS EC2
      |
      v
   Nginx
      |
      +---- React Frontend
      |
      +---- /api/* requests
                  |
                  v
          Spring Boot Backend
                  |
                  v
               MySQL
```

## Prerequisites

- Git
- Docker Engine
- Docker Compose plugin

## Run Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/MOHAMMED-RIYAZ/WorkSphere.git
   cd WorkSphere
   ```

2. Create a `.env` file in the project root with the database configuration expected by `docker-compose.yml`:

   ```env
   MYSQL_DATABASE=worksphere
   MYSQL_USER=worksphere_app
   MYSQL_PASSWORD=replace_with_a_strong_password
   MYSQL_ROOT_PASSWORD=replace_with_a_different_strong_password
   ```

3. Start the application:

   ```bash
   docker compose up -d --build
   ```

4. Open the frontend in your browser:

   ```text
   http://localhost
   ```

   If your local Compose configuration maps Nginx to port `5173`, use `http://localhost:5173` instead.

## API Overview

The backend exposes REST endpoints for authentication and task management.

- Authentication: `/api/auth/register`, `/api/auth/login`
- Task management: `/api/tasks`

Use the application's authentication flow to obtain a token before accessing protected task endpoints.

## Deployment

The application has been deployed to an AWS EC2 Linux instance using Docker Compose. Nginx handles frontend delivery and forwards API requests to the backend container.

## Security Notes

- Keep `.env` files and credentials out of Git.
- Use strong, unique database passwords.
- Do not expose the MySQL database port publicly.
- Configure the EC2 security group to allow only the required inbound traffic.

## Author

**Mohammed Riyaz**

GitHub: https://github.com/MOHAMMED-RIYAZ
