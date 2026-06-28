# Feature Enrichment — Attempt 1

Generated: 2026-06-28

Each section is one LLM call (~5–8K tokens). The instruction tells the generator how all files in the feature interact and what contracts they must honour.

---

## Shared Backend: Authentication Core

**Name:** `shared-backend-auth`  
**Type:** SHARED  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/User.java` — MODEL layer - Defines the data structure for a User, including credentials and role, for persistence in the database.
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/Role.java` — MODEL layer - A standalone enum defining the possible authorization roles (ADMIN, CUSTOMER) for User entities.
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/UserRepository.java` — REPOSITORY layer - Provides data access methods for User entities, including findByEmail(String email): Optional<User> used by the authentication service.
- `backend/src/main/java/com/ouzacocktailbarkitchen/service/UserService.java` — SERVICE layer - Implements UserDetailsService for Spring Security via loadUserByUsername(String email) and handles new user registration logic.
- `backend/src/main/java/com/ouzacocktailbarkitchen/util/JwtUtil.java` — UTIL layer - Provides static methods for JWT operations: generateToken(UserDetails), validateToken(String, UserDetails), and extractEmail(String). Used by JwtAuthFilter and AuthController.

**Feature Instruction:**

## Feature: Shared Backend Authentication Core

This feature establishes the foundational components for user authentication and authorization within the Spring Boot application. It includes the User entity, its corresponding repository, a service for user management that integrates with Spring Security, and a utility for JSON Web Token (JWT) handling.

### 1. `Role.java` - User Role Enum

This file defines the possible roles a user can have.

**File path:** `backend/src/main/java/com/ouzacocktailbarkitchen/model/Role.java`

**Implementation:**
- Create a public enum named `Role`.
- Define two enum constants: `ADMIN` and `CUSTOMER`.

### 2. `User.java` - User Data Model

This JPA entity represents a user in the system and implements Spring Security's `UserDetails` interface to integrate with the authentication framework.

**File path:** `backend/src/main/java/com/ouzacocktailbarkitchen/model/User.java`

**Implementation:**
- Annotate the class with `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Entity`, and `@Table(name = "users")`.
- Implement the `org.springframework.security.core.userdetails.UserDetails` interface.
- **Fields:**
  - `id`: `java.util.UUID`. Annotate with `@Id` and `@GeneratedValue(strategy = GenerationType.UUID)`.
  - `name`: `String`. Annotate with `@Column(nullable = false)`.
  - `email`: `String`. Annotate with `@Column(unique = true, nullable = false)`.
  - `password`: `String`. Annotate with `@Column(nullable = false)`.
  - `role`: `Role`. Annotate with `@Enumerated(EnumType.STRING)` and `@Column(nullable = false)`.
- **`UserDetails` method implementations:**
  - `getAuthorities()`: Return a `List` containing a single `SimpleGrantedAuthority` with the user's role name (e.g., `List.of(new SimpleGrantedAuthority(role.name()))`).
  - `getUsername()`: Return the `email` field.
  - `isAccountNonExpired()`: Return `true`.
  - `isAccountNonLocked()`: Return `true`.
  - `isCredentialsNonExpired()`: Return `true`.
  - `isEnabled()`: Return `true`.

### 3. `UserRepository.java` - User Data Access Layer

This Spring Data JPA repository provides database operations for the `User` entity.

**File path:** `backend/src/main/java/com/ouzacocktailbarkitchen/repository/UserRepository.java`

**Implementation:**
- Create a public interface `UserRepository` that extends `JpaRepository<User, UUID>`.
- Define the following methods:
  - `Optional<User> findByEmail(String email);`
  - `boolean existsByEmail(String email);`

### 4. `JwtUtil.java` - JWT Handling Utility

This utility class provides methods to generate, validate, and extract information from JWTs. It will be used by the `authentication-api` and `shared-backend-config` features.

**File path:** `backend/src/main/java/com/ouzacocktailbarkitchen/util/JwtUtil.java`

**Implementation:**
- Annotate the class with `@Component`.
- Inject JWT secret and expiration from `application.properties` using `@Value`:
  - `private String secretKey;` (e.g., `${application.security.jwt.secret-key}`)
  - `private long jwtExpiration;` (e.g., `${application.security.jwt.expiration}`)
- **Public Methods:**
  - `public String extractEmail(String token)`:
    1. Extracts and returns the subject claim from the token. This is a convenience method that calls `extractClaim(token, Claims::getSubject)`.
  - `public String generateToken(UserDetails userDetails)`:
    1. Calls an overloaded `generateToken` method with an empty map of extra claims: `generateToken(new HashMap<>(), userDetails)`.
  - `public boolean validateToken(String token, UserDetails userDetails)`:
    1. Extract the email from the token using `extractEmail(token)`.
    2. Return `true` if the extracted email equals `userDetails.getUsername()` and the token is not expired (call `!isTokenExpired(token)`). Otherwise, return `false`.
- **Private Helper Methods:**
  - `private boolean isTokenExpired(String token)`: Extracts the expiration claim and checks if it's before the current time.
  - `private Claims extractAllClaims(String token)`: Parses the JWT using the secret key and returns the `Claims` body.
  - `private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)`: A generic method to extract a specific claim using a resolver function.
  - `private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails)`: The core token generation logic. It should build the token with the provided claims, set the subject to `userDetails.getUsername()`, set the issued-at and expiration dates, and sign it with the `secretKey` using the `HS256` algorithm.
  - `private Key getSignInKey()`: Decodes the `secretKey` (which should be Base64 encoded) and returns a `Key` object for signing.

### 5. `UserService.java` - User Business Logic

This service class implements `UserDetailsService` for Spring Security integration and handles user registration logic.

**File path:** `backend/src/main/java/com/ouzacocktailbarkitchen/service/UserService.java`

**Implementation:**
- Annotate the class with `@Service` and make it `final`.
- Inject `UserRepository` and `PasswordEncoder` via the constructor.
- **Implement `UserDetailsService`:**
  - `public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException`:
    1. Call `userRepository.findByEmail(email)`.
    2. If the user is not found, throw a `UsernameNotFoundException` with a message like "User not found with email: " + email.
    3. Otherwise, return the found `User` object.
- **Public Methods:**
  - `public User registerUser(RegisterRequest registerRequest)`:
    1. Check if a user with the given email already exists by calling `userRepository.existsByEmail(registerRequest.getEmail())`.
    2. If it exists, throw a `ResponseStatusException` with `HttpStatus.CONFLICT` and a message like "Email address is already in use.".
    3. Create a new `User` instance using the builder pattern.
    4. Set the user's `name` and `email` from the `registerRequest`.
    5. Set the `password` by encoding `registerRequest.getPassword()` with the injected `PasswordEncoder`.
    6. Set the `role` to `Role.CUSTOMER`.
    7. Save the new `User` object using `userRepository.save()`.
    8. Return the saved `User` object.

### Inter-Feature Wiring

- **`UserService`** will be injected into `AuthController` (from `authentication-api`) to handle user registration.
- The `loadUserByUsername` method in `UserService` will be used by Spring Security's `AuthenticationProvider`, which is configured in `SecurityConfig` (from `shared-backend-config`).
- **`JwtUtil`** will be injected into `AuthController` to generate tokens on successful login and into `JwtAuthFilter` (from `shared-backend-config`) to validate tokens on incoming requests.

---

## Shared Backend: Security & App Configuration

**Name:** `shared-backend-config`  
**Type:** SHARED  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/security/JwtAuthFilter.java` — CONFIG layer - A Spring Security filter that validates the 'Authorization: Bearer' token on every request and sets the authentication context if valid.
- `backend/src/main/java/com/ouzacocktailbarkitchen/config/SecurityConfig.java` — CONFIG layer - Defines the core security configuration, including public/private route authorization, CORS policy, and integrating the JwtAuthFilter into the filter chain.
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/SpaController.java` — CONTROLLER layer - A special @Controller that forwards all non-API routes to index.html, enabling React Router to handle frontend navigation.
- `backend/src/main/java/com/ouzacocktailbarkitchen/config/AdminInitializer.java` — CONFIG layer - A CommandLineRunner that seeds the database with an initial ADMIN user on application startup if no admin exists.
- `backend/src/main/java/com/ouzacocktailbarkitchen/config/DataSeeder.java` — CONFIG layer - A CommandLineRunner that populates the database with realistic sample menu items and categories for 'Ouza Cocktail Bar & Kitchen' on first launch.

**Feature Instruction:**

This feature establishes the core security, configuration, and data seeding for the Spring Boot backend. It includes JWT-based authentication filtering, role-based authorization rules, initial admin user creation, sample menu data seeding, and a controller to support the React single-page application's client-side routing.

### 1. Security Configuration (`SecurityConfig.java`)

This is the central class for Spring Security. It defines which endpoints are public, which require authentication, and integrates the JWT filter.

**`SecurityConfig.java`**
- **Annotations**: `@Configuration`, `@EnableWebSecurity`, `@RequiredArgsConstructor`
- **Fields**:
  - `private final JwtAuthFilter jwtAuthFilter;`
  - `private final UserService userService;`
- **Beans**:
  - **`securityFilterChain(HttpSecurity http)`**: This is the main configuration method.
    - **Return Type**: `SecurityFilterChain`
    - **Logic**:
      1. Disable CSRF: `http.csrf(AbstractHttpConfigurer::disable)`
      2. Configure CORS: `http.cors(cors -> cors.configurationSource(request -> { ... }))`. The configuration should permit all origins, methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`), and headers. Set `allowCredentials` to `true`.
      3. Configure session management to be stateless: `http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`
      4. Define authorization rules using `http.authorizeHttpRequests(auth -> auth...)`:
         - Permit all requests to `/api/v1/auth/**`, `/api/v1/menu/**`, `/api/v1/reservations`.
         - Permit all requests to SPA fallback routes: `/`, `/{path:[^\.]*}`, `/error`.
         - Require `ADMIN` role for `/api/v1/admin/**`.
         - Require authentication for any other request (`anyRequest().authenticated()`).
      5. Configure the authentication provider: `http.authenticationProvider(authenticationProvider())`.
      6. Add the `JwtAuthFilter` before the standard `UsernamePasswordAuthenticationFilter`: `http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`.
      7. Build and return the `http` object.
  - **`authenticationProvider()`**:
    - **Return Type**: `AuthenticationProvider`
    - **Logic**:
      1. Create a `DaoAuthenticationProvider` instance.
      2. Set the `UserDetailsService` to the injected `userService`.
      3. Set the `PasswordEncoder` by calling `passwordEncoder()`.
      4. Return the provider.
  - **`authenticationManager(AuthenticationConfiguration config)`**:
    - **Return Type**: `AuthenticationManager`
    - **Logic**: Return `config.getAuthenticationManager()`.
  - **`passwordEncoder()`**:
    - **Return Type**: `PasswordEncoder`
    - **Logic**: Return a new `BCryptPasswordEncoder` instance.

### 2. JWT Authentication Filter (`JwtAuthFilter.java`)

This filter intercepts every incoming request to validate the JWT and set the user's authentication context.

**`JwtAuthFilter.java`**
- **Annotations**: `@Component`, `@RequiredArgsConstructor`
- **Extends**: `OncePerRequestFilter`
- **Fields**:
  - `private final JwtUtil jwtUtil;` (from `shared-backend-auth` feature)
  - `private final UserService userService;` (from `shared-backend-auth` feature)
- **Methods**:
  - **`doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`**: 
    - **Return Type**: `void`
    - **Logic**:
      1. Get the `Authorization` header from the `request`.
      2. If the header is `null` or does not start with `"Bearer "`, call `filterChain.doFilter(request, response)` and return immediately.
      3. Extract the JWT by taking the substring after `"Bearer "`.
      4. Extract the user's email from the JWT using `jwtUtil.extractEmail(jwt)`.
      5. If the email is not `null` and `SecurityContextHolder.getContext().getAuthentication()` is `null` (meaning the user is not yet authenticated):
         a. Load `UserDetails` using `userService.loadUserByUsername(email)`.
         b. Validate the token against the loaded `UserDetails` using `jwtUtil.validateToken(jwt, userDetails)`.
         c. If the token is valid, create a `UsernamePasswordAuthenticationToken` with `userDetails`, `null` credentials, and `userDetails.getAuthorities()`.
         d. Set the request details on the token: `token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))`.
         e. Set this token in the security context: `SecurityContextHolder.getContext().setAuthentication(token)`.
      6. Call `filterChain.doFilter(request, response)` to continue the filter chain.

### 3. Initial Admin User Creation (`AdminInitializer.java`)

This component runs on startup to ensure at least one admin user exists, configured via environment variables.

**`AdminInitializer.java`**
- **Annotations**: `@Component`, `@RequiredArgsConstructor`, `@Order(1)`
- **Implements**: `CommandLineRunner`
- **Fields**:
  - `private final UserRepository userRepository;` (from `shared-backend-auth`)
  - `private final PasswordEncoder passwordEncoder;`
  - `@Value("${app.admin.email:admin@ouzabar.com}") private String adminEmail;`
  - `@Value("${app.admin.password:admin123}") private String adminPassword;`
  - `@Value("${app.admin.name:Admin User}") private String adminName;`
- **Methods**:
  - **`run(String... args)`**:
    - **Return Type**: `void`
    - **Logic**:
      1. Check if a user with `adminEmail` exists using `userRepository.existsByEmail(adminEmail)`.
      2. If the user does not exist:
         a. Create a new `User` object.
         b. Set its `name` to `adminName`.
         c. Set its `email` to `adminEmail`.
         d. Set its `password` to the result of `passwordEncoder.encode(adminPassword)`.
         e. Set its `role` to `Role.ADMIN`.
         f. Save the user using `userRepository.save(adminUser)`.
         g. Log an informational message: `"Created initial admin user with email: {}", adminEmail`.

### 4. Database Seeding (`DataSeeder.java`)

This component populates the database with realistic sample menu data for Ouza Cocktail Bar & Kitchen on the first run.

**`DataSeeder.java`**
- **Annotations**: `@Component`, `@RequiredArgsConstructor`, `@Order(2)`
- **Implements**: `CommandLineRunner`
- **Fields**:
  - `private final MenuItemRepository menuItemRepository;` (from `menu-management-core`)
  - `private final MenuItemCategoryRepository menuItemCategoryRepository;` (from `menu-management-core`)
- **Methods**:
  - **`run(String... args)`**:
    - **Return Type**: `void`
    - **Logic**:
      1. Check if `menuItemCategoryRepository.count()` is greater than 0. If so, return early as data is already seeded.
      2. Create and save `MenuItemCategory` instances:
         - `Mezze & Appetizers`
         - `From the Grill`
         - `Main Courses`
         - `Desserts`
         - `Signature Cocktails`
         - `Classic Cocktails`
      3. Create and save `MenuItem` instances for each category. Use realistic names, descriptions, and prices (as `BigDecimal`). For example:
         - **Mezze**: Hummus (`"Creamy chickpea dip with tahini, lemon, and garlic."`, 8.00), Falafel (`"Crispy fried chickpea patties with herbs and spices."`, 9.50).
         - **From the Grill**: Chicken Souvlaki (`"Marinated chicken skewers grilled to perfection."`, 18.00), Lamb Kebab (`"Spiced ground lamb grilled on a skewer."`, 22.00).
         - **Main Courses**: Moussaka (`"Layered eggplant, spiced meat, and béchamel sauce."`, 24.00), Pan-Seared Sea Bass (`"With lemon-caper sauce and roasted vegetables."`, 28.00).
         - **Desserts**: Baklava (`"Rich, sweet pastry with layers of filo, nuts, and honey."`, 7.00).
         - **Signature Cocktails**: Aegean Sunset (`"Ouzo, aperol, passion fruit, and lime."`, 15.00), Spartan's Spirit (`"Metaxa brandy, honey syrup, and bitters."`, 16.00).
         - **Classic Cocktails**: Negroni (`"Gin, Campari, sweet vermouth."`, 14.00).
      4. Ensure each `MenuItem` is associated with its corresponding `MenuItemCategory` object before saving.
      5. Log an informational message: `"Database seeded with initial menu data."`

### 5. SPA Fallback Controller (`SpaController.java`)

This controller ensures that any non-API, non-static file request is forwarded to the React application's entry point, enabling client-side routing.

**`SpaController.java`**
- **Annotations**: `@Controller`
- **Methods**:
  - **`fallback()`**:
    - **Return Type**: `String`
    - **Annotation**: `@GetMapping(value = {"/", "/{path:[^\\.]*}"})`
    - **Logic**: Return the string `"forward:/index.html"`.

---

## Backend: Authentication API

**Name:** `authentication-api`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/AuthController.java` — CONTROLLER layer - Exposes public endpoints for user authentication, including POST /api/v1/auth/login and POST /api/v1/auth/register.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/AuthRequest.java` — DTO layer - A record defining the shape of the request body for the login endpoint, with validation annotations.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/AuthResponse.java` — DTO layer - A record defining the shape of the JSON response after a successful login.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/RegisterRequest.java` — DTO layer - A record defining the shape of the request body for the user registration endpoint, with validation annotations.

**Feature Instruction:**

This feature implements the backend authentication endpoints for user registration and login. It consists of a controller and several Data Transfer Objects (DTOs).

### 1. Data Transfer Objects (DTOs)

