# Digital Wallet Ledger Service

A Java-based backend service for managing digital wallets and recording financial transactions through an immutable ledger model.

## Overview

Digital Wallet Ledger Service is a portfolio project demonstrating backend engineering practices for financial applications, including wallet management, ledger entries, transaction processing, and idempotent transfer requests.

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven
- JUnit 5
- Testcontainers

## Key Features

- Wallet creation and retrieval
- Multi-currency wallet support
- Ledger-based balance calculation
- Debit and credit ledger entries
- Wallet ownership validation
- Transaction history
- Idempotency support for transfer requests
- Centralized exception handling
- JWT-based authentication infrastructure
- Audit logging and correlation ID support

> This is a portfolio project and is not intended for production financial operations without further security, reliability, and compliance reviews.

## Architecture

The application follows a layered architecture:

- **Controller:** REST API endpoints
- **Service:** Business rules and transaction orchestration
- **Repository:** Persistence and database queries
- **Domain:** Wallets, ledger transactions, and ledger entries
- **DTO:** API request and response models
- **Security:** Authentication and authorization
- **Exception Handling:** Consistent API error responses

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL
- Docker, for Testcontainers-based integration tests

## Configuration

Configure database credentials and application secrets through environment variables. Do not commit production credentials or JWT secrets.

Example environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/digital_wallet
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_local_secret
```

Ensure your Spring configuration maps these variables to the corresponding properties used by the application.

## Build and Test

Run the verification suite:

```bash
mvn clean verify
```

Integration tests using Testcontainers require Docker to be running.

## API Overview

Base path: `/api/v1`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/wallets?currency=USD` | Create a wallet |
| GET | `/wallets` | List the authenticated user's wallets |
| GET | `/wallets/{walletId}` | Retrieve a wallet |
| GET | `/wallets/{walletId}/transactions` | Retrieve wallet transaction history |
| POST | `/transfers` | Transfer funds between wallets |

Authenticated endpoints require the authentication mechanism configured by the application. Transfer requests use an `Idempotency-Key` header.

## Financial Model

Wallet balances are calculated from ledger entries. Transfers are represented by a debit entry in the source wallet and a credit entry in the destination wallet.

Ledger entries are intended to be append-only. Any balance-changing operation should be performed through a validated transaction workflow.

## Roadmap

- Expand PostgreSQL integration test coverage
- Add API documentation
- Add CI verification with GitHub Actions
- Improve operational monitoring and deployment documentation

## License

---
