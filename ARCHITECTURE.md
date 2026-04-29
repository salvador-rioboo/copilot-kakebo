# ARCHITECTURE.md

## Stack

- Java 21
- Spring Boot compatible with Java 21
- API first
- Embedded database
- Maven

## Backend

- Generates a REST API to interact with the application
- Expenses/income must have the option to be recurring or not
- The API must show expenses and income and be able to create, delete and update expenses

## Frontend

- Use Thymeleaf + Tailwind
- Thymeleaf must consume the REST API
- Styles must be minimalist
- Maven for familiarity

## Decisions

- Thymeleaf + Tailwind to facilitate frontend creation
- REST API to be able to change to another frontend in the future
- Embedded database for simplicity reasons
