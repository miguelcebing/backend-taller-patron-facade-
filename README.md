# Hotel Reservations - Backend (Spring Boot) - Facade Pattern

Hotel Reservation System built with the **structural Facade pattern** and a
**PostgreSQL** persistence layer. The REST client only knows `HotelFacade`; underneath it orchestrates
5 subsystems that **do not know each other**.

## Live (production)

- API: https://backend-production-ddcd.up.railway.app
- Health: https://backend-production-ddcd.up.railway.app/actuator/health
- Database: PostgreSQL (Railway plugin)

## Architecture

```
 [React frontend] --HTTP--> [HotelController] --> [HotelFacade] --+--> RoomService
        client              (translates HTTP)     (orchestrates)   +--> RateService
                                                                 +--> AdditionalServiceService
                                                                 +--> BillingService
                                                                 +--> AccessService
                                                                 +--> ReservationRepository (PostgreSQL)
                                                                 +--> InvoiceRepository     (PostgreSQL)
```

Golden rules:
1. `HotelController` only injects `HotelFacade`.
2. No `*Service` injects another `*Service`.
3. The facade orchestrates; business rules live in the subsystems.

## Stack

- Java 17, Spring Boot 3.3.x, Maven.
- **PostgreSQL + Spring Data JPA** for persistence (`reservations`, `contracted_services`,
  `invoices`, `invoice_lines`). Room inventory (15 rooms) is code-defined and availability is
  rebuilt from the persisted reservations on startup, so state **survives restarts**.
- `BigDecimal` (scale 2, HALF_UP) for money; `java.time.LocalDate` for dates.

## REST contract

| Method | Route | Facade |
|--------|-------|--------|
| POST | `/api/hotel/reservations` | `createReservation` |
| GET  | `/api/hotel/availability?checkIn&checkOut&roomType?` | `checkAvailability` |
| POST | `/api/hotel/reservations/{id}/services` | `addService` |
| PUT  | `/api/hotel/reservations/{id}/check-in` | `checkIn` |
| PUT  | `/api/hotel/reservations/{id}/check-out` | `checkOut` |
| GET  | `/api/hotel/reservations/{id}` | `getReservation` |

Health check: `GET /actuator/health`.
Status codes: 400 validation/malformed · 404 reservation or room not found · 409 no availability or invalid state.

## Domain rules

- 15 rooms: 6 singles (101-106), 6 doubles (201-206), 3 suites (301-303).
- Base price/night: single $80, double $120, suite $250.
- HIGH season (x1.5) when the check-in month is {12,1,6,7}; LOW otherwise.
- Services: spa $50 (one-time), transfer $30 (one-time), breakfast $15 x nights.
- Half-open ranges `[checkIn, checkOut)`: checking out on day X allows another check-in on day X.
- State machine: `CONFIRMED -> CHECKED_IN -> CHECKED_OUT`.
- Digital key (UUID) generated at check-in and revoked at check-out.

## Run locally

```bash
export PGHOST=localhost PGPORT=5432 PGDATABASE=railway PGUSER=postgres PGPASSWORD=postgres
mvn spring-boot:run          # http://localhost:8080
mvn test                     # tests (uses in-memory H2 for speed)
mvn -DskipTests clean package
java -jar target/*.jar
```

## Environment variables

- `PORT` - server port (default `8080`).
- `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` - PostgreSQL connection (provided by the
  Railway plugin).
- `CORS_ALLOWED_ORIGINS` - comma-separated allowed origins (default `http://localhost:5173`).

## Deploy (Railway)

The repo includes a multi-stage `Dockerfile` and `system.properties` (Java 17).

1. Railway -> New Project -> Deploy from GitHub repo.
2. Add a **PostgreSQL** plugin to the project (links `PG*` variables to the service).
3. Generate a public domain -> `https://<something>.up.railway.app`.
4. Variables: `CORS_ALLOWED_ORIGINS=https://<your-frontend>.vercel.app`.
5. Healthcheck path: `/actuator/health`.

## Tests

`RateServiceTest`, `RoomServiceTest` (overlaps + concurrency), `AdditionalServiceServiceTest`,
`HotelFacadeTest` (full flow over JPA), `HotelFacadeCompensationTest` (compensation),
`HotelControllerTest` (MockMvc) and `ArchitectureFacadeTest` (enforces the Facade pattern).

## Team

- Miguel Felipe Ceballos Ramírez — ID 931697
- Andrés Felipe Bolaños Cabrera — ID 922647
- Ian Santiago Ordoñez Rosero — ID 909560
