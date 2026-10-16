# Hotel Reservas - Backend (Spring Boot) - Patron Facade

Sistema de Reservas de Hotel implementado con el **patron estructural Facade**.
El cliente REST solo conoce `HotelFacade`; por debajo orquesta 5 subsistemas que **no se conocen entre si**.

## Arquitectura

```
 [Frontend React] --HTTP--> [HotelController] --> [HotelFacade] --+--> HabitacionService
        cliente            (solo traduce HTTP)    (orquesta)      +--> TarifaService
                                                                 +--> ServicioAdicionalService
                                                                 +--> FacturacionService
                                                                 +--> AccesoService
                                                                 +--> ReservaRepository (estado en memoria)
```

Reglas inviolables:
1. `HotelController` solo inyecta `HotelFacade`.
2. Ningun `*Service` inyecta a otro `*Service`.
3. La fachada orquesta; las reglas de negocio viven en los subsistemas.

## Stack

- Java 17, Spring Boot 3.3.x, Maven.
- Estado **en memoria** con colecciones concurrentes (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `AtomicInteger`) - **sin base de datos**.
- `BigDecimal` (escala 2, HALF_UP) para dinero; `java.time.LocalDate` para fechas.

## Endpoints (contrato REST)

| Metodo | Ruta | Fachada | Respuesta |
|--------|------|---------|-----------|
| POST | `/api/hotel/reservar` | `crearReserva` | 201 + `ReservaResponse` |
| GET  | `/api/hotel/disponibilidad?fechaEntrada&fechaSalida&tipo?` | `consultarDisponibilidad` | 200 + lista |
| POST | `/api/hotel/servicios/{reservaId}` | `agregarServicio` | 200 + `ReservaResponse` |
| PUT  | `/api/hotel/checkin/{reservaId}` | `realizarCheckIn` | 200 + `ReservaResponse` (llave digital) |
| PUT  | `/api/hotel/checkout/{reservaId}` | `realizarCheckOut` | 200 + `FacturaResponse` |
| GET  | `/api/hotel/reserva/{reservaId}` | `obtenerReserva` | 200 + `ReservaResponse` |

Health check: `GET /actuator/health`.

Codigos: 400 validacion/formato · 404 reserva o habitacion inexistente · 409 sin disponibilidad o estado invalido.

## Reglas de dominio

- 15 habitaciones: 6 sencillas (101-106), 6 dobles (201-206), 3 suites (301-303).
- Precios base/noche: sencilla $80, doble $120, suite $250.
- Temporada ALTA (x1.5) si el mes de entrada es {12,1,6,7}; BAJA en el resto.
- Servicios: spa $50 (unico), traslado $30 (unico), desayuno $15 x noches.
- Rangos semiabiertos `[entrada, salida)`: salir el dia X permite entrar el dia X.
- Maquina de estados: `CONFIRMADA -> CHECK_IN -> CHECK_OUT`.
- Llave digital UUID generada en check-in y revocada en check-out.

## Ejecutar en local

```bash
mvn spring-boot:run                 # http://localhost:8080
mvn test                            # pruebas
mvn -DskipTests clean package       # jar de produccion -> target/*.jar
java -jar target/*.jar
```

## Variables de entorno

- `PORT` - puerto del servidor (default `8080`).
- `CORS_ALLOWED_ORIGINS` - origenes permitidos separados por coma (default `http://localhost:5173`).

## Despliegue (Railway)

El repo incluye `Dockerfile` (build multi-etapa) y `system.properties` (Java 17).

1. Railway -> New Project -> Deploy from GitHub repo.
2. Generar dominio publico -> `https://<algo>.up.railway.app`.
3. Variables: `CORS_ALLOWED_ORIGINS=https://<tu-frontend>.vercel.app`.
4. Healthcheck path: `/actuator/health`.

## Pruebas

`TarifaServiceTest`, `HabitacionServiceTest` (solapamientos + concurrencia), `ServicioAdicionalServiceTest`,
`HotelFacadeTest` (flujo completo + compensacion), `HotelControllerTest` (MockMvc) y `ArquitecturaFacadeTest`
(verifica que el patron Facade se cumple).

## Equipo

- Miguel Ceballos (miguelcebing)
- Andres (Andres523-star)
- Santiago Rosero (ssantivr)

> Nota: el estado es **en memoria**; las reservas se pierden al reiniciar la aplicacion.
