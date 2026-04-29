# ARCHITECTURE.md

## Stack

- Java 25
- Spring boot compatible con Java 25
- Api first
- Base de datos embebida
- maven

## Backend

- genera una api rest para interactuar con la aplicación
- Los gastos/ingresos deben tener la opción de ser recurrentes o no
- la api debe mostrar gastos e ingresos y poder crear, borrar y actualizar los gastos

## Frontend

- usar Thymeleaf + tailwind
- Thymeleaf debe consumir la api rest
- los estilos deben ser minimalistas
- maven por familiaridad

## Decisiones

- Thymeleaf + tailwind para facilitar la creación del frontend
- Rest api para poder cambiar a otro frontend en el futuro
- Base de datos embebida por temas de sencillez