Create the following record classes in the `backend/src/main/java/com/ouzacocktailbarkitchen/dto/` package. These are simple data carriers with validation annotations.

**`AuthRequest.java`**
- A `record` to model the login request body.
- Fields:
  - `String email`: Add `@NotBlank` and `@Email` validation annotations.
  - `String password`: Add `@NotBlank` validation annotation.

**`RegisterRequest.java`**
- A `record` to model the user registration request body.
- Fields:
  - `String name`: Add `@NotBlank` validation annotation.
  - `String email`: Add `@NotBlank` and `@Email` validation annotations.
  - `String password`: Add `@NotBlank` and `@Size(min = 8, message = "Password must be at least 8 characters long")` validation annotations.

**`AuthResponse.java`**
- A `record` to model the successful authentication response.
- Fields:
  - `String token`: The JWT.
  - `String role`: The user's role (e.g., "ROLE_CUSTOMER", "ROLE_ADMIN").
  - `long expiresAt`: The token's expiration timestamp in epoch milliseconds.

### 2. Authentication Controller (`AuthController.java`)

Create this class in `backend/src/main/java/com/ouzacocktailbarkitchen/controller/`.

**Class-level Annotations:**
- `@RestController`
- `@RequestMapping("/api/v1/auth")`
- `@RequiredArgsConstructor` (or use constructor injection).

**Dependencies (injected via constructor):**
- `private final UserService userService;` (from `shared-backend-auth` feature)
- `private final JwtUtil jwtUtil;` (from `shared-backend-auth` feature)
- `private final AuthenticationManager authenticationManager;` (from Spring Security, get it from `AuthenticationConfiguration`)

**Endpoint Implementations:**

**A. `login(AuthRequest authRequest)`**
- **Signature**: `public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest)`
- **Mapping**: `@PostMapping("/login")`
- **Logic**:
  1.  Wrap the logic in a `try-catch` block to handle `BadCredentialsException`.
  2.  Authenticate the user by calling `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()))`.
  3.  If authentication succeeds, load the user details: `UserDetails userDetails = userService.loadUserByUsername(authRequest.email());`
  4.  Generate a JWT: `String token = jwtUtil.generateToken(userDetails);`
  5.  Extract the user's role. The role is the first authority in the `userDetails.getAuthorities()` collection. Get its string representation (e.g., `userDetails.getAuthorities().iterator().next().getAuthority()`).
  6.  Calculate the expiration timestamp. Define a constant for token validity (e.g., `JWT_EXPIRATION_MS = 1000 * 60 * 60 * 24` for 24 hours). The expiration timestamp will be `System.currentTimeMillis() + JWT_EXPIRATION_MS`.
  7.  Create a new `AuthResponse` object with the token, role, and calculated expiration timestamp.
  8.  Return `ResponseEntity.ok(authResponse)`.
- **Error Handling**:
  - In the `catch` block for `BadCredentialsException`, return `ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")`.
  - Validation errors on `AuthRequest` will be handled by `GlobalExceptionHandler`, returning a 400 Bad Request.

**B. `register(RegisterRequest registerRequest)`**
- **Signature**: `public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest)`
- **Mapping**: `@PostMapping("/register")`
- **Logic**:
  1.  Wrap the logic in a `try-catch` block to handle potential exceptions from the service layer if the user already exists.
  2.  Call the user service to register the new user: `userService.registerUser(registerRequest);`. This method is defined in the `shared-backend-auth` feature and is responsible for checking for existing emails, encoding the password, and saving the user with the `CUSTOMER` role.
  3.  If registration is successful, return a success message. You can return a simple map: `return ResponseEntity.ok(Map.of("message", "User registered successfully!"));`.
- **Error Handling**:
  - The `userService.registerUser` method should throw an exception (e.g., `IllegalStateException` or a custom one like `UserAlreadyExistsException`) if the email is already in use. Catch this exception and return `ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));`.
  - Validation errors on `RegisterRequest` will be handled by `GlobalExceptionHandler`, returning a 400 Bad Request.

### Inter-feature Wiring Summary
- `AuthController` is the entry point for this feature.
- It injects and calls `UserService` and `JwtUtil` from the `shared-backend-auth` feature to handle user data operations and JWT generation.
- It uses the DTOs (`AuthRequest`, `AuthResponse`, `RegisterRequest`) defined within this feature for its API request and response bodies.

---

## Shared Backend: Error Handling

**Name:** `shared-backend-error-handling`  
**Type:** SHARED  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/exception/GlobalExceptionHandler.java` — EXCEPTION layer - A @ControllerAdvice class that centralizes exception handling, translating Java exceptions into structured JSON error responses.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/ErrorResponse.java` — DTO layer - Defines the standard JSON structure for all API error responses, used by the GlobalExceptionHandler.
- `backend/src/main/java/com/ouzacocktailbarkitchen/exception/ResourceNotFoundException.java` — EXCEPTION layer - A custom RuntimeException thrown by services when an entity lookup by ID fails, which is then handled by GlobalExceptionHandler to produce a 404 response.

**Feature Instruction:**

### Feature: Shared Backend Error Handling

This feature establishes a centralized, consistent error handling mechanism for the entire backend application. It introduces a standard JSON error response format, a custom exception for 'not found' scenarios, and a global exception handler to catch and format errors automatically.

--- 

### File: `ErrorResponse.java`

**Path:** `backend/src/main/java/com/ouzacocktailbarkitchen/dto/ErrorResponse.java`

This file defines the standard structure for all JSON error responses sent by the API. It is a simple Data Transfer Object (DTO).

**Implementation Details:**

1.  **Package:** `com.ouzacocktailbarkitchen.dto`
2.  **Class:** `public class ErrorResponse`
3.  **Annotations:** Use Lombok's `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor` for boilerplate code generation (getters, setters, constructors).
4.  **Fields:** Define the following private fields:
    *   `private LocalDateTime timestamp;` // The time the error occurred.
    *   `private int status;` // The HTTP status code.
    *   `private String error;` // The HTTP status message (e.g., "Not Found").
    *   `private String message;` // A developer-friendly error message.
    *   `private String path;` // The API path where the error occurred.

--- 

### File: `ResourceNotFoundException.java`

**Path:** `backend/src/main/java/com/ouzacocktailbarkitchen/exception/ResourceNotFoundException.java`

This is a custom runtime exception that should be thrown by service layer methods whenever a requested entity (e.g., a `MenuItem` by ID) cannot be found in the database. The `GlobalExceptionHandler` will specifically catch this exception to generate a 404 Not Found response.

**Implementation Details:**

1.  **Package:** `com.ouzacocktailbarkitchen.exception`
2.  **Class:** `public class ResourceNotFoundException extends RuntimeException`
3.  **Annotations:** Add `@ResponseStatus(HttpStatus.NOT_FOUND)` to the class. This provides metadata to Spring about the intended HTTP status.
4.  **Constructor:**
    *   `public ResourceNotFoundException(String message)`: This constructor should call `super(message);` to pass the error message to the parent `RuntimeException` class.

--- 

### File: `GlobalExceptionHandler.java`

**Path:** `backend/src/main/java/com/ouzacocktailbarkitchen/exception/GlobalExceptionHandler.java`

This is the central component of the feature. It's a Spring `@ControllerAdvice` that intercepts exceptions thrown from any controller, formats them using the `ErrorResponse` DTO, and returns a standardized JSON error response to the client.

**Implementation Details:**

1.  **Package:** `com.ouzacocktailbarkitchen.exception`
2.  **Class:** `public class GlobalExceptionHandler extends ResponseEntityExceptionHandler`
    *   Annotate the class with `@ControllerAdvice`.
    *   Extending `ResponseEntityExceptionHandler` provides default handling for many standard Spring MVC exceptions, which we can override.
3.  **Imports:**
    *   `com.ouzacocktailbarkitchen.dto.ErrorResponse`
    *   `java.time.LocalDateTime`
    *   `java.util.stream.Collectors`
    *   Spring Framework annotations and classes (`@ControllerAdvice`, `@ExceptionHandler`, `ResponseEntity`, `HttpStatus`, `WebRequest`, `MethodArgumentNotValidException`, etc.).

4.  **Method: `handleResourceNotFoundException`**
    *   **Signature:** `public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request)`
    *   **Annotation:** `@ExceptionHandler(ResourceNotFoundException.class)`
    *   **Logic:**
        1.  Create a new `ErrorResponse` object.
        2.  Populate its fields:
            *   `timestamp`: `LocalDateTime.now()`
            *   `status`: `HttpStatus.NOT_FOUND.value()` (which is 404)
            *   `error`: `HttpStatus.NOT_FOUND.getReasonPhrase()` (which is "Not Found")
            *   `message`: The message from the exception, `ex.getMessage()`.
            *   `path`: The request URI, obtained from `request.getDescription(false).replace("uri=", "")`.
        3.  Return a new `ResponseEntity` containing the `ErrorResponse` object and the `HttpStatus.NOT_FOUND` status.

5.  **Method: `handleMethodArgumentNotValid`**
    *   **Signature:** `@Override protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    *   **Purpose:** This method overrides the default handler to provide a custom response for validation failures (e.g., when a request body DTO with `@Valid` fails validation checks like `@NotNull`, `@Size`, etc.).
    *   **Logic:**
        1.  Extract all validation error messages from the exception.
            *   Use a stream on `ex.getBindingResult().getFieldErrors()`.
            *   Map each `FieldError` to a descriptive string like `error.getField() + ": " + error.getDefaultMessage()`.
            *   Collect these strings into a single comma-separated string using `Collectors.joining(", ")`.
        2.  Create a new `ErrorResponse` object.
        3.  Populate its fields:
            *   `timestamp`: `LocalDateTime.now()`
            *   `status`: `HttpStatus.BAD_REQUEST.value()` (which is 400)
            *   `error`: "Validation Failed"
            *   `message`: The combined validation error string from step 1.
            *   `path`: The request URI, from `request.getDescription(false).replace("uri=", "")`.
        4.  Return a new `ResponseEntity` containing the `ErrorResponse` object and the `HttpStatus.BAD_REQUEST` status.

### Inter-File Wiring

-   `GlobalExceptionHandler` instantiates and returns `ErrorResponse` objects in its handler methods.
-   `GlobalExceptionHandler` has a handler method specifically for `ResourceNotFoundException`.
-   Other features' service layers (e.g., `MenuService`, `ReservationService`) will throw `ResourceNotFoundException` when they fail to find an entity. This exception will propagate up to the controller and be intercepted by `GlobalExceptionHandler` without any direct dependency.

---

## Backend: Menu Management Core

**Name:** `menu-management-core`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/MenuItem.java` — MODEL layer - Defines the data structure for a MenuItem, including its name, price, category, and image, for persistence.
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/MenuItemCategory.java` — MODEL layer - Defines the data structure for a MenuItemCategory, allowing menu items to be grouped logically.
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/MenuItemRepository.java` — REPOSITORY layer - Provides data access methods for MenuItem entities, including custom queries like findByCategoryName(String).
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/MenuItemCategoryRepository.java` — REPOSITORY layer - Provides standard CRUD data access methods for MenuItemCategory entities.
- `backend/src/main/java/com/ouzacocktailbarkitchen/service/MenuService.java` — SERVICE layer - Implements business logic for menu operations. Public methods include getAllMenuItems(): List<MenuItemDto> for public access and CRUD methods like createMenuItem(CreateMenuItemRequest) for admin use.

**Feature Instruction:**

This feature implements the core backend logic for managing the restaurant's menu. It includes the data models for menu items and categories, the JPA repositories for database interaction, and the service layer containing the business logic.

### 1. Data Models

#### `MenuItemCategory.java`
This class is a JPA entity representing a category for menu items.
- Annotate with `@Entity`, `@Table(name = "menu_item_categories")`, `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
- **Fields:**
  - `id`: `UUID`. Annotate with `@Id` and `@GeneratedValue(strategy = GenerationType.AUTO)`.
  - `name`: `String`. Annotate with `@Column(nullable = false, unique = true)`.

