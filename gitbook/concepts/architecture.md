# Architecture

## Monolithic Architecture

One of the first architectural decisions in software development concerns the internal organization of application code. Beginning developers often create applications as a single block of code without a clear separation of responsibilities. While such an approach may work for small projects, it quickly becomes problematic as the application grows.

In a **monolithic architecture**, all functionality is implemented together without a clearly defined structure. User interface code, business logic, database communication, validation, and utility functions may be mixed within the same classes or packages. As a result, classes become large and difficult to understand, maintain, and test. A change in one part of the application can easily introduce unexpected side effects in another part.

For example, a controller may directly communicate with the database and implement business rules at the same time:

{% code lineNumbers="true" %}
```
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {

        User user = jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new Object[]{id},
            (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("name"))
        );

        if (user.getName().length() < 3) {
            throw new IllegalArgumentException("Invalid name");
        }

        return user;
    }
}
```
{% endcode %}

Although this solution works, the controller is responsible for multiple tasks. It handles HTTP communication, executes SQL queries, and performs business validation. Such coupling reduces code readability and makes future modifications more difficult.

## Layered architecture

A more suitable approach is to use a **layered architecture**. In this architecture, the application is divided into several logical layers, each with a single responsibility. The most common structure used in Spring Boot applications consists of the presentation layer, service layer, and persistence layer.

The presentation layer is responsible for communication with clients and processing HTTP requests. The service layer contains business logic and implements application rules. The persistence layer handles communication with the database and data storage operations.

The following diagram illustrates a typical layered architecture:

{% code lineNumbers="true" %}
```
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```
{% endcode %}

By separating responsibilities into layers, the application becomes easier to maintain and extend. Individual layers can be modified independently, business logic can be tested without a running web server, and database technology can be changed with minimal impact on the rest of the system.

In Spring Boot, a layered architecture is often implemented using dedicated packages:

{% code lineNumbers="true" %}
```
com.example.application
├── controller
├── service
├── repository
├── entity
└── dto
```
{% endcode %}

The controller delegates processing to the service layer:

{% code lineNumbers="true" %}
```
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```
{% endcode %}

The service layer contains business logic:

{% code lineNumbers="true" %}
```
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDto findById(Long id) {
        User user = repository.findById(id)
                .orElseThrow();

        return UserDto.fromEntity(user);
    }
}
```
{% endcode %}

## MVC

Many modern web applications extend the layered architecture by applying the **Model-View-Controller (MVC)** architectural pattern. MVC further separates the responsibilities associated with user interaction. The **Model** represents application data, the **View** is responsible for presenting information to the user, and the **Controller** processes user requests and coordinates communication between the view and the business layer.

In traditional Spring MVC applications, controllers receive HTTP requests, services perform business operations, and views are rendered using technologies such as Thymeleaf. In REST-based applications, which are common in modern Spring Boot projects, controllers typically return JSON responses instead of HTML pages, while the frontend is implemented separately using frameworks such as React, Angular, or Vue.

As a result, layered architecture and MVC complement each other. Layered architecture improves the internal organization of the application, while MVC structures the interaction between the user interface and the backend. Together they promote better maintainability, scalability, and code quality than a simple monolithic design that mixes all responsibilities in a single place.

#### Responsibilities of the MVC Components in REST Applications

Although the MVC pattern was originally designed for applications that generate HTML pages, its principles are equally applicable to modern REST-based applications. In this context, the **View** does not represent an HTML page. Instead, it is the data returned to the client, most commonly in JSON format.

The **Model** represents the application's data and business domain. It contains classes that describe business entities and transfer data between application layers. In Spring Boot applications, models are typically implemented as entities and DTOs (Data Transfer Objects). The model defines _what data the application works with_, but not _how it is displayed_ or _how it is obtained_.

For example, an application managing products may use the following model:

{% code lineNumbers="true" %}
```
@Entity
public class Product {

    @Id
    private Long id;

    private String name;

    private BigDecimal price;

    // getters and setters
}
```
{% endcode %}

