# Data Source Management Backend (Minimal)

This is a minimal Spring Boot backend example for a data governance "Data Source Management" module.

## Tech Stack

- Spring Boot (REST API)
- Spring Data JPA (persistence)
- MySQL (database)
- Lombok (boilerplate reduction)

## Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+

## 1. Create Database

```sql
CREATE DATABASE governance_demo DEFAULT CHARACTER SET utf8mb4;
```

## 2. Configure DB Connection

Update values in `src/main/resources/application.yml` if needed:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## 3. Run

```bash
mvn spring-boot:run
```

## API Endpoints

Base path: `/api/data-sources`

- `POST /api/data-sources` Add data source
- `DELETE /api/data-sources/{id}` Delete data source
- `PUT /api/data-sources/{id}` Update data source
- `GET /api/data-sources` Get all data sources

### POST /api/data-sources request example

```json
{
  "name": "MySQL-Prod",
  "type": "DATABASE",
  "connectionUrl": "jdbc:mysql://localhost:3306/demo",
  "username": "root",
  "password": "root",
  "description": "Production MySQL"
}
```

### Success Response Example

```json
{
  "success": true,
  "message": "Data source created",
  "data": {
    "id": 1,
    "name": "MySQL-Prod",
    "type": "DATABASE",
    "connectionUrl": "jdbc:mysql://localhost:3306/demo",
    "username": "root",
    "description": "Production MySQL",
    "createdAt": "2026-03-01T10:00:00",
    "updatedAt": "2026-03-01T10:00:00"
  }
}
```

### Error Response Example (duplicate name)

```json
{
  "success": false,
  "message": "Data source name already exists: MySQL-Prod",
  "data": null
}
```

## React Integration Note

React can call these APIs with `fetch` or `axios` and bind data to a list/table view.