#### `MenuItem.java`
This class is a JPA entity representing a single menu item.
- Annotate with `@Entity`, `@Table(name = "menu_items")`, `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
- **Fields:**
  - `id`: `UUID`. Annotate with `@Id` and `@GeneratedValue(strategy = GenerationType.AUTO)`.
  - `name`: `String`. Annotate with `@Column(nullable = false)`.
  - `description`: `String`. Annotate with `@Column(columnDefinition = "TEXT")`.
  - `price`: `BigDecimal`. Annotate with `@Column(nullable = false, precision = 10, scale = 2)`.
  - `imageUrl`: `String`.
  - `isAvailable`: `boolean`. Annotate with `@Column(nullable = false)`.
- **Relationships:**
  - `category`: `MenuItemCategory`. This is a many-to-one relationship.
    - Annotate with `@ManyToOne(fetch = FetchType.LAZY)`.
    - Annotate with `@JoinColumn(name = "category_id", nullable = false)`.

### 2. Repositories

#### `MenuItemCategoryRepository.java`
This is a Spring Data JPA repository for `MenuItemCategory` entities.
- It should be an interface that extends `JpaRepository<MenuItemCategory, UUID>`.
- No custom methods are required.

#### `MenuItemRepository.java`
This is a Spring Data JPA repository for `MenuItem` entities.
- It should be an interface that extends `JpaRepository<MenuItem, UUID>`.
- **Public Methods:**
  - `List<MenuItem> findByCategoryName(String categoryName)`: Finds all menu items belonging to a specific category name.
  - `List<MenuItem> findAllByIsAvailableTrue()`: Finds all menu items where `isAvailable` is true.

### 3. Service Layer

#### `MenuService.java`
This service contains the business logic for menu operations. It will be consumed by the controllers in the `menu-management-api` feature.
- Annotate the class with `@Service` and `@RequiredArgsConstructor` (from Lombok for constructor injection).
- **Dependencies (injected via constructor):**
  - `private final MenuItemRepository menuItemRepository;`
  - `private final MenuItemCategoryRepository menuItemCategoryRepository;`
- You will need to implement mapping logic from entities (`MenuItem`, `MenuItemCategory`) to DTOs (`MenuItemDto`, `MenuItemCategoryDto`). For the purpose of this instruction, assume the DTOs have fields that directly correspond to the entities.

- **Public Method Implementations:**

  - **`List<MenuItemDto> getAllMenuItems()`**
    1. Call `menuItemRepository.findAll()` to retrieve all `MenuItem` entities.
    2. Map each `MenuItem` entity to a `MenuItemDto`.
    3. Return the list of `MenuItemDto`s.

  - **`List<MenuItemDto> getAvailableMenuItems()`**
    1. Call `menuItemRepository.findAllByIsAvailableTrue()` to retrieve all available `MenuItem` entities.
    2. Map each `MenuItem` entity to a `MenuItemDto`.
    3. Return the list of `MenuItemDto`s.

  - **`List<MenuItemCategoryDto> getAllCategories()`**
    1. Call `menuItemCategoryRepository.findAll()` to retrieve all `MenuItemCategory` entities.
    2. Map each `MenuItemCategory` entity to a `MenuItemCategoryDto`.
    3. Return the list of `MenuItemCategoryDto`s.

  - **`MenuItemDto createMenuItem(CreateMenuItemRequest request)`**
    1. Fetch the `MenuItemCategory` using `menuItemCategoryRepository.findById(request.getCategoryId())`.
    2. If the category is not found, throw a `ResourceNotFoundException` with the message "Category not found with id: [categoryId]".
    3. Create a new `MenuItem` entity using the data from the `CreateMenuItemRequest` and the fetched `MenuItemCategory`.
    4. Save the new entity using `menuItemRepository.save(newMenuItem)`.
    5. Map the saved `MenuItem` entity to a `MenuItemDto` and return it.

  - **`MenuItemDto updateMenuItem(UUID id, UpdateMenuItemRequest request)`**
    1. Fetch the existing `MenuItem` using `menuItemRepository.findById(id)`.
    2. If the menu item is not found, throw a `ResourceNotFoundException` with the message "MenuItem not found with id: [id]".
    3. Update the properties of the existing `MenuItem` entity with the values from the `UpdateMenuItemRequest` (name, description, price, imageUrl, isAvailable).
    4. If `request.getCategoryId()` is not null and is different from the current category's ID, fetch the new `MenuItemCategory` using `menuItemCategoryRepository.findById(request.getCategoryId())`. If not found, throw `ResourceNotFoundException`. Set this new category on the menu item.
    5. Save the updated entity using `menuItemRepository.save(existingMenuItem)`.
    6. Map the updated `MenuItem` entity to a `MenuItemDto` and return it.

  - **`void deleteMenuItem(UUID id)`**
    1. Check if the menu item exists using `menuItemRepository.existsById(id)`.
    2. If it does not exist, throw a `ResourceNotFoundException` with the message "MenuItem not found with id: [id]".
    3. If it exists, call `menuItemRepository.deleteById(id)`.

---

## Backend: Menu Management API

**Name:** `menu-management-api`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/MenuController.java` — CONTROLLER layer - Exposes public, read-only endpoints for the menu, such as GET /api/v1/menu, for the frontend to display.
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/AdminMenuController.java` — CONTROLLER layer - Exposes ADMIN-only CRUD endpoints under /api/v1/admin/menu for managing menu items from the admin dashboard.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/MenuItemDto.java` — DTO layer - A record defining the public-facing JSON representation of a menu item, used in both public and admin API responses.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/CreateMenuItemRequest.java` — DTO layer - A record defining the request body for creating a new menu item, with validation annotations for incoming data.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/UpdateMenuItemRequest.java` — DTO layer - A record defining the request body for updating an existing menu item, with validation annotations.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/MenuItemCategoryDto.java` — DTO layer - A record defining the public JSON representation of a menu category.

**Feature Instruction:**

This feature implements the REST API for managing the restaurant's menu. It consists of two controllers: one for public, read-only access to the menu, and another for admin-only CRUD operations. The feature also defines the Data Transfer Objects (DTOs) used for API requests and responses.

### Inter-Feature Dependencies
- **`menu-management-core`**: The controllers in this feature will inject and delegate all business logic to `MenuService` from the `menu-management-core` feature.
- **`shared-backend-error-handling`**: The controllers rely on `GlobalExceptionHandler` to automatically handle exceptions, such as `ResourceNotFoundException` (returning HTTP 404) and `MethodArgumentNotValidException` (returning HTTP 400 with validation errors).
- **`shared-backend-config`**: The `SecurityConfig` is expected to protect all endpoints under `/api/v1/admin/**`, ensuring only authenticated users with the 'ADMIN' role can access `AdminMenuController` methods.

--- 

### DTO Layer

**1. `MenuItemDto.java`**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/dto/MenuItemDto.java`
- **Purpose**: A `record` representing a menu item in API responses.
- **Fields**:
  - `UUID id`: The unique identifier for the menu item.
  - `String name`: The name of the menu item.
  - `String description`: A description of the menu item.
  - `BigDecimal price`: The price of the menu item.
  - `String imageUrl`: A URL for the item's image.
  - `String categoryName`: The name of the category this item belongs to.
  - `boolean isAvailable`: The availability status of the item.

**2. `MenuItemCategoryDto.java`**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/dto/MenuItemCategoryDto.java`
- **Purpose**: A `record` representing a menu category.
- **Fields**:
  - `UUID id`: The unique identifier for the category.
  - `String name`: The name of the category.

**3. `CreateMenuItemRequest.java`**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/dto/CreateMenuItemRequest.java`
- **Purpose**: A `record` for the request body when creating a new menu item. It must include validation annotations.
- **Fields**:
  - `@NotBlank String name`
  - `String description`
  - `@NotNull @Positive BigDecimal price`
  - `@URL String imageUrl`
  - `@NotNull UUID categoryId`
  - `@NotNull Boolean isAvailable`

**4. `UpdateMenuItemRequest.java`**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/dto/UpdateMenuItemRequest.java`
- **Purpose**: A `record` for the request body when updating an existing menu item. It must include the same validation annotations as the creation request.
- **Fields**:
  - `@NotBlank String name`
  - `String description`
  - `@NotNull @Positive BigDecimal price`
  - `@URL String imageUrl`
  - `@NotNull UUID categoryId`
  - `@NotNull Boolean isAvailable`

--- 

### Controller Layer

**1. `MenuController.java` (Public API)**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/controller/MenuController.java`
- **Annotations**: `@RestController`, `@RequestMapping("/api/v1/menu")`
- **Dependencies**: Injects `MenuService` from the `menu-management-core` feature via constructor injection.
- **Methods**:
  - **`getPublicMenu()`**: `ResponseEntity<List<MenuItemDto>>`
    - **Endpoint**: `GET /api/v1/menu`
    - **Logic**:
      1. Call `menuService.getAvailableMenuItems()`.
      2. Return the resulting `List<MenuItemDto>` in a `ResponseEntity` with `HttpStatus.OK`.

  - **`getMenuCategories()`**: `ResponseEntity<List<MenuItemCategoryDto>>`
    - **Endpoint**: `GET /api/v1/menu/categories`
    - **Logic**:
      1. Call `menuService.getAllCategories()`.
      2. Return the resulting `List<MenuItemCategoryDto>` in a `ResponseEntity` with `HttpStatus.OK`.

**2. `AdminMenuController.java` (Admin API)**
- **File Path**: `backend/src/main/java/com/ouzacocktailbarkitchen/controller/AdminMenuController.java`
- **Annotations**: `@RestController`, `@RequestMapping("/api/v1/admin/menu")`
- **Dependencies**: Injects `MenuService` from the `menu-management-core` feature via constructor injection.
- **Methods**:
  - **`getAllMenuItemsAdmin()`**: `ResponseEntity<List<MenuItemDto>>`
    - **Endpoint**: `GET /api/v1/admin/menu`
    - **Logic**:
      1. Call `menuService.getAllMenuItems()`.
      2. Return the resulting `List<MenuItemDto>` in a `ResponseEntity` with `HttpStatus.OK`.

  - **`createMenuItem(CreateMenuItemRequest request)`**: `ResponseEntity<MenuItemDto>`
    - **Endpoint**: `POST /api/v1/admin/menu`
    - **Parameters**: `@Valid @RequestBody CreateMenuItemRequest request`
    - **Logic**:
      1. Call `menuService.createMenuItem(request)`.
      2. Return the created `MenuItemDto` in a `ResponseEntity` with `HttpStatus.CREATED`.
    - **Error Cases**: If validation fails, `GlobalExceptionHandler` will return HTTP 400.

  - **`updateMenuItem(UUID id, UpdateMenuItemRequest request)`**: `ResponseEntity<MenuItemDto>`
    - **Endpoint**: `PUT /api/v1/admin/menu/{id}`
    - **Parameters**: `@PathVariable UUID id`, `@Valid @RequestBody UpdateMenuItemRequest request`
    - **Logic**:
      1. Call `menuService.updateMenuItem(id, request)`.
      2. Return the updated `MenuItemDto` in a `ResponseEntity` with `HttpStatus.OK`.
    - **Error Cases**:
      - If `menuService` throws `ResourceNotFoundException`, `GlobalExceptionHandler` will return HTTP 404.
      - If validation fails, `GlobalExceptionHandler` will return HTTP 400.

  - **`deleteMenuItem(UUID id)`**: `ResponseEntity<Void>`
    - **Endpoint**: `DELETE /api/v1/admin/menu/{id}`
    - **Parameters**: `@PathVariable UUID id`
    - **Logic**:
      1. Call `menuService.deleteMenuItem(id)`.
      2. Return an empty `ResponseEntity` with `HttpStatus.NO_CONTENT`.
    - **Error Cases**: If `menuService` throws `ResourceNotFoundException`, `GlobalExceptionHandler` will return HTTP 404.

---

## Backend: Reservation System Core

**Name:** `reservation-system-core`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/Reservation.java` — MODEL layer - Defines the data structure for a table Reservation, including customer details, time, and status, for persistence.
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/ReservationStatus.java` — MODEL layer - A standalone enum defining the possible states of a Reservation (PENDING, CONFIRMED, CANCELLED, COMPLETED).
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/ReservationRepository.java` — REPOSITORY layer - Provides data access methods for Reservation entities, including custom queries for finding reservations by date.
- `backend/src/main/java/com/ouzacocktailbarkitchen/service/ReservationService.java` — SERVICE layer - Implements business logic for reservations. Public methods include createReservation(CreateReservationRequest) for customers and admin methods like updateReservationStatus(UUID, UpdateReservationStatusRequest).

**Feature Instruction:**

This feature implements the core backend logic for the reservation system of Ouza Cocktail Bar & Kitchen. It includes the data model for a reservation, the repository for database interactions, and the service layer for business logic. The API controllers that expose this functionality are in the `reservation-system-api` feature.

### 1. Data Models

First, define the data structures for reservations.

**`com.ouzacocktailbarkitchen.model.ReservationStatus.java`**

This is a simple enum to represent the lifecycle of a reservation.

- Create a `public enum` named `ReservationStatus`.
- Define the following enum constants: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.

**`com.ouzacocktailbarkitchen.model.Reservation.java`**

This is the JPA entity representing a single reservation.

- Annotate the class with `@Entity`, `@Table(name = "reservations")`, `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor` for Lombok boilerplate.
- Define the following fields with specified annotations:
    - `id`: `private UUID id;`
        - Annotations: `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`
    - `customerName`: `private String customerName;`
        - Annotations: `@NotBlank`, `@Column(nullable = false)`
    - `customerEmail`: `private String customerEmail;`
        - Annotations: `@NotBlank`, `@Email`, `@Column(nullable = false)`
    - `customerPhone`: `private String customerPhone;`
        - Annotations: `@NotBlank`, `@Column(nullable = false)`
    - `reservationTime`: `private LocalDateTime reservationTime;`
        - Annotations: `@NotNull`, `@Future` (from `jakarta.validation.constraints`), `@Column(nullable = false)`
    - `partySize`: `private int partySize;`
        - Annotations: `@Min(1)`, `@Column(nullable = false)`
    - `status`: `private ReservationStatus status;`
        - Annotations: `@NotNull`, `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)`
    - `specialRequests`: `private String specialRequests;`

### 2. Repository Layer

**`com.ouzacocktailbarkitchen.repository.ReservationRepository.java`**

This interface provides data access methods for the `Reservation` entity.

- Create a `public interface` named `ReservationRepository` that extends `JpaRepository<Reservation, UUID>`.
- Annotate the interface with `@Repository`.
- Define the following method signature. Spring Data JPA will automatically provide the implementation.
    - `List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);`

### 3. Service Layer

**`com.ouzacocktailbarkitchen.service.ReservationService.java`**

This class contains the business logic for managing reservations. It uses the `ReservationRepository` to interact with the database and maps entities to DTOs.

- Annotate the class with `@Service` and `@RequiredArgsConstructor`.
- Inject the repository: `private final ReservationRepository reservationRepository;`

**Public Methods:**

- **`public ReservationDto createReservation(CreateReservationRequest request)`**
    1.  Instantiate a new `Reservation` object.
    2.  Map all corresponding fields from the `request` object (`customerName`, `customerEmail`, `customerPhone`, `reservationTime`, `partySize`, `specialRequests`) to the new `Reservation` entity.
    3.  Set the initial status: `reservation.setStatus(ReservationStatus.PENDING);`
    4.  Save the entity to the database: `Reservation savedReservation = reservationRepository.save(reservation);`
    5.  Map the `savedReservation` entity to a `ReservationDto` object.
    6.  Return the resulting `ReservationDto`.

- **`public List<ReservationDto> getAllReservations()`**
    1.  Retrieve all reservations from the database: `List<Reservation> reservations = reservationRepository.findAll();`
    2.  Use a stream to map each `Reservation` entity in the list to a `ReservationDto`.
    3.  Collect the results into a `List<ReservationDto>`.
    4.  Return the list.

- **`public ReservationDto updateReservationStatus(UUID id, UpdateReservationStatusRequest request)`**
    1.  Find the reservation by its ID: `Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));`
        - Note: `ResourceNotFoundException` is imported from the `shared-backend-error-handling` feature.
    2.  Update the status of the found reservation: `reservation.setStatus(request.getStatus());`
    3.  Save the updated entity: `Reservation updatedReservation = reservationRepository.save(reservation);`
    4.  Map the `updatedReservation` to a `ReservationDto`.
    5.  Return the `ReservationDto`.

**DTO Mapping:**
Since there is no dedicated mapper component in this feature, implement the mapping logic directly within the service or as private helper methods. A `ReservationDto` should contain all fields from the `Reservation` entity (`id`, `customerName`, `customerEmail`, `customerPhone`, `reservationTime`, `partySize`, `status`, `specialRequests`).

Example private helper method for DTO conversion:
```

java
private ReservationDto convertToDto(Reservation reservation) {
    // Manual mapping from Reservation to ReservationDto
    ReservationDto dto = new ReservationDto();
    dto.setId(reservation.getId());
    dto.setCustomerName(reservation.getCustomerName());
    dto.setCustomerEmail(reservation.getCustomerEmail());
    dto.setCustomerPhone(reservation.getCustomerPhone());
    dto.setReservationTime(reservation.getReservationTime());
    dto.setPartySize(reservation.getPartySize());
    dto.setStatus(reservation.getStatus());
    dto.setSpecialRequests(reservation.getSpecialRequests());
    return dto;
}


```

---

## Backend: Reservation System API

**Name:** `reservation-system-api`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/ReservationController.java` — CONTROLLER layer - Exposes the public POST /api/v1/reservations endpoint for customers to book a table.
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/AdminReservationController.java` — CONTROLLER layer - Exposes ADMIN-only endpoints under /api/v1/admin/reservations for viewing and managing all table reservations.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/ReservationDto.java` — DTO layer - A record defining the public JSON representation of a reservation, used in API responses.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/CreateReservationRequest.java` — DTO layer - A record defining the request body for creating a new reservation, with validation annotations.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/UpdateReservationStatusRequest.java` — DTO layer - A record defining the request body for an admin to update a reservation's status.

**Feature Instruction:**

### Feature: Backend Reservation System API

This feature implements the REST API endpoints for managing table reservations. It provides a public endpoint for customers to create a reservation and a set of admin-only endpoints to view all reservations and update their status.

### 1. Data Transfer Objects (DTOs)

These are simple Java records used for API requests and responses. They should be placed in the `com.ouzacocktailbarkitchen.dto` package.

#### `CreateReservationRequest.java`
This record models the request body for creating a new reservation. It includes validation annotations that will be enforced by the controller.

- **File Type**: `record`
- **Fields**:
  - `String customerName`: Must not be blank. Use `@jakarta.validation.constraints.NotBlank`.
  - `String customerEmail`: Must not be blank and must be a valid email format. Use `@NotBlank` and `@jakarta.validation.constraints.Email`.
  - `String customerPhone`: Must not be blank. Use `@NotBlank`.
  - `java.time.LocalDateTime reservationTime`: Must not be null and must be in the future. Use `@jakarta.validation.constraints.NotNull` and `@jakarta.validation.constraints.Future`.
  - `int partySize`: Must not be null and must be at least 1. Use `@NotNull` and `@jakarta.validation.constraints.Min(1)`.
  - `String specialRequests`: Optional field, no validation needed.

#### `UpdateReservationStatusRequest.java`
This record models the request body for an admin updating a reservation's status.

- **File Type**: `record`
- **Fields**:
  - `String status`: The new status for the reservation. Add a `@NotBlank` annotation.

#### `ReservationDto.java`
This record represents a reservation in API responses.

- **File Type**: `record`
- **Fields**:
  - `java.util.UUID id`
  - `String customerName`
  - `java.time.LocalDateTime reservationTime`
  - `int partySize`
  - `String status`

### 2. Controllers

These classes expose the REST endpoints and handle incoming HTTP requests by delegating to the `ReservationService`.

#### `ReservationController.java`
This controller handles public reservation creation.

- **Package**: `com.ouzacocktailbarkitchen.controller`
- **Annotations**: `@RestController`, `@RequestMapping("/api/v1/reservations")`, `@org.springframework.validation.annotation.Validated`
- **Dependencies**:
  - Inject `com.ouzacocktailbarkitchen.service.ReservationService` via constructor injection.

- **Method: `makeReservation`**
  - **Signature**: `public org.springframework.http.ResponseEntity<com.ouzacocktailbarkitchen.dto.ReservationDto> makeReservation(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody CreateReservationRequest request)`
  - **Endpoint**: `POST /api/v1/reservations`
  - **Logic**:
    1. The `@Valid` annotation triggers validation on the `CreateReservationRequest` object. If validation fails, the `GlobalExceptionHandler` from the `shared-backend-error-handling` feature will automatically intercept the `MethodArgumentNotValidException` and return a 400 Bad Request response.
    2. Call `reservationService.createReservation(request)`.
    3. The service returns a `ReservationDto` instance.
    4. Return the `ReservationDto` wrapped in a `ResponseEntity` with an HTTP status of `201 CREATED`.

#### `AdminReservationController.java`
This controller provides admin-only endpoints for managing reservations. Security for the `/api/v1/admin/**` path is handled by `SecurityConfig` in the `shared-backend-config` feature.

- **Package**: `com.ouzacocktailbarkitchen.controller`
- **Annotations**: `@RestController`, `@RequestMapping("/api/v1/admin/reservations")`
- **Dependencies**:
  - Inject `com.ouzacocktailbarkitchen.service.ReservationService` via constructor injection.

- **Method: `getAllReservations`**
  - **Signature**: `public org.springframework.http.ResponseEntity<java.util.List<com.ouzacocktailbarkitchen.dto.ReservationDto>> getAllReservations()`
  - **Endpoint**: `GET /api/v1/admin/reservations`
  - **Logic**:
    1. Call `reservationService.getAllReservations()`.
    2. The service returns a `List<ReservationDto>`.
    3. Return the list wrapped in a `ResponseEntity` with an HTTP status of `200 OK`.

- **Method: `updateReservationStatus`**
  - **Signature**: `public org.springframework.http.ResponseEntity<com.ouzacocktailbarkitchen.dto.ReservationDto> updateReservationStatus(@org.springframework.web.bind.annotation.PathVariable("id") java.util.UUID id, @org.springframework.web.bind.annotation.RequestBody UpdateReservationStatusRequest request)`
  - **Endpoint**: `PUT /api/v1/admin/reservations/{id}/status`
  - **Logic**:
    1. Call `reservationService.updateReservationStatus(id, request)`.
    2. The service returns the updated `ReservationDto`.
    3. Return the DTO wrapped in a `ResponseEntity` with an HTTP status of `200 OK`.
  - **Error Handling**: If `reservationService` throws a `ResourceNotFoundException` (from `shared-backend-error-handling`), the `GlobalExceptionHandler` will catch it and return a 404 Not Found response.

### 3. Cross-Feature Interactions

- **`reservation-system-core`**: Both `ReservationController` and `AdminReservationController` depend on `ReservationService` from this feature. They will inject it and call the following methods:
  - `ReservationDto createReservation(CreateReservationRequest request)`
  - `List<ReservationDto> getAllReservations()`
  - `ReservationDto updateReservationStatus(UUID id, UpdateReservationStatusRequest request)`

- **`shared-backend-error-handling`**: This feature relies on `GlobalExceptionHandler` to handle exceptions. Specifically, it handles `MethodArgumentNotValidException` for DTO validation failures (returning 400) and `ResourceNotFoundException` for when a reservation ID is not found (returning 404).

---

## Backend: Order Management Core

**Name:** `order-management-core`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/Order.java` — MODEL layer - Defines the data structure for a customer Order, linking to OrderItems and tracking total amount and payment status.
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/OrderItem.java` — MODEL layer - Defines a line item within an Order, linking a specific MenuItem with a quantity and price.
- `backend/src/main/java/com/ouzacocktailbarkitchen/model/OrderStatus.java` — MODEL layer - A standalone enum defining the lifecycle states of an Order (e.g., PENDING_PAYMENT, RECEIVED, PREPARING).
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/OrderRepository.java` — REPOSITORY layer - Provides data access methods for Order entities, including finding orders by user or by Razorpay order ID.
- `backend/src/main/java/com/ouzacocktailbarkitchen/repository/OrderItemRepository.java` — REPOSITORY layer - Provides standard CRUD data access methods for OrderItem entities.
- `backend/src/main/java/com/ouzacocktailbarkitchen/service/OrderService.java` — SERVICE layer - Implements business logic for orders. Key methods are createOrder(CreateOrderRequest, String) which coordinates with PaymentService, and verifyPaymentAndUpdateStatus(PaymentVerificationRequest).

**Feature Instruction:**

This feature instruction covers the core backend logic for order management. It includes the data models, repositories, and the service layer responsible for creating orders, processing payments via Razorpay, and retrieving order information.

### 1. Data Models

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/model/OrderStatus.java`**
- This is a public enum defining the possible states of an order.
- **Enum values**: `PENDING_PAYMENT`, `RECEIVED`, `PREPARING`, `READY_FOR_PICKUP`, `COMPLETED`, `CANCELLED`.

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/model/OrderItem.java`**
- This class is a JPA entity representing a single item within an order.
- Annotate with `@Entity`, `@Table(name = "order_items")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Fields**:
    - `id` (UUID): The primary key. Annotate with `@Id` and `@GeneratedValue(strategy = GenerationType.UUID)`.
    - `order` (Order): A many-to-one relationship to the parent `Order`. Annotate with `@ManyToOne(fetch = FetchType.LAZY)` and `@JoinColumn(name = "order_id")`. To prevent serialization loops, add `@com.fasterxml.jackson.annotation.JsonBackReference`.
    - `menuItem` (MenuItem): A many-to-one relationship to the `MenuItem` being ordered. Annotate with `@ManyToOne` and `@JoinColumn(name = "menu_item_id")`.
    - `quantity` (int): The quantity of the menu item ordered.
    - `price` (BigDecimal): The price of the menu item at the time the order was placed.

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/model/Order.java`**
- This class is a JPA entity representing a customer's order.
- Annotate with `@Entity`, `@Table(name = "orders")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Fields**:
    - `id` (UUID): The primary key. Annotate with `@Id` and `@GeneratedValue(strategy = GenerationType.UUID)`.
    - `user` (User): A many-to-one relationship to the `User` who placed the order. Annotate with `@ManyToOne` and `@JoinColumn(name = "user_id", nullable = true)` to allow for guest orders.
    - `orderItems` (List<OrderItem>): A one-to-many relationship to the items in this order. Annotate with `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)`. To prevent serialization loops, add `@com.fasterxml.jackson.annotation.JsonManagedReference`.
    - `totalAmount` (BigDecimal): The total cost of the order.
    - `status` (OrderStatus): The current status of the order. Annotate with `@Enumerated(EnumType.STRING)`.
    - `createdAt` (LocalDateTime): The timestamp when the order was created. Annotate with `@org.hibernate.annotations.CreationTimestamp`.
    - `razorpayOrderId` (String): The order ID generated by Razorpay.
    - `razorpayPaymentId` (String): The payment ID from Razorpay after a successful transaction. This can be null initially.

### 2. Repositories

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/repository/OrderItemRepository.java`**
- This is a Spring Data JPA repository interface for `OrderItem` entities.
- Annotate with `@Repository`.
- It should extend `org.springframework.data.jpa.repository.JpaRepository<OrderItem, UUID>`.
- No custom methods are required.

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/repository/OrderRepository.java`**
- This is a Spring Data JPA repository interface for `Order` entities.
- Annotate with `@Repository`.
- It should extend `org.springframework.data.jpa.repository.JpaRepository<Order, UUID>`.
- **Public Methods**:
    - `List<Order> findByUserId(UUID userId);`
    - `java.util.Optional<Order> findByRazorpayOrderId(String razorpayOrderId);`

### 3. Service Layer

**File: `backend/src/main/java/com/ouzacocktailbarkitchen/service/OrderService.java`**
- This service contains the business logic for managing orders.
- Annotate with `@Service` and `@lombok.RequiredArgsConstructor`.
- Annotate the class with `@org.springframework.transaction.annotation.Transactional` to ensure data consistency.
- **Inject the following dependencies via constructor:**
    - `OrderRepository orderRepository`
    - `com.ouzacocktailbarkitchen.repository.MenuItemRepository menuItemRepository` (from `menu-management-core`)
    - `com.ouzacocktailbarkitchen.repository.UserRepository userRepository` (from `shared-backend-auth`)
    - `com.ouzacocktailbarkitchen.service.PaymentService paymentService` (from `payment-processing`)
    - `org.modelmapper.ModelMapper` (configure this as a bean in a config file if not already present).

- **Public Method Implementations**:

    - **`OrderResponse createOrder(CreateOrderRequest request, String userEmail)`**
        1.  Initialize a `User` variable to `null`.
        2.  If `userEmail` is not null, find the user using `userRepository.findByEmail(userEmail)`. If not found, throw a `com.ouzacocktailbarkitchen.exception.ResourceNotFoundException` with the message "User not found". Assign the found user to the variable.
        3.  Create a new `Order` instance and set its `user` property.
        4.  Initialize `totalAmount` to `BigDecimal.ZERO`.
        5.  Create a `java.util.ArrayList<OrderItem>` to hold the order items.
        6.  Iterate through each item in `request.getItems()`:
            a. Fetch the `MenuItem` using `menuItemRepository.findById(item.getMenuItemId())`. If not found, throw `ResourceNotFoundException` with a message like "Menu item not found".
            b. If `!menuItem.isAvailable()`, throw an `IllegalStateException` with a message indicating the item is unavailable.
            c. Create a new `OrderItem` instance. Set its `order` to the new `Order` object, `menuItem` to the fetched menu item, `quantity` from the request item, and `price` from `menuItem.getPrice()`.
            d. Add the new `OrderItem` to the list.
            e. Add the item's subtotal (`menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))`) to `totalAmount`.
        7.  Set the `orderItems` list and the final `totalAmount` on the `Order` object.
        8.  Set the order `status` to `OrderStatus.PENDING_PAYMENT`.
        9.  Call `paymentService.createRazorpayOrder(totalAmount)` to generate a Razorpay order ID. This method is from the `payment-processing` feature.
        10. Set the returned `razorpayOrderId` on the `Order` object.
        11. Save the `Order` entity using `orderRepository.save(order)`. Cascade persistence will save the associated `OrderItem`s.
        12. Map the saved `Order` entity to an `OrderResponse` DTO. This DTO should include fields like `id`, `razorpayOrderId`, `totalAmount`, `status`, `createdAt`, and a list of `OrderItemResponse` DTOs. It should also include the Razorpay API key, which you should retrieve from your application properties (`@Value("${razorpay.key.id}")`).
        13. Return the populated `OrderResponse` DTO.

    - **`Order verifyPaymentAndUpdateStatus(PaymentVerificationRequest request)`**
        1.  Call `paymentService.verifyPaymentSignature(request)`. This method is from the `payment-processing` feature.
        2.  If the result is `false`, throw a `RuntimeException` with the message "Payment verification failed. Signature mismatch."
        3.  If verification is successful, find the order using `orderRepository.findByRazorpayOrderId(request.getRazorpay_order_id())`. If not found, throw `ResourceNotFoundException`.
        4.  Update the order's `status` to `OrderStatus.RECEIVED`.
        5.  Set the `razorpayPaymentId` on the order from `request.getRazorpay_payment_id()`.
        6.  Save the updated order using `orderRepository.save(order)`.
        7.  Return the updated `Order` entity.

    - **`List<OrderResponse> getAllOrders()`**
        1.  Fetch all orders using `orderRepository.findAll()`.
        2.  Use a stream and the `ModelMapper` to map each `Order` entity to an `OrderResponse` DTO.
        3.  Return the resulting `List<OrderResponse>`.

    - **`List<OrderResponse> getOrdersForUser(String userEmail)`**
        1.  Find the user by email using `userRepository.findByEmail(userEmail)`. If not found, throw `ResourceNotFoundException`.
        2.  Fetch orders for the user using `orderRepository.findByUserId(user.getId())`.
        3.  Map the list of `Order` entities to a list of `OrderResponse` DTOs.
        4.  Return the list.

---

## Backend: Order Management API

**Name:** `order-management-api`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/OrderController.java` — CONTROLLER layer - Exposes customer-facing order endpoints, including POST /api/v1/orders for placing an order and GET /api/v1/orders/my-history for viewing past orders.
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/AdminOrderController.java` — CONTROLLER layer - Exposes ADMIN-only endpoints under /api/v1/admin/orders for viewing and updating the status of all customer orders.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/CreateOrderRequest.java` — DTO layer - A record defining the request body for creating a new order, containing a list of order items.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/OrderItemRequest.java` — DTO layer - A record defining the structure of a single item within a CreateOrderRequest, specifying the menu item ID and quantity.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/OrderResponse.java` — DTO layer - A record defining the JSON response when an order is created or retrieved, including the critical razorpayOrderId for the frontend.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/UpdateOrderStatusRequest.java` — DTO layer - A record defining the request body for an admin to update an order's status.

**Feature Instruction:**

### Feature: Order Management API

This feature implements the backend API endpoints for both customer and admin order management. It includes controllers for placing orders, viewing order history, and administrative functions like viewing all orders and updating their status. This feature relies heavily on the `OrderService` from the `order-management-core` feature to handle business logic.

### DTO (Data Transfer Object) Layer

These are simple Java records used for API request and response bodies. Implement them with the specified fields and validation annotations.

**1. `OrderItemRequest.java`**
- A `public record` representing a single item in an order request.
- **Fields:**
  - `UUID menuItemId`: Must be annotated with `@NotNull`.
  - `int quantity`: Must be annotated with `@Min(1)`.

**2. `CreateOrderRequest.java`**
- A `public record` for the customer's order creation request body.
- **Fields:**
  - `List<OrderItemRequest> items`: Must be annotated with `@NotEmpty`.

**3. `UpdateOrderStatusRequest.java`**
- A `public record` for the admin's order status update request body.
- **Fields:**
  - `String status`: Must be annotated with `@NotBlank`.

**4. `OrderResponse.java`**
- A `public record` used as the standard response for order-related endpoints.
- **Fields:**
  - `UUID orderId`
  - `String razorpayOrderId`
  - `BigDecimal totalAmount`
  - `String status`
  - `LocalDateTime createdAt`

### Controller Layer

Implement two controllers: one for customer-facing actions and one for admin-only actions.

**1. `OrderController.java`**
- **Annotations:** `@RestController`, `@RequestMapping("/api/v1/orders")`, `@RequiredArgsConstructor`
- **Dependency Injection:**
  - Inject `OrderService` via constructor: `private final OrderService orderService;`

- **Methods:**
  - **`public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest request, Principal principal)`**
    - **Annotation:** `@PostMapping`
    - **Logic:**
      1. Extract the authenticated user's email from the `Principal` object: `principal.getName()`.
      2. Call the `orderService.createOrder(request, userEmail)` method. This method is defined in the `order-management-core` feature and will handle the core logic of creating the order and getting a `razorpayOrderId`.
      3. The service method returns an `OrderResponse` DTO.
      4. Return the `OrderResponse` with an HTTP 201 Created status: `return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);`

  - **`public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal)`**
    - **Annotation:** `@GetMapping("/my-history")`
    - **Logic:**
      1. Extract the user's email from `principal.getName()`.
      2. Call `orderService.getOrdersForUser(userEmail)` to retrieve the order history for the current user.
      3. Return the `List<OrderResponse>` with an HTTP 200 OK status: `return ResponseEntity.ok(orders);`

**2. `AdminOrderController.java`**
- **Annotations:** `@RestController`, `@RequestMapping("/api/v1/admin/orders")`, `@RequiredArgsConstructor`
- **Dependency Injection:**
  - Inject `OrderService` via constructor: `private final OrderService orderService;`

- **Methods:**
  - **`public ResponseEntity<List<OrderResponse>> getAllOrders()`**
    - **Annotation:** `@GetMapping`
    - **Logic:**
      1. Call `orderService.getAllOrders()` to fetch all orders in the system.
      2. Return the `List<OrderResponse>` with an HTTP 200 OK status: `return ResponseEntity.ok(orders);`

  - **`public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest request)`**
    - **Annotation:** `@PutMapping("/{orderId}/status")`
    - **Logic:**
      1. Call `orderService.updateOrderStatus(orderId, request.status())`. Note: This method must be implemented in the `order-management-core` feature (see Cross-Feature Contracts below).
      2. The service method will return the updated `OrderResponse`.
      3. Return the updated `OrderResponse` with an HTTP 200 OK status: `return ResponseEntity.ok(updatedOrder);`
    - **Error Handling:** If `orderService.updateOrderStatus` throws a `ResourceNotFoundException` for an invalid `orderId`, the `GlobalExceptionHandler` will catch it and return an HTTP 404 Not Found response.

### Cross-Feature Contracts & Wiring

- **`OrderController` and `AdminOrderController`** both inject and use `OrderService` from the `order-management-core` feature.
- **Calls to `order-management-core.OrderService`:**
  - `createOrder(CreateOrderRequest request, String userEmail)`: Used by `OrderController.placeOrder`.
  - `getOrdersForUser(String userEmail)`: Used by `OrderController.getMyOrders`.
  - `getAllOrders()`: Used by `AdminOrderController.getAllOrders`.
  - `updateOrderStatus(UUID orderId, String newStatus)`: This method is required by `AdminOrderController.updateOrderStatus`. You must assume it exists on `OrderService` with the following contract:
    - **Signature:** `public OrderResponse updateOrderStatus(UUID orderId, String newStatus)`
    - **Logic:** Finds the `Order` by `orderId`. If not found, throws `ResourceNotFoundException`. It validates that `newStatus` is a valid `OrderStatus` enum value. It updates the order's status, saves it, and returns the updated entity mapped to an `OrderResponse`.

### Error Handling

- Invalid request bodies in any of the `POST` or `PUT` endpoints (e.g., empty item list, quantity less than 1, blank status) will trigger validation annotations. The `GlobalExceptionHandler` (from `shared-backend-error-handling`) will intercept the resulting `MethodArgumentNotValidException` and automatically return an HTTP 400 Bad Request response with validation error details.

---

## Backend: Payment Processing (Razorpay)

**Name:** `payment-processing`  
**Type:** BACKEND  
**Change required:** true

**Files in this feature:**
- `backend/src/main/java/com/ouzacocktailbarkitchen/service/PaymentService.java` — SERVICE layer - Encapsulates all interaction with the Razorpay API. Provides createRazorpayOrder(BigDecimal) and verifyPaymentSignature(PaymentVerificationRequest) methods for use by the OrderService.
- `backend/src/main/java/com/ouzacocktailbarkitchen/controller/PaymentController.java` — CONTROLLER layer - Exposes the POST /api/v1/payments/verify endpoint, which the frontend calls after a successful Razorpay transaction to confirm the payment on the backend.
- `backend/src/main/java/com/ouzacocktailbarkitchen/dto/PaymentVerificationRequest.java` — DTO layer - A record defining the request body sent from the frontend to the backend to verify a payment's authenticity with Razorpay.

**Feature Instruction:**

## Feature: Backend Payment Processing (Razorpay)

This feature integrates the Razorpay payment gateway to handle online order payments. It provides a service to create Razorpay orders and a controller endpoint for the frontend to verify payment success. The core logic involves creating a payment order on Razorpay, receiving payment details from the frontend after the user completes the transaction, verifying the payment signature with Razorpay, and finally updating the internal order status.

### Dependencies

Add the Razorpay Java SDK to the `pom.xml`:

```

xml
<dependency>
    <groupId>com.razorpay</groupId>
    <artifactId>razorpay-java</artifactId>
    <version>1.4.3</version>
</dependency>


```

### Configuration

Add your Razorpay API keys to `backend/src/main/resources/application.properties`. These will be injected into the `PaymentService`.

```

properties
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET


```

### File Implementations

Here is the detailed implementation plan for each file in the feature.

#### 1. `backend/src/main/java/com/ouzacocktailbarkitchen/dto/PaymentVerificationRequest.java`

This file defines a Data Transfer Object (DTO) for the payment verification request body sent from the frontend.

- **Class Definition**: Define this as a `public record` for immutability and conciseness.
- **Fields**: The record should have three `String` fields:
  - `String razorpayOrderId;`
  - `String razorpayPaymentId;`
  - `String razorpaySignature;`
- **Validation**: Annotate each field with `@jakarta.validation.constraints.NotBlank` to ensure they are present in the request.

```

java
package com.ouzacocktailbarkitchen.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentVerificationRequest(
    @NotBlank String razorpayOrderId,
    @NotBlank String razorpayPaymentId,
    @NotBlank String razorpaySignature
) {}


```

#### 2. `backend/src/main/java/com/ouzacocktailbarkitchen/service/PaymentService.java`

This service encapsulates all direct interactions with the Razorpay API.

- **Annotations**: Annotate the class with `@Service`.
- **Fields**:
  - `private final String razorpayKeyId;`: To store the Razorpay Key ID.
  - `private final String razorpayKeySecret;`: To store the Razorpay Key Secret.
  - `private final RazorpayClient razorpayClient;`: The Razorpay SDK client.
- **Constructor**:
  - The constructor should accept the key ID and secret, injected using `@Value("${razorpay.key.id}")` and `@Value("${razorpay.key.secret}")` respectively.
  - Inside the constructor, initialize the `razorpayClient` using `new RazorpayClient(razorpayKeyId, razorpayKeySecret)`.
  - The constructor should throw `RazorpayException`.
- **`createRazorpayOrder(BigDecimal amount)` method**:
  - **Signature**: `public String createRazorpayOrder(BigDecimal amount)`
  - **Logic**:
    1. Convert the `amount` from `BigDecimal` (in Rupees) to an `Integer` representing the smallest currency unit (paise). Do this by multiplying by 100: `amount.multiply(new BigDecimal("100")).intValue()`. 
    2. Create a `JSONObject` for the order request.
    3. Put the following key-value pairs into the `JSONObject`:
       - `"amount"`: The amount in paise.
       - `"currency"`: `"INR"`
       - `"receipt"`: A unique receipt ID, e.g., `"receipt_" + System.currentTimeMillis()`.
    4. Call `razorpayClient.orders.create(orderRequest)` to create the order on Razorpay.
    5. Extract the order ID from the returned `Order` object using `order.get("id")`.
    6. Return the order ID as a `String`.
  - **Error Handling**: Wrap the logic in a `try-catch` block for `RazorpayException`. In the catch block, log the error and throw a `RuntimeException` (e.g., `"Error creating Razorpay order"`).

- **`verifyPaymentSignature(PaymentVerificationRequest request)` method**:
  - **Signature**: `public boolean verifyPaymentSignature(PaymentVerificationRequest request)`
  - **Logic**:
    1. Create a `JSONObject` containing the payment details from the `request` DTO.
    2. Put the following key-value pairs:
       - `"razorpay_order_id"`: `request.razorpayOrderId()`
       - `"razorpay_payment_id"`: `request.razorpayPaymentId()`
       - `"razorpay_signature"`: `request.razorpaySignature()`
    3. Call the static utility method `com.razorpay.Utils.verifyPaymentSignature(attributes, this.razorpayKeySecret)`.
    4. Return the `boolean` result of this method call.
  - **Error Handling**: Wrap the logic in a `try-catch` block for `RazorpayException`. In the catch block, log the error and return `false`.

#### 3. `backend/src/main/java/com/ouzacocktailbarkitchen/controller/PaymentController.java`

This controller exposes an endpoint for the frontend to verify a payment after it has been completed on the Razorpay UI.

- **Annotations**: Annotate the class with `@RestController` and `@RequestMapping("/api/v1/payments")`.
- **Dependencies**: Inject `PaymentService` and `OrderService` (from the `order-management-core` feature) via the constructor.
- **`verifyPayment(PaymentVerificationRequest request)` method**:
  - **Signature**: `public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerificationRequest request)`
  - **Annotation**: `@PostMapping("/verify")`
  - **Logic**:
    1. Call `paymentService.verifyPaymentSignature(request)`.
    2. If the result is `false`, the signature is invalid. Return `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Payment verification failed: Invalid signature."))`.
    3. If the result is `true`, the signature is valid. Proceed to call `orderService.verifyPaymentAndUpdateStatus(request)`. This method is responsible for finding the corresponding order in the database and updating its status to `PAID`.
    4. After the `orderService` call completes successfully, return `ResponseEntity.ok(Map.of("status", "success"))`.
  - **Error Handling**: The `GlobalExceptionHandler` will catch exceptions from `orderService` (e.g., `ResourceNotFoundException` if the order ID doesn't exist) and return appropriate HTTP error responses.

### Inter-Feature Integration

- **`order-management-core` -> `payment-processing`**: The `OrderService` (from `order-management-core`) must inject `PaymentService`. When `OrderService.createOrder()` is called, it should first calculate the total order amount and then call `paymentService.createRazorpayOrder(totalAmount)` to get the `razorpayOrderId`. This ID is then saved with the `Order` entity and returned to the client in the `OrderResponse`.

- **`payment-processing` -> `order-management-core`**: As described above, `PaymentController` injects `OrderService`. After a successful payment signature verification, it calls `orderService.verifyPaymentAndUpdateStatus()` to delegate the business logic of updating the order's state in the database.

- **`frontend-order-flow` -> `payment-processing`**: The frontend, after receiving a successful response from the Razorpay checkout popup, will call the `POST /api/v1/payments/verify` endpoint with the `razorpay_order_id`, `razorpay_payment_id`, and `razorpay_signature` to finalize the order on the backend.

---

## Frontend: Core Infrastructure

**Name:** `frontend-core-infra`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/api/client.ts` — SERVICE layer - Exports a singleton Axios instance. Includes a request interceptor that reads 'token' from localStorage and adds the 'Authorization: Bearer' header to all outgoing API requests.
- `frontend/src/App.tsx` — PAGE layer - The main application entry point that sets up context providers (QueryClientProvider, AuthProvider) and defines all application routes using React Router.

**Feature Instruction:**

This feature sets up the core infrastructure for the frontend React application. It includes the configuration of a global API client with authentication handling and the main application component that defines routing and context providers.

### File: `frontend/src/api/client.ts`

This file configures and exports a singleton Axios instance for all API communication. This instance will automatically attach the user's authentication token to every request.

**Public Variables:**

-   `apiClient: AxiosInstance`
    -   Create an Axios instance using `axios.create()`.
    -   Configure it with the following options:
        -   `baseURL`: `'/api/v1'`
        -   `headers`: `{ 'Content-Type': 'application/json' }`
    -   Add a request interceptor to `apiClient` using `apiClient.interceptors.request.use()`.
    -   The interceptor logic should be as follows:
        1.  Retrieve the authentication token from local storage: `const token = localStorage.getItem('token');`
        2.  If a `token` exists, add it to the request's `Authorization` header: `config.headers.Authorization = `Bearer ${token}`;`
        3.  Return the `config` object.
    -   Export `apiClient` as the default export.

### File: `frontend/src/App.tsx`

This is the root component of the React application. It sets up all necessary context providers and defines the application's routing structure using `react-router-dom`.

**Component: `App()`**

-   **Return Type:** `JSX.Element`

-   **Logic:**
    1.  Instantiate a `QueryClient` from `@tanstack/react-query`.
    2.  The component's structure should be a hierarchy of providers wrapping the application's routes.
    3.  The provider hierarchy is as follows:
        -   `<QueryClientProvider client={queryClient}>`
            -   `<AuthProvider>` (imported from `@/context/AuthContext`)
                -   `<BrowserRouter>`
                    -   `<Routes>`
                        -   Define all application routes using the `<Route>` component.

-   **Routes to define inside `<Routes>`:**
    -   `<Route path="/" element={<HomePage />} />`
    -   `<Route path="/menu" element={<MenuPage />} />`
    -   `<Route path="/reservations" element={<ReservationPage />} />`
    -   `<Route path="/contact" element={<ContactPage />} />`
    -   `<Route path="/login" element={<LoginPage />} />`
    -   A protected route for the admin dashboard:
        ```

jsx
        <Route 
          path="/admin/dashboard" 
          element={
            <ProtectedRoute adminOnly={true}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        

```

-   **Imports:**
    -   `React` from `react`
    -   `BrowserRouter`, `Routes`, `Route` from `react-router-dom`
    -   `QueryClient`, `QueryClientProvider` from `@tanstack/react-query`
    -   `AuthProvider` from `@/context/AuthContext`
    -   `HomePage` from `@/pages/HomePage`
    -   `MenuPage` from `@/pages/MenuPage`
    -   `ReservationPage` from `@/pages/ReservationPage`
    -   `ContactPage` from `@/pages/ContactPage`
    -   `LoginPage` from `@/pages/LoginPage`
    -   `AdminDashboardPage` from `@/pages/admin/AdminDashboardPage`
    -   `ProtectedRoute` from `@/components/ProtectedRoute`

### Inter-file and Cross-feature Wiring

-   The `apiClient` instance exported from `frontend/src/api/client.ts` will be the standard mechanism for all other frontend services and hooks to communicate with the backend API.
-   `App.tsx` serves as the application's entry point, composing pages from various features (`frontend-core-layout`, `frontend-menu-display`, `frontend-reservation-booking`, `frontend-auth-ui`, `frontend-admin-portal`).
-   It uses `AuthProvider` and `ProtectedRoute` from the `frontend-auth-ui` feature to manage application-wide authentication state and protect access to admin-only routes.

---

## Frontend: Core Layout & Pages

**Name:** `frontend-core-layout`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/components/Layout.tsx` — COMPONENT layer - A wrapper component for all public pages that renders the shared Header, the page-specific children, and the shared Footer.
- `frontend/src/components/Header.tsx` — COMPONENT layer - Renders the responsive, sticky top navigation bar with the business name 'Ouza', navigation links, and primary CTAs. Uses deep blue (bg-[#0A2342]) and gold accent (text-[#D4A843]) colors.
- `frontend/src/components/Footer.tsx` — COMPONENT layer - Renders the site footer with three columns: business address/phone, opening hours, and an embedded Google Maps iframe pointing to the Baner, Pune location.
- `frontend/src/pages/HomePage.tsx` — PAGE layer - The main landing page. Renders sections: a full-bleed hero image, an 'About Ouza' summary, a grid of featured menu items, a customer testimonials carousel, and clear CTAs to 'Book a Table' and 'Order Online'.
- `frontend/src/pages/ContactPage.tsx` — PAGE layer - Displays the restaurant's contact details (address, phone, email), opening hours, and a large embedded Google Map for the Baner, Pune location.

**Feature Instruction:**

This feature instruction covers the core layout and public-facing pages for the 'Ouza Cocktail Bar & Kitchen' website. It establishes the main visual structure and provides the primary landing and contact pages.

## Design Tokens
- **Navbar**: `bg-[#0A2342]` (deep blue) with `text-white` and `text-[#D4A843]` (gold accent) for the brand name.
- **Footer**: `bg-[#0A2342]` (deep blue) with `text-gray-300`.
- **Primary CTA**: `bg-[#D4A843]` `hover:bg-[#c0953a]` `text-black` font-semibold rounded-md `px-6 py-2` transition-colors.
- **Secondary CTA**: `border border-[#D4A843]` `text-[#D4A843]` `hover:bg-[#D4A843]` `hover:text-black` font-semibold rounded-md `px-6 py-2` transition-colors.
- **Brand Text Accent**: `text-[#D4A843]`.
- **Section Backgrounds**: `bg-white` (odd sections) / `bg-gray-50` (even sections).
- **Section Container**: `<section className="py-20 px-4"><div className="max-w-7xl mx-auto">...</div></section>`
- **Hero H1**: `text-4xl md:text-6xl font-bold text-white` tracking-tight.
- **Hero Subheadline**: `text-lg md:text-xl text-gray-200 mt-4`.
- **Section Heading**: `text-3xl md:text-4xl font-bold text-center text-[#0A2342]`.
- **Body Text**: `text-gray-700 leading-relaxed`.
- **Card**: `bg-white rounded-lg shadow-md overflow-hidden`.

--- 

### File: `frontend/src/components/Header.tsx`

This file defines the main navigation header for the website. It is responsive and sticky.

**Component: `Header`**
- **Props**: None.
- **State**: `const [isMenuOpen, setIsMenuOpen] = useState(false);` to manage the mobile menu visibility.
- **Logic**:
  1.  Render a `<header>` element with `className="sticky top-0 z-50 bg-[#0A2342] text-white shadow-md"`.
  2.  Inside a container (`max-w-7xl mx-auto px-4 sm:px-6 lg:px-8`), create a flexbox to align items (`flex items-center justify-between h-20`).
  3.  **Brand Logo**: A `Link` from `react-router-dom` to `/`. It should contain the text "Ouza" with the brand accent color: `<span className="text-3xl font-bold text-[#D4A843]">Ouza</span>`.
  4.  **Desktop Navigation**: For medium screens and up (`hidden md:flex`), render a `<nav>` with `NavLink` components from `react-router-dom` for "Home" (`/`), "Menu" (`/menu`), "Reservations" (`/reservations`), and "Contact" (`/contact`). Use `className="hover:text-[#D4A843] transition-colors"` for links.
  5.  **Desktop CTA**: A `Link` to `/order` styled as the Primary CTA: `<Link to="/order" className="hidden md:block bg-[#D4A843] hover:bg-[#c0953a] text-black font-semibold rounded-md px-6 py-2 transition-colors">Order Online</Link>`.
  6.  **Mobile Menu Button**: For small screens (`md:hidden`), render a button to toggle the mobile menu. Use the `Menu` icon from `lucide-react` when closed and the `X` icon when open. The `onClick` handler should call `setIsMenuOpen(!isMenuOpen)`.
  7.  **Mobile Navigation Menu**: Render a `div` absolutely positioned below the header that is shown/hidden based on `isMenuOpen`. It should have a `bg-[#0A2342]` background. Inside, list the same navigation links and the "Order Online" CTA button, stacked vertically.

### File: `frontend/src/components/Footer.tsx`

This file defines the site-wide footer, containing contact information, hours, and a map.

**Component: `Footer`**
- **Props**: None.
- **Logic**:
  1.  Render a `<footer>` element with `className="bg-[#0A2342] text-gray-300 py-12 px-4"`.
  2.  Inside a container (`max-w-7xl mx-auto`), create a grid layout (`grid grid-cols-1 md:grid-cols-3 gap-8`).
  3.  **Column 1: About Ouza**
      -   `h3` with `className="text-xl font-semibold text-white mb-4"`: "Ouza Cocktail Bar & Kitchen".
      -   `p` with the full address: "Murkute Complex, 45, Baner DP Rd, near Vijay Sales, Pallod Farms, Baner, Pune, Maharashtra 411069".
      -   `p` with the phone number: "Phone: 091461 93535".
      -   `p` with a placeholder email: "Email: contact@ouza.com".
  4.  **Column 2: Opening Hours**
      -   `h3` with `className="text-xl font-semibold text-white mb-4"`: "Opening Hours".
      -   `p`: "Mon - Thu: 12:00 PM - 11:00 PM".
      -   `p`: "Fri - Sun: 12:00 PM - 1:00 AM".
  5.  **Column 3: Location**
      -   `h3` with `className="text-xl font-semibold text-white mb-4"`: "Find Us".
      -   An `<iframe>` for Google Maps. Use the following `src`: `https://maps.google.com/maps?q=Ouza%20Cocktail%20Bar%20&%20Kitchen,Murkute%20Complex,45,Baner%20DP%20Rd,near%20Vijay%20Sales,Pallod%20Farms,Baner,Pune,Maharashtra%20411069&t=&z=15&ie=UTF8&iwloc=&output=embed`. Style it with `className="w-full h-48 rounded-md border-0"`.
  6.  **Copyright Bar**: Below the grid, add a `div` with a top border. It should contain `&copy; ${new Date().getFullYear()} Ouza. All rights reserved.` centered.

### File: `frontend/src/components/Layout.tsx`

This component wraps all public-facing pages, providing the consistent `Header` and `Footer`.

**Component: `Layout`**
- **Props**: `{ children: React.ReactNode }`.
- **Logic**:
  1.  Import `Header` from `./Header` and `Footer` from `./Footer`.
  2.  Render a main `div` with `className="flex flex-col min-h-screen"`.
  3.  Render the `<Header />` component.
  4.  Render a `<main>` element with `className="flex-grow"`.
  5.  Inside `<main>`, render the `children` prop.
  6.  Render the `<Footer />` component.

### File: `frontend/src/pages/HomePage.tsx`

This is the main landing page for the website.

**Component: `HomePage`**
- **Props**: None.
- **Logic**:
  1.  Wrap the entire page content in the `<Layout>` component.
  2.  **Hero Section**:
      -   A `div` with `className="relative h-screen bg-cover bg-center"` and a background image: `style={{ backgroundImage: "url('https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1920&q=80')" }}`.
      -   An overlay `div` with `className="absolute inset-0 bg-black bg-opacity-50"`.
      -   A centered content container with:
          -   `h1` (Hero H1 token): "Ouza Cocktail Bar & Kitchen".
          -   `p` (Hero Subheadline token): "Experience the vibrant flavors of the Mediterranean in the heart of Pune."
          -   A `div` with two CTA buttons: a `Link` to `/reservations` (Primary CTA style, "Book a Table") and a `Link` to `/order` (Secondary CTA style, "Order Online").
  3.  **About Us Section**:
      -   Use the Section Container and `bg-gray-50`.
      -   `h2` (Section Heading token): "Discover Ouza".
      -   A two-column grid with an image of the restaurant interior on one side and text on the other. The text should be welcoming and describe the restaurant's philosophy, using the sophisticated tone.
  4.  **Featured Menu Section**:
      -   Use the Section Container and `bg-white`.
      -   `h2` (Section Heading token): "A Taste of the Mediterranean".
      -   A grid of 3-4 cards. Each card should have an image, a title (e.g., "Lamb Kofta"), and a short description (e.g., "Perfectly spiced, chargrilled lamb skewers served with a cooling yogurt dip.").
  5.  **Testimonials Section**:
      -   Use the Section Container and `bg-gray-50`.
      -   `h2` (Section Heading token): "What Our Guests Say".
      -   Display 2-3 styled blockquotes with placeholder testimonials. Example: `"An unforgettable dining experience. The cocktails are a work of art!" - A Happy Customer`.

### File: `frontend/src/pages/ContactPage.tsx`

This page displays contact details and a map.

**Component: `ContactPage`**
- **Props**: None.
- **Logic**:
  1.  Wrap the entire page content in the `<Layout>` component.
  2.  **Page Header**:
      -   A `div` with `className="bg-gray-100 py-16"`.
      -   Inside, a container with an `h1` (Section Heading token, but not centered): "Contact Us".
  3.  **Main Content Section**:
      -   Use the Section Container (`py-16` not `py-20`) with `bg-white`.
      -   Create a two-column grid (`grid grid-cols-1 md:grid-cols-2 gap-12`).
      -   **Left Column: Contact Information**
          -   `h2` with `className="text-2xl font-bold text-[#0A2342] mb-4"`: "Get in Touch".
          -   Display the Address, Phone, and Email from the `Footer` component, but with more spacing and clear labels.
          -   Display the Opening Hours from the `Footer` component.
      -   **Right Column: Map**
          -   An `<iframe>` for Google Maps, same `src` as the footer but larger: `className="w-full h-96 rounded-lg shadow-md"`.

---

## Frontend: Authentication UI

**Name:** `frontend-auth-ui`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/context/AuthContext.tsx` — CONTEXT layer - Provides global authentication state (user, token, role) and methods (login, logout) to all child components. Manages the 'token' in localStorage.
- `frontend/src/hooks/useAuth.ts` — HOOK layer - A simple custom hook that provides convenient access to the AuthContext values, abstracting away the useContext boilerplate.
- `frontend/src/services/authService.ts` — SERVICE layer - Makes HTTP requests for authentication. Exports async functions login(credentials) and register(data) that call the backend API.
- `frontend/src/types/auth.ts` — UTIL layer - Exports all TypeScript interfaces and types for the authentication domain, such as AuthResponse and LoginCredentials, ensuring type safety across the feature.
- `frontend/src/components/ProtectedRoute.tsx` — COMPONENT layer - A route wrapper that checks authentication status using useAuth(). If the user is not authenticated (or not an admin for admin routes), it redirects to /login.
- `frontend/src/pages/LoginPage.tsx` — PAGE layer - Displays a login form for administrators and customers. On successful login, it redirects admins to /admin/dashboard and customers to their profile page.

**Feature Instruction:**

## Feature: Frontend Authentication UI

This feature implements the complete user authentication flow for the frontend application. It includes a React Context for global state management, a service for API communication, a login page, and protected route components. The core logic revolves around JWT-based authentication, with tokens stored in `localStorage`.

### Local Storage Key

The JWT token MUST be stored in `localStorage` under the key `'token'`. All files that interact with the token in `localStorage` must use this exact key.

### Dependency Note

You will need to add the `jwt-decode` library to handle JWT parsing:
`npm install jwt-decode`

--- 

### `frontend/src/types/auth.ts`

This file defines all TypeScript types and interfaces for the authentication domain.

**`LoginCredentials` interface**
```

typescript
export interface LoginCredentials {
  email: string;
  password: string;
}


```

**`RegisterData` interface**
```

typescript
export interface RegisterData {
  name: string;
  email: string;
  password: string;
  phone: string;
}


```

**`User` interface**
Represents the authenticated user object stored in the `AuthContext`.
```

typescript
export interface User {
  id: string;
  name: string;
  email: string;
  roles: string[];
}


```

**`AuthResponse` interface**
Matches the shape of the response from the backend's `/api/v1/auth/login` endpoint.
```

typescript
export interface AuthResponse {
  token: string;
  id: string;
  name: string;
  email: string;
  roles: string[];
}


```

---

### `frontend/src/services/authService.ts`

This service handles all HTTP requests related to authentication.

**Dependencies:**
- Import `api` from `frontend/src/api/client.ts`.
- Import `LoginCredentials`, `RegisterData`, `AuthResponse` from `frontend/src/types/auth.ts`.

**`login` function**
- **Signature:** `login(credentials: LoginCredentials): Promise<AuthResponse>`
- **Logic:**
  1. Make a `POST` request to `/api/v1/auth/login` using the `api` client.
  2. Pass `credentials` as the request body.
  3. On success, return the `response.data` which is expected to be of type `AuthResponse`.
  4. Axios will automatically throw for non-2xx responses, which will be handled by the caller.

**`register` function**
- **Signature:** `register(data: RegisterData): Promise<any>`
- **Logic:**
  1. Make a `POST` request to `/api/v1/auth/register` using the `api` client.
  2. Pass `data` as the request body.
  3. On success, return `response.data`.

---

### `frontend/src/context/AuthContext.tsx`

This file provides a global context for authentication state and logic.

**Dependencies:**
- `React`, `createContext`, `useState`, `useEffect`, `useContext`, `ReactNode`.
- `useNavigate` from `react-router-dom`.
- `jwtDecode` from `jwt-decode`.
- `authService` from `frontend/src/services/authService.ts`.
- `User`, `LoginCredentials`, `RegisterData` from `frontend/src/types/auth.ts`.

**`AuthContextType` interface**
Define an interface for the context value:
```

typescript
interface AuthContextType {
  isAuthenticated: boolean;
  isAdmin: boolean;
  user: User | null;
  token: string | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  register: (data: RegisterData) => Promise<any>;
  loading: boolean;
  error: string | null;
}


```

**`AuthContext`**
- Create the context: `export const AuthContext = createContext<AuthContextType | undefined>(undefined);`

**`AuthProvider` component**
- **Signature:** `AuthProvider({ children }: { children: React.ReactNode }): JSX.Element`
- **State Management:**
  - `user: User | null`
  - `token: string | null`
  - `isAuthenticated: boolean`
  - `isAdmin: boolean`
  - `loading: boolean` (initially `true` to check for token)
  - `error: string | null`
- **Hooks:**
  - `const navigate = useNavigate();`
- **`useEffect` on mount:**
  1. This effect runs once to initialize auth state from `localStorage`.
  2. Get token from `localStorage.getItem('token')`.
  3. If a token exists:
     a. Decode it using `jwtDecode`.
     b. Check if the token is expired by comparing `decodedToken.exp * 1000` with `Date.now()`.
     c. If not expired, construct a `User` object from the decoded token's claims (e.g., `sub` for email, `name`, `roles`). Set the `user`, `token`, `isAuthenticated`, and `isAdmin` states.
  4. Set `loading` to `false` at the end.
- **`login` function:**
  - **Signature:** `async (credentials: LoginCredentials): Promise<void>`
  - **Logic:**
    1. Set `loading` to `true` and `error` to `null`.
    2. Wrap the API call in a `try...catch` block.
    3. Call `authService.login(credentials)`.
    4. On success:
       a. Extract the `AuthResponse` data.
       b. Create a `User` object: `{ id: data.id, name: data.name, email: data.email, roles: data.roles }`.
       c. Set the `user` state.
       d. Set the `token` state with `data.token`.
       e. Set `isAuthenticated` to `true`.
       f. Check if `data.roles` includes `'ROLE_ADMIN'` and set `isAdmin` state accordingly.
       g. Store the token: `localStorage.setItem('token', data.token)`.
       h. Navigate based on role: if admin, `navigate('/admin/dashboard')`; otherwise, `navigate('/')`.
    5. In the `catch` block, set the `error` state with a user-friendly message (e.g., "Invalid email or password").
    6. In a `finally` block, set `loading` to `false`.
- **`logout` function:**
  - **Signature:** `(): void`
  - **Logic:**
    1. Set `user`, `token`, `isAuthenticated`, `isAdmin` states to their initial `null`/`false` values.
    2. Remove the token: `localStorage.removeItem('token')`.
    3. Navigate to `/login`.
- **`register` function:**
  - **Signature:** `async (data: RegisterData): Promise<any>`
  - **Logic:**
    1. Set `loading` to `true` and `error` to `null`.
    2. Call `authService.register(data)`.
    3. Return the result from the service call. Handle errors similarly to `login`.
- **Provider Value:**
  - Pass all state variables and functions (`isAuthenticated`, `isAdmin`, `user`, `token`, `login`, `logout`, `register`, `loading`, `error`) in the `AuthContext.Provider` value.

---

### `frontend/src/hooks/useAuth.ts`

This custom hook simplifies access to the `AuthContext`.

**Dependencies:**
- `useContext` from `react`.
- `AuthContext` from `frontend/src/context/AuthContext.tsx`.

**`useAuth` function**
- **Signature:** `useAuth(): AuthContextType`
- **Logic:**
  1. Get the context: `const context = useContext(AuthContext);`
  2. If `context` is `undefined`, throw an error: `'useAuth must be used within an AuthProvider'`. 
  3. Return the `context`.

---

### `frontend/src/components/ProtectedRoute.tsx`

This component guards routes based on authentication and authorization status.

**Dependencies:**
- `Navigate`, `useLocation` from `react-router-dom`.
- `useAuth` from `frontend/src/hooks/useAuth.ts`.

**`ProtectedRoute` component**
- **Signature:** `ProtectedRoute({ children, adminOnly = false }: { children: JSX.Element; adminOnly?: boolean }): JSX.Element | null`
- **Logic:**
  1. Get auth state using `const { isAuthenticated, isAdmin, loading } = useAuth();`.
  2. Get current location: `const location = useLocation();`.
  3. If `loading` is `true`, return a loading spinner or `null` to prevent rendering children prematurely.
  4. If `!isAuthenticated`, redirect to the login page. Return `<Navigate to="/login" state={{ from: location }} replace />`.
  5. If `adminOnly` is `true` and `!isAdmin`, redirect to the home page. Return `<Navigate to="/" replace />`.
  6. If all checks pass, render the `children`: `return children;`.

---

### `frontend/src/pages/LoginPage.tsx`

This page provides the user interface for logging in.

## Design Tokens
- **Page Container**: `<div className="min-h-screen flex items-center justify-center bg-[#fdfaf6] p-4">`
- **Form Card**: `bg-white rounded-xl shadow-lg border border-gray-100 p-8 md:p-12 w-full max-w-md`
- **Heading**: `text-3xl font-bold text-center text-[#002d5b]` (e.g., "Sign In")
- **Subheading**: `text-center text-gray-600 mt-2 mb-8` (e.g., "Welcome back to Ouza Cocktail Bar & Kitchen")
- **Input Label**: `text-sm font-medium text-gray-700`
- **Input Field**: `mt-1 block w-full px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-[#d4a843] focus:border-[#d4a843] sm:text-sm`
- **Primary CTA (Login Button)**: `w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-[#002d5b] hover:bg-[#001f3f] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#002d5b] transition-colors disabled:opacity-50`
- **Error Message**: `text-sm text-red-600 mt-2`

**Dependencies:**
- `useState` from `react`.
- `useAuth` from `frontend/src/hooks/useAuth.ts`.
- `Layout` from `frontend/src/components/Layout.tsx`.

**`LoginPage` component**
- **Signature:** `LoginPage(): JSX.Element`
- **State:**
  - `email: string`
  - `password: string`
- **Hooks:**
  - `const { login, loading, error } = useAuth();`
- **Logic:**
  1. The entire page content should be wrapped in the `<Layout>` component.
  2. Implement a form with `email` and `password` input fields and a submit button, styled according to the Design Tokens.
  3. Use the `email` and `password` state variables to control the input fields.
  4. Create a `handleSubmit` function that prevents default form submission and calls `await login({ email, password });`.
  5. The submit button should be disabled when `loading` is `true`, and can show a spinner or text like "Signing In...".
  6. If the `error` state from `useAuth` is not `null`, display it below the form inputs.

---

## Frontend: Admin Portal

**Name:** `frontend-admin-portal`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/components/admin/AdminLayout.tsx` — COMPONENT layer - A wrapper for all admin pages that provides a consistent layout with a left sidebar for navigation between Dashboard, Menu, Orders, and Reservations management.
- `frontend/src/pages/admin/AdminDashboardPage.tsx` — PAGE layer - The landing page for the admin section after login. Displays summary statistics (e.g., new orders, upcoming reservations) and provides navigation to detailed management pages.
- `frontend/src/pages/admin/AdminMenuPage.tsx` — PAGE layer - Provides a CRUD interface for menu management. Displays a table of all menu items with options to add, edit, or delete items via modal forms.
- `frontend/src/services/adminMenuService.ts` — SERVICE layer - Makes authenticated HTTP requests for menu management. Exports async functions for full CRUD operations (get, create, update, delete) on menu items.
- `frontend/src/pages/admin/AdminReservationsPage.tsx` — PAGE layer - Provides an interface for managing reservations. Displays a table of all bookings with options to confirm, cancel, or view details.
- `frontend/src/services/adminReservationService.ts` — SERVICE layer - Makes authenticated HTTP requests for reservation management. Exports async functions getReservations() and updateReservationStatus(id, status).
- `frontend/src/pages/admin/AdminOrdersPage.tsx` — PAGE layer - Provides an interface for managing orders. Displays a table of all orders with options to update status (e.g., from 'Received' to 'Preparing').
- `frontend/src/services/adminOrderService.ts` — SERVICE layer - Makes authenticated HTTP requests for order management. Exports async functions getOrders() and updateOrderStatus(id, status).

**Feature Instruction:**

## Design Tokens
- **Sidebar**: `bg-gray-900 text-gray-200`
- **Sidebar Link**: `flex items-center gap-3 rounded-lg px-3 py-2 text-gray-400 transition-all hover:text-gray-50 hover:bg-gray-800`
- **Active Sidebar Link**: `bg-gray-800 text-gray-50`
- **Header**: `bg-white border-b border-gray-200 sticky top-0 z-30`
- **Page Content Area**: `bg-gray-100/40 dark:bg-gray-800/40 p-4 sm:p-6`
- **Card/Widget**: `bg-white rounded-lg shadow-sm border border-gray-200`
- **Primary Button**: Use the `shadcn/ui` `Button` component with its default variant.
- **Table**: Use the `shadcn/ui` `Table` component and its sub-components.
- **Modal**: Use the `shadcn/ui` `Dialog` component for forms and `AlertDialog` for confirmations.
- **Form**: Use `shadcn/ui` components: `Input`, `Label`, `Select`, `Textarea`, `Switch`.
- **Badge**: Use the `shadcn/ui` `Badge` component for status indicators.

## Feature Overview
This feature implements the admin portal for "Ouza Cocktail Bar & Kitchen". It consists of a shared layout (`AdminLayout.tsx`) that provides a consistent sidebar navigation and header, and several pages for managing the business. Each page is a vertical slice, fetching its data via a dedicated service that communicates with the backend API. All admin routes must be protected to ensure only authenticated admin users can access them.

## Data Types
For clarity, the services and components will use the following data structures. These should be defined in their respective files under `frontend/src/types/`.

**`frontend/src/types/menu.ts`**
```

typescript
export interface MenuItem {
  id: string;
  name: string;
  description: string;
  price: number;
  categoryName: string;
  isAvailable: boolean;
}

export interface CreateMenuItemData extends Omit<MenuItem, 'id'> {}


```

**`frontend/src/types/reservation.ts`**
```

typescript
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface Reservation {
  id: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  reservationTime: string; // ISO 8601 format
  partySize: number;
  status: ReservationStatus;
}


```

**`frontend/src/types/order.ts`**
```

typescript
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'COMPLETED' | 'CANCELLED';

export interface OrderItem {
  menuItemId: string;
  name: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  user: { name: string; email: string; };
  orderItems: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  orderDate: string; // ISO 8601 format
}


```

## Services
All services must import the configured `api` instance from `frontend/src/api/client.ts` to ensure authenticated requests are made.

### `frontend/src/services/adminMenuService.ts`
This service handles all CRUD operations for menu items.

- **Imports**: `api` from `@/api/client`, `MenuItem`, `CreateMenuItemData` from `@/types/menu`.
- **`getMenuItems()`**: `Promise<MenuItem[]>`
  1. Makes a `GET` request to `/api/v1/admin/menu`.
  2. Returns the response data.
- **`createMenuItem(data: CreateMenuItemData)`**: `Promise<MenuItem>`
  1. Makes a `POST` request to `/api/v1/admin/menu` with `data` as the body.
  2. Returns the response data.
- **`updateMenuItem(id: string, data: Partial<CreateMenuItemData>)`**: `Promise<MenuItem>`
  1. Makes a `PUT` request to `/api/v1/admin/menu/${id}` with `data` as the body.
  2. Returns the response data.
- **`deleteMenuItem(id: string)`**: `Promise<void>`
  1. Makes a `DELETE` request to `/api/v1/admin/menu/${id}`.

### `frontend/src/services/adminReservationService.ts`
This service manages reservations.

- **Imports**: `api` from `@/api/client`, `Reservation` from `@/types/reservation`.
- **`getReservations()`**: `Promise<Reservation[]>`
  1. Makes a `GET` request to `/api/v1/admin/reservations`.
  2. Returns the response data.
- **`updateReservationStatus(id: string, status: string)`**: `Promise<Reservation>`
  1. Makes a `PUT` request to `/api/v1/admin/reservations/${id}/status` with `{ status }` as the body.
  2. Returns the response data.

### `frontend/src/services/adminOrderService.ts`
This service manages orders.

- **Imports**: `api` from `@/api/client`, `Order` from `@/types/order`.
- **`getOrders()`**: `Promise<Order[]>`
  1. Makes a `GET` request to `/api/v1/admin/orders`.
  2. Returns the response data.
- **`updateOrderStatus(id: string, status: string)`**: `Promise<Order>`
  1. Makes a `PUT` request to `/api/v1/admin/orders/${id}/status` with `{ status }` as the body.
  2. Returns the response data.

## Components

### `frontend/src/components/admin/AdminLayout.tsx`
This component provides the persistent UI shell for the admin portal.

- **Props**: `{ children: React.ReactNode }`
- **Dependencies**: `react-router-dom` for `Link` and `NavLink`, `lucide-react` for icons, `useAuth` from `@/hooks/useAuth.ts`.
- **Structure**:
  1. A root `div` with class `grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[280px_1fr]`.
  2. **Sidebar (`div`)**: Hidden on mobile, visible on `md` and up. Use `bg-gray-900 text-gray-200`.
     - Contains a `div` for the business name/logo: "Ouza Admin Panel".
     - A `nav` element with `NavLink` components for navigation. Use the active/inactive state of `NavLink` to apply active styles (`bg-gray-800 text-white`).
     - Links (with `lucide-react` icons like `Home`, `UtensilsCrossed`, `ShoppingCart`, `Calendar`):
       - `/admin/dashboard`: Dashboard
       - `/admin/menu`: Menu
       - `/admin/orders`: Orders
       - `/admin/reservations`: Reservations
  3. **Main Content Area (`div`)**: A `flex flex-col` container.
     - **Header (`header`)**: Use `bg-white border-b`. It should contain:
       - A mobile navigation button (using `Sheet` from `shadcn/ui`) that reveals the sidebar links.
       - A user dropdown menu (using `DropdownMenu` from `shadcn/ui`) on the right, triggered by a user icon. This menu should contain a "Logout" item.
     - **Logout Logic**: The "Logout" `DropdownMenuItem` should call the `logout` function obtained from the `useAuth()` hook.
     - **Content (`main`)**: A `main` tag with class `flex-1 p-4 sm:p-6 bg-gray-100/40`. This is where `{children}` will be rendered.

## Pages
All pages must be wrapped in `<AdminLayout>`.

### `frontend/src/pages/admin/AdminDashboardPage.tsx`
- **Structure**:
  1. Wrap the entire page content in `<AdminLayout>`. 
  2. Display a main heading: `h1` with `text-2xl font-bold` reading "Dashboard".
  3. Create a `div` with `grid gap-4 md:grid-cols-2 lg:grid-cols-4`.
  4. Inside the grid, render four `Card` components from `shadcn/ui` as summary widgets.
     - Each `Card` should have a `CardHeader` with a title (e.g., "Today's Revenue") and an icon, and a `CardContent` with a placeholder value (e.g., "$1,250", "+50 New Orders").

### `frontend/src/pages/admin/AdminMenuPage.tsx`
- **State Management**: Use `useState` and `useEffect` to fetch and store menu items from `adminMenuService.getMenuItems()`.
- **Structure**:
  1. Wrap in `<AdminLayout>`.
  2. A header `div` with `flex items-center justify-between` containing an `h1` ("Menu Management") and a `Button` ("Add New Item").
  3. Use `shadcn/ui`'s `Table` to display the menu items.
     - **Columns**: Name, Category, Price (formatted as currency), Availability (`Badge`), Actions.
     - **Actions Column**: A `DropdownMenu` with "Edit" and "Delete" items.
  4. **Add/Edit Modal**:
     - The "Add New Item" button and the "Edit" menu item will trigger a `Dialog` component.
     - The `Dialog` will contain a form built with `shadcn/ui` form components (`Input`, `Textarea`, `Select` for category, `Switch` for availability) for creating or editing a menu item. Use `react-hook-form` for form state management.
     - On submit, call `adminMenuService.createMenuItem` or `adminMenuService.updateMenuItem`, then close the modal and refetch the menu items.
  5. **Delete Confirmation**:
     - The "Delete" menu item will trigger an `AlertDialog` to confirm the action.
     - On confirmation, call `adminMenuService.deleteMenuItem` and refetch the menu items.

### `frontend/src/pages/admin/AdminReservationsPage.tsx`
- **State Management**: Use `useState` and `useEffect` to fetch and store reservations from `adminReservationService.getReservations()`.
- **Structure**:
  1. Wrap in `<AdminLayout>`.
  2. An `h1` for the title: "Reservations Management".
  3. A `Table` to display reservations.
     - **Columns**: Customer Name, Contact (Email/Phone), Reservation Time (formatted), Party Size, Status, Actions.
     - **Status Column**: Use a `Badge` with different colors based on the status (`PENDING`, `CONFIRMED`, `CANCELLED`).
     - **Actions Column**: A `DropdownMenu` allowing status updates. Menu items like "Confirm Reservation" and "Cancel Reservation" will call `adminReservationService.updateReservationStatus` with the new status, then refetch the data.

### `frontend/src/pages/admin/AdminOrdersPage.tsx`
- **State Management**: Use `useState` and `useEffect` to fetch and store orders from `adminOrderService.getOrders()`.
- **Structure**:
  1. Wrap in `<AdminLayout>`.
  2. An `h1` for the title: "Order Management".
  3. A `Table` to display orders.
     - **Columns**: Order ID, Date, Customer Name, Total, Status, Actions.
     - **Status Column**: Use a `Badge` with different colors for each status.
     - **Actions Column**: A `DropdownMenu` with options to update the order status (e.g., "Mark as Preparing", "Mark as Completed"). These actions will call `adminOrderService.updateOrderStatus` and then refetch the orders.

## Routing Integration
In `frontend/src/App.tsx`, ensure these admin pages are configured behind a protected route that checks for admin privileges.

- Import `ProtectedRoute` from `frontend-auth-ui`.
- Import all the newly created admin pages.
- Add the following routes, ensuring they are only accessible to admin users:

```

jsx
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminMenuPage from './pages/admin/AdminMenuPage';
import AdminOrdersPage from './pages/admin/AdminOrdersPage';
import AdminReservationsPage from './pages/admin/AdminReservationsPage';

// Inside your Routes component
<Route path="/admin/dashboard" element={<ProtectedRoute adminOnly={true}><AdminDashboardPage /></ProtectedRoute>} />
<Route path="/admin/menu" element={<ProtectedRoute adminOnly={true}><AdminMenuPage /></ProtectedRoute>} />
<Route path="/admin/orders" element={<ProtectedRoute adminOnly={true}><AdminOrdersPage /></ProtectedRoute>} />
<Route path="/admin/reservations" element={<ProtectedRoute adminOnly={true}><AdminReservationsPage /></ProtectedRoute>} />


```

---

## Frontend: Menu Display

**Name:** `frontend-menu-display`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/types/menu.ts` — UTIL layer - Exports all TypeScript interfaces for the menu domain, including MenuItem and MenuItemCategory, ensuring type safety for API data.
- `frontend/src/services/menuService.ts` — SERVICE layer - Makes HTTP requests for menu data. Exports async functions getMenu() and getCategories() that call the public backend API.
- `frontend/src/hooks/useMenu.ts` — HOOK layer - Provides React Query hooks like useMenu() that call menuService functions to fetch data, while managing state (loading, error, data) and caching.
- `frontend/src/pages/MenuPage.tsx` — PAGE layer - Displays the full restaurant menu. Uses the useMenu hook to fetch data and renders items grouped by category, with high-quality images and 'Add to Order' buttons.

**Feature Instruction:**

## Feature: Frontend Menu Display

This feature implements the public-facing digital menu for 'Ouza Cocktail Bar & Kitchen'. It fetches menu data from the backend, manages state with React Query, and renders an interactive, categorized menu page that allows users to add items to their order cart. The implementation spans from TypeScript type definitions to the final React page component.

### Design Tokens
- **Hero Section**: Full-width background image with a dark overlay. `bg-cover bg-center` with `div className="absolute inset-0 bg-black bg-opacity-50"`.
- **Hero H1**: `text-4xl md:text-6xl font-bold text-white`
- **Hero Subtitle**: `text-lg md:text-xl text-gray-200 mt-4`
- **Section Container**: `<section className="py-16 sm:py-24"><div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">`
- **Section Title**: `text-3xl md:text-4xl font-bold text-gray-900 mb-12 text-center`
- **Primary CTA / Add to Order Button**: `bg-[#d4a843] hover:bg-[#c0953b] text-white font-semibold rounded-md px-4 py-2 transition-all duration-200`
- **Category Filter Button (Active)**: `bg-gray-800 text-white rounded-full px-4 py-2`
- **Category Filter Button (Inactive)**: `bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full px-4 py-2`
- **Menu Item Card**: `bg-white rounded-lg shadow-md overflow-hidden border border-gray-100 flex flex-col`
- **Item Title**: `text-xl font-semibold text-gray-800`
- **Item Description**: `text-gray-600 mt-2`
- **Item Price**: `text-lg font-bold text-[#d4a843] mt-4`

### File Implementation Details

#### 1. `frontend/src/types/menu.ts`

This file defines the TypeScript types for the menu domain, ensuring type safety between the frontend and the backend API.

- **`MenuItemCategory` interface**: Represents a menu category.
  ```

typescript
  export interface MenuItemCategory {
    id: string; // UUID
    name: string;
  }
  

```

- **`MenuItem` interface**: Represents a single menu item. This must match the structure of `MenuItemDto.java` from the backend.
  ```

typescript
  export interface MenuItem {
    id: string; // UUID
    name: string;
    description: string;
    price: number;
    categoryName: string;
    imageUrl: string;
    isAvailable: boolean;
  }
  

```

- **`CreateMenuItemData` type**: Defines the shape for creating a new menu item. While used by the admin portal, it's defined here for domain cohesion.
  ```

typescript
  export type CreateMenuItemData = {
    name: string;
    description: string;
    price: number;
    categoryId: string; // UUID of the category
    imageUrl?: string;
  };
  

```

#### 2. `frontend/src/services/menuService.ts`

This service is responsible for all HTTP communication related to the public menu.

- **Dependencies**: Import `api` from `../api/client` (the pre-configured Axios instance) and `MenuItem`, `MenuItemCategory` from `../types/menu.ts`.

- **`getMenu(): Promise<MenuItem[]>` function**:
  1.  Makes a GET request to `/api/v1/menu` using the `api` client.
  2.  On success, it returns the `data` property of the response, which is an array of `MenuItem` objects.
  3.  Errors will be automatically handled by the Axios interceptor and React Query.

- **`getCategories(): Promise<MenuItemCategory[]>` function**:
  1.  Makes a GET request to `/api/v1/menu/categories` using the `api` client.
  2.  On success, it returns the `data` property of the response, which is an array of `MenuItemCategory` objects.

#### 3. `frontend/src/hooks/useMenu.ts`

This file provides React Query hooks to fetch, cache, and manage the state for menu data, connecting the service layer to the UI.

- **Dependencies**: Import `useQuery`, `UseQueryResult` from `@tanstack/react-query`. Import `getMenu`, `getCategories` from `../services/menuService.ts`. Import `MenuItem`, `MenuItemCategory` from `../types/menu.ts`.

- **`useMenu(): UseQueryResult<MenuItem[], Error>` function**:
  1.  Implement a `useQuery` hook.
  2.  Set the `queryKey` to `['menu']`.
  3.  Set the `queryFn` to `getMenu` from `menuService`.
  4.  Return the result of the `useQuery` call.

- **`useMenuCategories(): UseQueryResult<MenuItemCategory[], Error>` function**:
  1.  Implement a `useQuery` hook.
  2.  Set the `queryKey` to `['menuCategories']`.
  3.  Set the `queryFn` to `getCategories` from `menuService`.
  4.  Return the result of the `useQuery` call.

#### 4. `frontend/src/pages/MenuPage.tsx`

This page component renders the entire menu, making it interactive and visually appealing according to the design context.

- **Component Structure**:
  - The entire page content must be wrapped in the `<Layout>` component imported from `@/components/Layout.tsx`.
  - It will contain a Hero section, a category filter section, and the main menu grid.

- **Data and State Hooks**:
  - Import and call `useMenu` and `useMenuCategories` to fetch data.
  - Import and use `useContext(CartContext)` from `@/context/CartContext.tsx` to get the `addToCart` function. Assume the context provides `addToCart: (item: MenuItem) => void;`.
  - Use `useState<string | null>(null)` to manage the `selectedCategory`, initialized to `null` (which will represent 'All').
  - Use `useMemo` to group menu items by category name for efficient rendering. The result should be a structure like `Record<string, MenuItem[]>`. 

- **Rendering Logic**:
  1.  **Loading State**: If `isLoadingMenu` or `isLoadingCategories` is true, display a full-page loading spinner or skeleton loaders for the menu cards.
  2.  **Error State**: If `isErrorMenu` or `isErrorCategories` is true, display a user-friendly error message.
  3.  **Hero Section**:
      - Use a `div` with a background image: `https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1920&q=80`.
      - Add the dark overlay `div` on top.
      - Inside a container, add an `h1` with text like "Our Menu" and a `p` tag with a welcoming subtitle like "Discover the vibrant tastes of the Mediterranean, crafted with the freshest ingredients at Ouza Cocktail Bar & Kitchen."
  4.  **Menu Content Section**:
      - Use the `Section Container` token for padding and max-width.
      - Display the `Section Title` "Explore Our Flavours".
      - **Category Filters**: Render an 'All' button and then map over the `categories` data to render a button for each category. Use the active/inactive design tokens based on the `selectedCategory` state.
      - **Menu Grid**: 
          - Group the `menuItems` data by `categoryName`.
          - Map through the `categories` data. If a category (or 'All') is selected, render its section.
          - For each category, render its name as a sub-heading (`h3` or `h4`).
          - Render the associated menu items in a responsive grid (`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8`).
          - Each item in the grid should be a card component.
  5.  **Menu Item Card (Sub-component logic)**:
      - Use the `Menu Item Card` design token for styling.
      - Display the item's `imageUrl` in an `<img>` tag with `aspect-ratio: 3/2` and `object-cover`.
      - In the card body, display the `name` (Item Title token), `description` (Item Description token), and `price` (Item Price token, formatted as `₹{item.price.toFixed(2)}`).
      - Include an "Add to Order" button (Primary CTA token) which, on click, calls `addToCart(item)`.

---

## Frontend: Reservation Booking

**Name:** `frontend-reservation-booking`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/types/reservation.ts` — UTIL layer - Exports all TypeScript interfaces for the reservation domain, including Reservation and CreateReservationData, ensuring type safety.
- `frontend/src/services/reservationService.ts` — SERVICE layer - Makes HTTP requests for reservations. Exports an async function createReservation(data) that calls the public backend API.
- `frontend/src/hooks/useReservations.ts` — HOOK layer - Provides a React Query mutation hook useCreateReservation() that calls the reservationService to submit a new booking and manages the mutation state.
- `frontend/src/pages/ReservationPage.tsx` — PAGE layer - Displays an intuitive form for booking a table. Uses the useCreateReservation hook to submit the form data and handles success/error states.

**Feature Instruction:**

## Feature: Frontend Reservation Booking

This feature implements the customer-facing table reservation functionality. It includes a reservation page with a form, a React Query hook to manage the API mutation, a service to handle the HTTP request, and the necessary TypeScript types.

### Design Tokens
- **Page Layout**: Use the `<Layout>` component from `@/components/Layout.tsx` to wrap the page content.
- **Hero Section**: Full-width section with a background image and a dark overlay.
  - **Image**: `https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1920&q=80`
  - **Overlay**: `<div className="absolute inset-0 bg-black bg-opacity-50" />`
  - **H1**: `text-4xl md:text-6xl font-bold text-white`
- **Form Section**:
  - **Container**: `<div className="max-w-2xl mx-auto p-8 bg-white rounded-lg shadow-xl">`
  - **Heading**: `text-3xl font-bold text-center text-[#003366] mb-6` (Deep Mediterranean Blue)
- **Form Elements (Shadcn/UI)**:
  - **Label**: `text-gray-700 font-medium`
  - **Input**: `bg-gray-50 border border-gray-300 rounded-md focus:ring-[#d4a843] focus:border-[#d4a843]` (Gold/Brass accent)
- **Primary CTA (Submit Button)**: `bg-[#d4a843] hover:bg-[#b8860b] text-white font-semibold rounded-md px-8 py-3 w-full transition-all duration-200`
- **Success/Error Messages**:
  - **Success**: `text-green-600 bg-green-100 p-4 rounded-md`
  - **Error**: `text-red-600 bg-red-100 p-4 rounded-md`

--- 

### File: `frontend/src/types/reservation.ts`

This file defines the TypeScript interfaces for the reservation domain.

**`Reservation` interface**
Matches the `ReservationDto` from the backend. It represents a confirmed reservation.
```

typescript
export interface Reservation {
  id: string; // UUID
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  partySize: number;
  reservationTime: string; // ISO 8601 format
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
}


```

**`CreateReservationData` type**
This type defines the shape of the data required to create a new reservation. It's used by the form and sent to the API.
```

typescript
export type CreateReservationData = Omit<Reservation, 'id' | 'status'>;


```

### File: `frontend/src/services/reservationService.ts`

This service handles the API call to create a reservation.

**Dependencies**
- `axios` client instance from `../api/client`.
- `Reservation` and `CreateReservationData` types from `../types/reservation.ts`.

**`createReservation` function**
- **Signature**: `export const createReservation = async (data: CreateReservationData): Promise<Reservation> => { ... }`
- **Logic**:
  1.  Import the pre-configured `api` client from `@/api/client`.
  2.  Make a `POST` request to `/api/v1/reservations` using `api.post<Reservation>()`.
  3.  Pass the `data` object as the request body.
  4.  The endpoint is defined in the `reservation-system-api` feature.
  5.  Return `response.data` on success.
  6.  Axios interceptors will handle standard API errors; no custom try/catch is needed here.

### File: `frontend/src/hooks/useReservations.ts`

This file provides a React Query mutation hook for creating reservations, abstracting away the API call and state management.

**Dependencies**
- `useMutation` from `@tanstack/react-query`.
- `createReservation` function from `../services/reservationService`.
- `Reservation` and `CreateReservationData` types from `../types/reservation.ts`.

**`useCreateReservation` hook**
- **Signature**: `export const useCreateReservation = (): UseMutationResult<Reservation, Error, CreateReservationData> => { ... }`
- **Logic**:
  1.  Call `useMutation` from `@tanstack/react-query`.
  2.  Provide an object with the `mutationFn` property set to `createReservation` from the `reservationService`.
  3.  Return the result of the `useMutation` call. This will provide `mutate`, `isPending`, `isSuccess`, `isError`, etc., to the component.

### File: `frontend/src/pages/ReservationPage.tsx`

This page component renders the reservation form for customers.

**Dependencies**
- `React`, `useState` for form state management.
- `<Layout>` component from `@/components/Layout.tsx`.
- `useCreateReservation` hook from `../hooks/useReservations.ts`.
- Shadcn/UI components: `Input`, `Button`, `Label`, and potentially a date/time picker if available, otherwise use text inputs with `type="date"` and `type="time"`.

**Component: `ReservationPage`**
- **Signature**: `export const ReservationPage = (): JSX.Element => { ... }`
- **State Management**:
  1.  Call `useCreateReservation()` to get mutation handlers: `const { mutate, isPending, isSuccess, isError, error } = useCreateReservation();`
  2.  Use `useState` or `react-hook-form` to manage form fields: `customerName`, `customerEmail`, `customerPhone`, `partySize`, `reservationDate`, `reservationTime`.

- **Rendering Logic**:
  1.  Wrap the entire page content in the `<Layout>` component.
  2.  Render a hero section with the background image and overlay defined in the Design Tokens. The heading (`h1`) should be "Book Your Table at Ouza Cocktail Bar & Kitchen". A sub-heading can be "Experience the taste of the Mediterranean."
  3.  Below the hero, render a section containing the reservation form inside a styled container as per the Design Tokens.
  4.  The form should have a heading (`h2`) like "Make a Reservation".
  5.  The form must include the following fields, using Shadcn/UI `Label` and `Input` components:
      -   Customer Name (text input, required)
      -   Customer Email (email input, required)
      -   Customer Phone (tel input, required)
      -   Party Size (number input, required, min 1)
      -   Date (date input, required)
      -   Time (time input, required)
  6.  The submit button should be a Shadcn/UI `Button` styled as the Primary CTA. Its text should be "Book Now". Disable the button and show a spinner when `isPending` is true.

- **Form Submission Logic**:
  1.  Implement an `onSubmit` handler for the form.
  2.  Inside the handler, prevent the default form submission.
  3.  Combine the `reservationDate` and `reservationTime` form fields into a single ISO 8601 string (e.g., `new Date(`${date}T${time}`).toISOString()`).
  4.  Construct the `CreateReservationData` object from the form state.
  5.  Call `mutate(reservationData)`.

- **Feedback Logic**:
  1.  If `isSuccess` is true, display a success message above the form: "Thank you! Your reservation has been successfully submitted. We will confirm shortly via email."
  2.  If `isError` is true, display an error message: `"An error occurred: " + (error?.message || 'Please try again.')`

### Inter-File and Cross-Feature Wiring
- `ReservationPage.tsx` uses the `useCreateReservation` hook from `useReservations.ts` to handle form submission.
- `useReservations.ts` calls the `createReservation` function from `reservationService.ts` as its `mutationFn`.
- `reservationService.ts` makes a `POST` request to the `/api/v1/reservations` endpoint, which is implemented in the `reservation-system-api` backend feature.
- `ReservationPage.tsx` is wrapped in the `<Layout>` component from the `frontend-core-layout` feature.

---

## Frontend: Order & Checkout Flow

**Name:** `frontend-order-flow`  
**Type:** FRONTEND  
**Change required:** true

**Files in this feature:**
- `frontend/src/types/order.ts` — UTIL layer - Exports all TypeScript interfaces for the ordering domain, including CartItem and Order, ensuring type safety.
- `frontend/src/context/CartContext.tsx` — CONTEXT layer - Provides global state management for the shopping cart. Exposes the cart items array and functions like addItemToCart, removeItem, and clearCart.
- `frontend/src/services/orderService.ts` — SERVICE layer - Makes HTTP requests for ordering. Exports async functions createOrder(data) and verifyPayment(paymentData).
- `frontend/src/hooks/useOrders.ts` — HOOK layer - Provides a React Query mutation hook useCreateOrder() that calls the orderService to place an order and manages the mutation state.
- `frontend/src/pages/CheckoutPage.tsx` — PAGE layer - Displays the cart summary and initiates the payment flow. On 'Pay Now' click, it calls useCreateOrder, then opens the Razorpay checkout with the received order ID.

**Feature Instruction:**

## Feature: Frontend Order & Checkout Flow

This feature implements the complete user-facing order and checkout experience. It includes a shopping cart managed via React Context, a checkout page to review the order, and integration with Razorpay for payment processing.

### Inter-file Relationships

1.  **`frontend/src/types/order.ts`**: Defines the core TypeScript interfaces (`CartItem`, `Order`, `CreateOrderData`) used across all other files in this feature.
2.  **`frontend/src/context/CartContext.tsx`**: Provides a global `CartContext` to manage the shopping cart state. It exposes the cart items and functions to modify them. It persists the cart to `localStorage`.
3.  **`frontend/src/services/orderService.ts`**: Contains functions (`createOrder`, `verifyPayment`) that make API calls to the backend endpoints (`/api/v1/orders` and `/api/v1/payments/verify`) using the shared `apiClient`.
4.  **`frontend/src/hooks/useOrders.ts`**: Exports `useCreateOrder`, a React Query mutation hook that wraps the `orderService.createOrder` function to handle server state, loading, and errors gracefully.
5.  **`frontend/src/pages/CheckoutPage.tsx`**: The main UI component. It consumes `CartContext` to display the order summary, uses the `useCreateOrder` hook to initiate the order process, and integrates the Razorpay payment gateway script to handle the final payment.

### Cross-Feature Integration

*   **API Endpoints Consumed**:
    *   `POST /api/v1/orders` (from `order-management-api`) to create an order and get a Razorpay Order ID.
    *   `POST /api/v1/payments/verify` (from `payment-processing`) to verify the payment signature after successful payment.
*   **Shared Components**:
    *   The `CheckoutPage` must be wrapped in the `<Layout>` component from `frontend/src/components/Layout.tsx`.
*   **Authentication**:
    *   The checkout process is for authenticated users. The route for `CheckoutPage` should be wrapped in `<ProtectedRoute>` from `frontend-auth-ui`.
    *   The `apiClient` (from `frontend/src/api/client.ts`) is expected to have an interceptor that attaches the JWT token from `localStorage.getItem('token')` to all outgoing requests.

--- 

## Design Tokens (for CheckoutPage.tsx)

-   **Page Background**: `bg-gray-50`
-   **Primary CTA (Pay Now)**: `bg-[#D4A843] hover:bg-[#c89c3a] text-white font-semibold rounded-lg px-8 py-3 transition-all duration-200 w-full`
-   **Card/Container**: `bg-white rounded-xl shadow-md border border-gray-100 p-6`
-   **Section Title**: `text-2xl font-bold text-[#003366]`
-   **Body Text**: `text-gray-700`
-   **Link**: `text-[#D4A843] hover:underline`

--- 

### File Implementation Details

#### 1. `frontend/src/types/order.ts`

This file defines all TypeScript types for the order domain.

```

typescript
// A minimal representation of a menu item, needed for the cart.
export interface MenuItem {
  id: string;
  name: string;
  price: number;
}

// Represents an item within the shopping cart.
export interface CartItem {
  id: string; // This is the menuItem's ID
  name: string;
  price: number;
  quantity: number;
}

// Represents an item within a confirmed order.
export interface OrderItem {
  menuItemName: string;
  quantity: number;
  price: number;
}

// Matches the OrderResponse.java DTO from the backend.
export interface Order {
  id: string;
  orderItems: OrderItem[];
  totalAmount: number;
  orderStatus: string;
  razorpayOrderId: string;
  createdAt: string;
}

// Type for the data sent to the createOrder API endpoint.
export type CreateOrderData = {
  items: {
    menuItemId: string;
    quantity: number;
  }[];
};


```

#### 2. `frontend/src/context/CartContext.tsx`

This file creates a React Context for managing the shopping cart state globally.

*   **State**: `cartItems: CartItem[]` managed with `useState`.
*   **Persistence**: Use a `useEffect` hook to synchronize the `cartItems` state with `localStorage`.
    *   On initial load, read the cart from `localStorage.getItem('cart')`.
    *   On any change to `cartItems`, write the updated state to `localStorage.setItem('cart', JSON.stringify(cartItems))`.
*   **Context Value**: The context should provide:
    *   `cartItems: CartItem[]`
    *   `addItemToCart(item: MenuItem): void`
        1.  Check if the item already exists in the cart by its `id`.
        2.  If it exists, increment its `quantity`.
        3.  If it doesn't exist, add it to the cart as a new `CartItem` with `quantity: 1`.
    *   `removeItem(itemId: string): void`
        1.  Find the item in the cart.
        2.  If its quantity is greater than 1, decrement the quantity.
        3.  If its quantity is 1, remove the item completely from the `cartItems` array.
    *   `clearCart(): void`
        1.  Set `cartItems` to an empty array `[]`.
    *   `getCartTotal(): number`
        1.  Calculate and return the total price of all items in the cart (`price * quantity`).
*   **Component**: `CartProvider({ children })` wraps its children in the `CartContext.Provider`.

#### 3. `frontend/src/services/orderService.ts`

This service handles all HTTP communication related to orders.

*   **Dependencies**: Import `apiClient` from `../api/client` and types from `../types/order`.

*   **`createOrder(data: CreateOrderData): Promise<Order>`**
    1.  Make a `POST` request to `/api/v1/orders` using `apiClient`.
    2.  Pass `data` as the request body.
    3.  On success, return `response.data`.
    4.  On error, `apiClient`'s interceptors should handle it, but allow the error to propagate.

*   **`verifyPayment(paymentData: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string; }): Promise<any>`**
    1.  Make a `POST` request to `/api/v1/payments/verify` using `apiClient`.
    2.  Pass `paymentData` as the request body.
    3.  On success, return `response.data`.

#### 4. `frontend/src/hooks/useOrders.ts`

This file provides a React Query mutation hook for creating an order.

*   **Dependencies**: Import `useMutation` from `@tanstack/react-query`, `orderService` and types.

*   **`useCreateOrder(): UseMutationResult<Order, Error, CreateOrderData>`**
    1.  Return the result of calling `useMutation`.
    2.  The configuration object for `useMutation` should have:
        *   `mutationFn: orderService.createOrder`

#### 5. `frontend/src/pages/CheckoutPage.tsx`

This page allows users to review their cart and complete the payment.

*   **Dependencies**: `React`, `useContext`, `useEffect`, `Layout`, `CartContext`, `useCreateOrder`, `orderService`, `react-router-dom` for `Link`, and an icon library like `lucide-react`.

*   **Component Structure**:
    1.  Wrap the entire page content in `<Layout>`.
    2.  Use `useContext(CartContext)` to get cart data and functions (`cartItems`, `removeItem`, `getCartTotal`, `clearCart`).
    3.  Use `useCreateOrder()` to get the mutation function and its state (`mutate`, `isPending`).
    4.  If `cartItems.length === 0`, display a message: "Your cart is empty." with a `<Link>` to the menu page.
    5.  Otherwise, render a two-column layout on larger screens:
        *   **Left Column: Order Summary**
            *   A card (`bg-white rounded-xl shadow-md p-6`).
            *   Title: "Your Order".
            *   Map through `cartItems` to display each item's name, quantity, price, and a button with a trash icon to call `removeItem(item.id)`.
            *   Display Subtotal, Taxes (e.g., 5%), and a bold Total amount, calculated using `getCartTotal()`.
        *   **Right Column: Payment**
            *   A card with a title "Confirm and Pay".
            *   A "Pay Now" button (`bg-[#D4A843]`). This button should be disabled and show a spinner if `isPending` from `useCreateOrder` is true.

*   **Razorpay Integration Logic**:
    1.  **Load Script**: Use a `useEffect` hook to dynamically create a `<script>` tag with `src="https://checkout.razorpay.com/v1/checkout.js"` and append it to the document head. Include a cleanup function to remove it on component unmount.
    2.  **Handle Payment Button Click**:
        *   The `onClick` handler of the "Pay Now" button should call `mutate(orderData)` from `useCreateOrder`.
        *   `orderData` should be constructed from `cartItems` in the format `{ items: [{ menuItemId: '...', quantity: ... }] }`.
    3.  **Handle Mutation Success**:
        *   The `useCreateOrder` hook should be configured with an `onSuccess` callback that receives the `order` object from the backend.
        *   Inside `onSuccess`:
            a. Check if `window.Razorpay` is available.
            b. Create the `options` object for Razorpay:
                *   `key`: `import.meta.env.VITE_RAZORPAY_KEY_ID` (ensure this is in your `.env.local` file).
                *   `amount`: `order.totalAmount * 100`.
                *   `currency`: "INR".
                *   `name`: "Ouza Cocktail Bar & Kitchen".
                *   `description`: "Order Payment".
                *   `order_id`: `order.razorpayOrderId`.
                *   `handler`: A function that will be called upon successful payment. This function receives a response object containing `razorpay_payment_id`, `razorpay_order_id`, and `razorpay_signature`.
                *   `prefill`: Can be used to pre-fill customer name/email if available from an auth context.
            c. Instantiate Razorpay: `const razorpay = new window.Razorpay(options);`
            d. Open the checkout modal: `razorpay.open();`
    4.  **Payment Verification**: 
        *   Inside the Razorpay `handler` function, call `orderService.verifyPayment` with the payment details.
        *   On successful verification, call `clearCart()`, show a success toast/alert (e.g., "Payment successful! Your order has been placed."), and navigate the user to a confirmation page or their order history.
    5.  **Error Handling**: Use the `onError` callback in `useCreateOrder` and a `.catch` on the `verifyPayment` promise to show error toasts to the user (e.g., "Failed to create order. Please try again.").

---

## Infrastructure

**Name:** `infrastructure`  
**Type:** INFRA  
**Change required:** true

**Files in this feature:**
- `Dockerfile` — INFRA layer - Defines the steps to build the Java application using Maven and create a lightweight, production-ready Docker image for deployment.
- `docker-compose.yml` — INFRA layer - Orchestrates the local development environment, defining services for the Spring Boot backend and a PostgreSQL database, with networking and volume mounts.
- `.env.example` — CONFIG layer - A template for environment variables required by the application, including database credentials, JWT secret, and payment gateway keys.
- `.github/workflows/ci-cd.yml` — INFRA layer - Defines the CI/CD pipeline. On push to main, it builds, tests, and deploys the backend to AWS App Runner and the frontend to Vercel.

**Feature Instruction:**

_Not enriched (INFRA or skipped)._

---

