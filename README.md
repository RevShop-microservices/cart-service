# Cart Service

The **Cart Service** is a Spring Boot microservice responsible for managing shopping carts and wishlists in the e-commerce platform. It provides a full-featured, MongoDB-backed cart system with real-time price calculations, item quantity management, and wishlist operations including seamless move-to-cart functionality.

---

## Features

- **Shopping Cart Management**: Full CRUD for a user's cart — add items, view cart, update quantities, remove individual items, and clear the entire cart.
- **Automatic Price Recalculation**: Recomputes the cart `totalPrice` on every add, update, or remove operation to keep the total accurate.
- **Wishlist Management**: Allows users to save products to a wishlist, remove them, and view the full wishlist with live product details fetched via Feign.
- **Move Wishlist Item to Cart**: Seamlessly moves a product from a user's wishlist directly into their active cart in a single operation.
- **JWT Security**: All endpoints are protected with stateless JWT authentication — user identity is extracted from the token, not passed as a parameter.
- **Cross-Service Communication**: Uses **OpenFeign** to call the Product Service for live product data (name, price, stock status) during cart/wishlist operations.
- **OpenAPI Docs**: Swagger UI available at `http://localhost:8084/swagger-ui.html`.
- **Distributed Tracing**: Integrated with Zipkin via Micrometer Brave for end-to-end request tracing.

---

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Core** | Spring Boot 3.3.2, Java 21 |
| **Data** | Spring Data MongoDB |
| **Security** | Spring Security, JJWT 0.11.5 |
| **HTTP Client** | Spring Cloud OpenFeign |
| **Service Discovery** | Spring Cloud Netflix Eureka Client |
| **Centralized Config** | Spring Cloud Config Client |
| **API Docs** | SpringDoc OpenAPI 2.5.0 |
| **Tracing** | Zipkin, Micrometer Brave |
| **Build & Coverage** | Maven Wrapper, Jacoco, SonarQube |

---

## Configuration

The service runs on port **`8084`** and fetches its configuration from the centralized Spring Cloud Config Server.

### Key Properties (`cart-service.properties` in Config Server):

| Property | Default Value | Description |
| :--- | :--- | :--- |
| `spring.data.mongodb.uri` | Atlas connection string | MongoDB URI for the `ecommerceDB` database |

### Environment Variables (Docker / K8s override):

```
MONGO_URI
EUREKA_SERVER_URL
CONFIG_SERVER_URL
```

---

## REST API Documentation

### 1. Cart Endpoints (`/api/cart`)

| Method | Path | Auth Required | Description |
| :--- | :--- | :---: | :--- |
| **POST** | `/api/cart/add` | ✓ | Add a product to the authenticated user's cart. Creates a new cart document if none exists. |
| **GET** | `/api/cart` | ✓ | Retrieve the full cart for the authenticated user including all items and total price. |
| **PUT** | `/api/cart/update` | ✓ | Update the quantity of a specific item in the cart. Total price is recalculated automatically. |
| **DELETE** | `/api/cart/remove` | ✓ | Remove a specific product from the cart by `productId`. |
| **DELETE** | `/api/cart/clear` | ✓ | Clear all items from the cart (used after a successful checkout). |

### 2. Wishlist Endpoints (`/api/wishlist`)

| Method | Path | Auth Required | Description |
| :--- | :--- | :---: | :--- |
| **POST** | `/api/wishlist/add` | ✓ | Add a product to the authenticated user's wishlist. |
| **DELETE** | `/api/wishlist/remove` | ✓ | Remove a specific product from the wishlist by `productId`. |
| **GET** | `/api/wishlist` | ✓ | Retrieve the full wishlist with live product details from the Product Service. |
| **POST** | `/api/wishlist/move-to-cart` | ✓ | Move a product from the wishlist into the active cart in a single operation. |

---

## Data Models

### `carts` collection (MongoDB)

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | Auto-generated MongoDB document ID |
| `userId` | `Long` | Reference to the owning user (from `users` MySQL table) |
| `items` | `List<CartItem>` | Embedded list of cart line items |
| `totalPrice` | `Double` | Computed sum of all item prices × quantities |

**Embedded `CartItem`:**

| Field | Type | Description |
| :--- | :--- | :--- |
| `productId` | `String` | Reference to the product (MongoDB `products._id`) |
| `name` | `String` | Product name (denormalized for display) |
| `price` | `Double` | Unit price at time of add |
| `quantity` | `Integer` | Number of units in the cart |

---

### `wishlists` collection (MongoDB)

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `String` | Auto-generated MongoDB document ID |
| `userId` | `Long` | Reference to the owning user |
| `productIds` | `List<String>` | Ordered list of saved product IDs |

---

## Build, Test, and Run

### 1. Compile and Package
```bash
./mvnw clean package
```
> Tests are skipped by default (`skipTests=true` in `pom.xml`).

### 2. Run Tests
```bash
./mvnw test -DskipTests=false
```

### 3. Run Locally
```bash
./mvnw spring-boot:run
```
> Ensure the following services are running before starting:
> - Config Server (`8888`)
> - Eureka Server (`8761`)
> - MongoDB (`27017`)
> - Product Service (`8082`) — required for Feign client calls in wishlist operations

### 4. Build Docker Image
```bash
docker build -t cart-service .
```
> The `Dockerfile` uses `eclipse-temurin:17-jre-alpine` and copies the pre-built JAR from `target/`.

### 5. Run via Docker Compose
```bash
docker compose up cart-service
```
> Refer to the root `docker-compose.yml` for the full environment variable configuration.

---

## CI/CD

The GitHub Actions workflow at [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) automatically:
1. Checks out the code.
2. Sets up JDK 17 (Temurin) with Maven dependency caching.
3. Builds and tests the service with `./mvnw clean package -DskipTests=false`.
4. Builds and pushes the Docker image to **GitHub Container Registry (GHCR)** on every push to `main`, `master`, or `develop`.
