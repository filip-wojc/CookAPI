# Cook API

## Introduction
Cook API is a comprehensive backend system built with Java Spring Boot for managing cooking recipes and user reviews. The primary purpose of this project is to **demonstrate and practice integration testing techniques using Testcontainers**, providing a real-world example of testing Spring Boot applications with containerized dependencies.

The API allows users to register, authenticate, create and manage recipes, upload images, and write reviews. All operations are thoroughly tested using integration tests that spin up actual PostgreSQL and Redis containers, ensuring reliable and realistic testing scenarios.

---

## Key Testing Features
This project showcases advanced integration testing practices:

- **Testcontainers Integration**: Full integration tests using real PostgreSQL and Redis containers
- **Security Testing**: Authentication and authorization testing with custom test configurations  
- **File Upload Testing**: Multipart form data handling with mocked Cloudinary service
- **Caching Testing**: Redis cache behavior validation and performance testing
- **Mock External Services**: Cloudinary image service mocking for isolated testing

---

## Core Features
- **User Authentication**: JWT-based registration and login system
- **Recipe Management**: Full CRUD operations for recipes with image support
- **Review System**: Users can rate and review recipes
- **Image Handling**: Recipe images stored via Cloudinary integration
- **Caching**: Redis-powered caching for improved performance
- **Pagination & Sorting**: Flexible recipe browsing with multiple sorting options
- **Authorization**: Role-based access control and ownership validation

---

## Technologies Used
- **Java 21** - Programming language
- **Spring Boot** - Backend framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database interactions
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Cloudinary** - Image storage service
- **JWT** - Token-based authentication
- **JUnit** - Testing framework
- **Testcontainers** - Integration testing with containerized dependencies
- **MockMvc** - Web layer testing
- **AssertJ** - Fluent assertions

---

## API Endpoints

### Authentication Controller (`/api/auth`)
- `POST /register` - User registration
- `POST /login` - User login
- `POST /refresh-token` - JWT token refresh

### Recipe Controller (`/api/recipe`)
- `GET /` - Get paginated recipes with sorting
- `GET /{id}` - Get recipe by ID
- `POST /` - Create new recipe (with image upload)
- `PUT /{id}` - Update recipe (with image upload)
- `DELETE /{id}` - Delete recipe

### Review Controller (`/api/review`)
- `POST /recipe/{recipeId}` - Add review to recipe
- `GET /recipe/{recipeId}` - Get paginated reviews for recipe
- `GET /{reviewId}` - Get review by ID
- `DELETE /{reviewId}` - Delete review

---

## Testing Architecture

The project implements comprehensive integration testing using:

### Testcontainers Setup
```java
@Container
@ServiceConnection
static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");

@Container  
@ServiceConnection
static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
        .withExposedPorts(6379);
```

### Test Configuration
- **TestSecurityConfig**: Mock authentication for testing different user scenarios
- **TestCloudinaryConfig**: Mocked image upload service to avoid external dependencies
- **Transaction Management**: `@Transactional` and `@Rollback` for clean test isolation

### Test Coverage
- **Recipe Management**: All CRUD operations with authorization testing
- **Review System**: Complete review lifecycle with user permission validation
- **Caching Behavior**: Cache hit/miss scenarios and performance validation
- **Security**: Authentication, authorization, and forbidden access testing
- **Validation**: Input validation and error response testing
- **Mapping**: Object mapping tests

---

## How to Run

### Prerequisites
- Java 21
- Docker (for Testcontainers, Redis and PostgreSQL)
- Cloudinary account

### Running the Application
1. Clone this repository:
 
2. Set up environment variables:
   ```bash
   export CLOUDINARY_APIKEY=your_cloudinary_apikey
   export CLOUDINARY_APISECRET=your_cloudinary_apisecret
   export CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
   export DB_PASSWORD=your_postgresql_password
   export DB_USER=your_postgresql_username
   ```

3. Add .env file with DB_USER and DB_PASSWORD variables (for docker-compose.yml file)

4. Run docker-compose:
    ```bash
    docker compose up -d
   ```

5. Run the application.

### Running Tests
Before running the tests, remember to have the Docker engine running.

The tests will automatically:
- Start PostgreSQL and Redis containers
- Run all integration tests
- Clean up containers after completion

---

## License
This project is available for use under the MIT License.

---

*This project serves as a practical example of implementing robust integration testing in Spring Boot applications using Testcontainers and modern testing practices.*