The **Controller** acts as the entry point to the application. It receives HTTP requests from clients, extracts request parameters, invokes the business logic, and returns a response. Controllers should remain lightweight and should not contain complex business rules or database operations.

A typical REST controller in Spring Boot may look as follows:

{% code lineNumbers="true" %}
```
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }
}
```
{% endcode %}

The **View** represents the final response returned to the client. In a REST application, this is usually a DTO automatically converted into JSON by Spring Boot. Unlike traditional MVC applications, where the view is an HTML template, REST applications use serialized objects as their views.

For example, the DTO returned by the controller may look as follows:

{% code lineNumbers="true" %}
```
public class ProductDto {

    private Long id;

    private String name;

    private BigDecimal price;

    // getters and setters
}
```
{% endcode %}

When the endpoint is called, the client receives the following JSON response:

{% code lineNumbers="true" %}
```
{
    "id": 1,
    "name": "Laptop",
    "price": 999.99
}
```
{% endcode %}

From the MVC perspective, this JSON document serves as the View because it is the representation of the data exposed to the user or frontend application.

The communication flow can therefore be simplified as:

{% code lineNumbers="true" %}
```
HTTP Request
      ↓
 Controller
      ↓
Model / Business Logic
      ↓
DTO Object
      ↓
JSON Response (View)
```
{% endcode %}

#### MVC Combined with Layered Architecture

In Spring Boot applications, MVC is typically combined with a layered architecture. Each component has a clearly defined responsibility.

The controller handles HTTP communication, the service layer contains business logic, the repository layer provides data access, and models represent application data.

The resulting request flow is usually:

{% code lineNumbers="true" %}
```
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Database

Database
   ↑
Repository
   ↑
Service
   ↑
DTO (View)
   ↑
Controller
   ↑
Client
```
{% endcode %}

#### Example Project Structure

A typical REST-based Spring Boot application may be organized as follows:

{% code lineNumbers="true" %}
```
src
└── main
    ├── java
    │   └── com.example.shop
    │
    │       ├── controller
    │       │   └── ProductController.java
    │       │
    │       ├── service
    │       │   ├── ProductService.java
    │       │   └── ProductServiceImpl.java
    │       │
    │       ├── repository
    │       │   └── ProductRepository.java
    │       │
    │       ├── entity
    │       │   └── Product.java
    │       │
    │       ├── dto
    │       │   └── ProductDto.java
    │       │
    │       └── Application.java
    │
    └── resources
        └── application.properties
```
{% endcode %}

In this structure:

* `entity` contains database entities representing the application's domain model.
* `repository` provides database access.
* `service` contains business logic.
* `controller` exposes REST endpoints.
* `dto` contains objects used as REST responses and requests.

When viewed through the MVC perspective, the mapping is approximately:

{% code lineNumbers="true" %}
```
MVC Component         Spring Boot Classes

Model      →  Product, ProductDto
Controller →  ProductController
View       →  JSON returned by REST endpoints
```
{% endcode %}

This approach provides significantly better maintainability than placing database access, business logic, and HTTP processing in a single class. Each component has a clearly defined responsibility, making the application easier to understand, test, and extend.

## Vertical Slice Architecture

Although layered architecture significantly improves code organization compared to a monolithic design, it introduces another problem. As the application grows, functionality becomes spread across many packages. To understand a single use case, a developer often needs to navigate through controllers, services, repositories, DTOs, and entities located in different parts of the project.

For example, implementing a simple "Get Product" feature may require modifications in several locations:

{% code lineNumbers="true" %}
```
controller/ProductController.java
service/ProductService.java
service/ProductServiceImpl.java
repository/ProductRepository.java
dto/ProductDto.java
entity/Product.java
```
{% endcode %}

As the number of features increases, developers spend more time navigating through the project structure than implementing business logic. The architecture becomes organized according to technical concerns rather than business functionality.

