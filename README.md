# Bank Management System

The Bank Management System is a web-based banking application built using Java, Spring Boot, Spring Security, Spring Data JPA, and MySQL. It provides a banking interface where users can manage their bank accounts, view balances, deposit and withdraw money, transfer funds to other accounts, and view transaction history. It also includes an admin dashboard for managing users, accounts, transactions, and audit logs.

## Features

### Customer Features

- Register a new customer account.
- Log in securely using a username and password.
- View personal account details.
- Check the current account balance.
- Deposit money into an account for development and testing.
- Withdraw money from an account.
- Transfer money to another bank account.
- View transaction history.
- Review transaction details, including references, amounts, dates, and statuses.
- Change account passwords.

### Admin Panel

- Log in using admin credentials.
- View dashboard statistics.
- View registered users.
- View bank accounts.
- View transaction records.
- View audit logs.
- Manage customer account statuses.
- Manage bank account statuses.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 | Programming language |
| Spring Boot 4.1.1 | Backend application framework |
| Spring MVC | Web controllers and request handling |
| Spring Security | Authentication, authorization, and security |
| Spring Data JPA | Database access |
| Hibernate | Object-relational mapping |
| MySQL | Relational database |
| HTML5 | Web page structure |
| CSS3 | Styling and responsive design |
| JavaScript | Client-side interactions |
| Maven | Dependency management and build |
| MySQL Workbench | Database management |

## Database Setup

This application uses MySQL to store customer information, bank accounts, transactions, and audit records.

### Step 1: Open MySQL Workbench

Open MySQL Workbench and connect to your local MySQL server, typically at `127.0.0.1:3306`.

### Step 2: Create the database

Run the following SQL command:

```sql
CREATE DATABASE IF NOT EXISTS bank_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

### Step 3: Create the application database user

Create a dedicated MySQL user for the application instead of using the root account.

```sql
CREATE USER IF NOT EXISTS 'bank_app'@'localhost'
IDENTIFIED BY 'Password@123';

GRANT SELECT, INSERT, UPDATE, DELETE,
      CREATE, ALTER, INDEX, DROP
ON bank_management.*
TO 'bank_app'@'localhost';
```

Replace the example password with your own strong password.

### Step 4: Verify the database

Run:

```sql
SHOW DATABASES;

SHOW GRANTS FOR 'bank_app'@'localhost';
```

## Application Configuration

Open:

```text
src/main/resources/application.properties
```

Configure the database connection using environment variables:

```properties
spring.application.name=bank-management

server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/bank_management
spring.datasource.username=bank_app
spring.datasource.password=Password@123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Session
server.servlet.session.timeout=15m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax

server.error.include-message=never
server.error.include-stacktrace=never

# Admin
bank.admin.username=bankadmin
bank.admin.password=password@123
bank.admin.email=admin@example.com
```

## Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

## Open the Application

Open your browser and visit:

```text
http://localhost:8080
```