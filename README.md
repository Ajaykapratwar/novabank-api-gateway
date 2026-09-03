# NovaBank API Gateway

The NovaBank API Gateway is the single entry point for the NovaBank backend services. It is built with Spring Boot and Spring Cloud Gateway and uses Eureka service discovery and client-side load balancing to forward requests to the appropriate service.

## Features

- Routes user, account, authentication, and transaction requests to backend services.
- Uses Eureka for service discovery.
- Supports load-balanced routing with Spring Cloud LoadBalancer.
- Validates JWT bearer tokens before forwarding protected requests.
- Allows authentication endpoints to be accessed without a bearer token.
- Exposes Spring Boot Actuator endpoints for health and monitoring.
- Can be run locally with Maven or packaged as a Docker image.

## Request routing

The gateway listens on port `8080` by default and exposes these routes:

| Request path | Destination service | Authentication |
| --- | --- | --- |
| `/api/auth/**` | `user-account-service` | Not required |
| `/api/users/**` | `user-account-service` | Bearer token required |
| `/api/accounts/**` | `user-account-service` | Bearer token required |
| `/api/transactions/**` | `transaction-service` | Bearer token required |

The destination services must register with Eureka using the service names shown above. Requests are forwarded using `lb://` service URIs.

## Prerequisites

- Java 21 or newer
- Maven 3.9 or newer, or the included Maven Wrapper
- A running Eureka server at `http://localhost:8761/eureka/`
- Registered instances of:
  - `user-account-service`
  - `transaction-service`

## Configuration

The gateway imports an optional `.env` file and reads configuration from `src/main/resources/application.properties`.

At minimum, configure a long, random JWT signing secret:

```properties
JWT_SECRET=replace_with_a_long_random_secret
```

For local development, create a `.env` file in the project root:

```dotenv
JWT_SECRET=replace_with_a_long_random_secret
```

Do not commit real secrets. The JWT secret must be long enough for the HMAC key algorithm used by the gateway; a 64-character secret or longer is recommended.

The default infrastructure settings are:

```properties
spring.application.name=api-gateway
server.port=8080
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

To use a different Eureka server or port, override the corresponding properties through environment variables, a profile-specific configuration file, or command-line arguments.

## Running locally

Start the Eureka server and the backend services first, then run the gateway with the Maven Wrapper.

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The gateway is available at:

```text
http://localhost:8080
```

Build and run the packaged application:

```bash
./mvnw clean package
java -jar target/apigateway-0.0.1-SNAPSHOT.jar
```

On Windows, use `.\mvnw.cmd clean package` for the build command.

## Authentication

Every request except paths containing `/api/auth` must include a JWT in the `Authorization` header:

```http
Authorization: Bearer <jwt>
```

Invalid, expired, or missing tokens receive an HTTP `401 Unauthorized` response. The gateway verifies the token signature with the configured `JWT_SECRET`; token creation and user authentication are handled by the user-account service.

## Monitoring

Spring Boot Actuator endpoints are exposed for monitoring. With the default configuration, the health endpoint is available at:

```text
http://localhost:8080/actuator/health
```

Because all Actuator endpoints are currently exposed, restrict `management.endpoints.web.exposure.include` in production and protect monitoring endpoints at the network or platform level.

## Docker

Build the image from the project root:

```bash
docker build -t novabank-api-gateway .
```

Run the container on Windows:

```powershell
docker run --rm -p 8080:8080 ^
  -e JWT_SECRET=replace_with_a_long_random_secret ^
  novabank-api-gateway
```

On macOS/Linux:

```bash
docker run --rm -p 8080:8080 \
  -e JWT_SECRET=replace_with_a_long_random_secret \
  novabank-api-gateway
```

The image exposes port `8080`. When running in Docker, configure the Eureka URL and any service hostnames so they are reachable from the container.

## Project structure

```text
src/
|-- main/java/com/example/apigateway/
|   |-- ApigatewayApplication.java
|   |-- exception/GlobalExceptionHandler.java
|   `-- security/
|       |-- AuthenticationFilter.java
|       |-- GatewaySecurityConfig.java
|       `-- JwtValidationUtils.java
|-- main/resources/application.properties
`-- test/java/com/example/apigateway/
    `-- ApigatewayApplicationTests.java
```

## Testing

Run the test suite with the Maven Wrapper:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

## Technology stack

- Java 21
- Spring Boot 4.1.1
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Cloud LoadBalancer
- Spring Security WebFlux
- JJWT 0.13.0
- Maven