To address this issue, modern applications often adopt **Vertical Slice Architecture (VSA)**. Instead of grouping files by their technical role, VSA groups them by business feature. Each feature contains everything it needs, including controllers, services, requests, responses, validators, and other supporting classes.

The key idea is that code belonging to a particular use case should be stored together. A developer working on product-related functionality should be able to find all relevant files in a single location.

Instead of asking:

_"Is this file a controller, service, or repository?"_

the project is organized around the question:

_"Which business functionality does this file belong to?"_

#### Layered Architecture Example

A traditional layered application might be organized as follows:

{% code lineNumbers="true" %}
```
src/main/java/com.example.shop

├── controller
│   ├── ProductController.java
│   └── OrderController.java
│
├── service
│   ├── ProductService.java
│   ├── ProductServiceImpl.java
│   ├── OrderService.java
│   └── OrderServiceImpl.java
│
├── repository
│   ├── ProductRepository.java
│   └── OrderRepository.java
│
├── dto
│   ├── ProductDto.java
│   ├── CreateProductRequest.java
│   ├── OrderDto.java
│   └── CreateOrderRequest.java
│
└── entity
    ├── Product.java
    └── Order.java
```
{% endcode %}

To implement a new product-related feature, multiple packages often need to be modified.

#### Vertical Slice Architecture Example

The same application organized using Vertical Slice Architecture may look as follows:

{% code lineNumbers="true" %}
```
src/main/java/com.example.shop

├── features
│
├── product
│   ├── create
│   │   ├── CreateProductController.java
│   │   ├── CreateProductService.java
│   │   ├── CreateProductRequest.java
│   │   └── CreateProductResponse.java
│   │
│   ├── getbyid
│   │   ├── GetProductController.java
│   │   ├── GetProductService.java
│   │   ├── GetProductResponse.java
│   │   └── GetProductMapper.java
│   │
│   └── delete
│       ├── DeleteProductController.java
│       └── DeleteProductService.java
│
├── order
│   ├── create
│   │   ├── CreateOrderController.java
│   │   ├── CreateOrderService.java
│   │   ├── CreateOrderRequest.java
│   │   └── CreateOrderResponse.java
│   │
│   └── getbyid
│       ├── GetOrderController.java
│       ├── GetOrderService.java
│       └── GetOrderResponse.java
│
├── entity
│   ├── Product.java
│   └── Order.java
│
└── repository
    ├── ProductRepository.java
    └── OrderRepository.java
```
{% endcode %}

In this structure, each use case is implemented as an independent vertical slice. When a developer needs to modify the "Create Product" functionality, all related files can be found in a single directory.

#### Advantages of Vertical Slice Architecture

The primary advantage of VSA is improved maintainability. Functionality is localized, making it easier to understand, modify, and test. Developers do not need to search across multiple packages to follow the execution flow of a request.

Another important benefit is reduced coupling. Features become more independent and can evolve separately. This is particularly valuable in large applications developed by multiple teams, where different groups may be responsible for different business areas.

VSA also aligns naturally with the way users and stakeholders think about the system. Business requirements usually describe features such as "create order", "register user", or "cancel reservation", not controllers, services, and repositories. Organizing the source code around use cases therefore leads to a structure that better reflects the actual problem domain.

#### VSA in Spring Boot

A typical request flow in a Spring Boot application using Vertical Slice Architecture might look as follows:

{% code lineNumbers="true" %}
```
HTTP Request
      ↓
GetProductController
      ↓
GetProductService
      ↓
ProductRepository
      ↓
Database
      ↓
GetProductResponse
      ↓
JSON Response
```
{% endcode %}

From the outside, the application behaves exactly the same as a traditional layered application. The difference lies in the organization of the source code. Rather than grouping files by technical responsibility, Vertical Slice Architecture groups them by business functionality. As a result, large projects often become easier to navigate, understand, and maintain over time.
