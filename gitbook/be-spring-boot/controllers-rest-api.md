---
icon: square-5
---

# Controllers / REST API

In Spring Boot, a **controller** is a core component of the MVC (Model-View-Controller) architecture that manages the flow of data between the model (business logic and data) and the view (user interface/REST API). Controllers handle HTTP requests from clients, process them (often by interacting with services or repositories), and return appropriate responses, such as data or view templates. Annotated with `@RestController`, they allow developers to define routes and corresponding methods for actions like fetching, updating, or deleting resources. This design makes controllers central to building scalable, maintainable web applications and APIs.

## @RestController

In Spring Boot, a `@RestController` annotation simplifies the development of RESTful web services. It is used to create endpoints that handle HTTP requests and return responses, typically in JSON or XML format. A `@RestController` maps HTTP requests to specific handler methods in the controller class using annotations such as `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, and `@RequestMapping`. These methods process client requests, interact with the service or repository layers for business logic, and return responses.&#x20;

### Creating a controller

A controller is a simple class with two annotations:

* `@RestController` declaring that the class is a REST controller, and
* `@RequestMapping` defining how the controller will be accessed (at which URL).

Then, a method with the required annotations are created inside of the controlle representing the endpoints.

```java
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    // GET endpoint to retrieve all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers(); // Returns a list of users
    }

    // POST endpoint to create a new user
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user); // Creates a new user and returns it
    }

    // ...
}

```

The code above creates a new controller listening at `server/api` with two endpoints, where `server` is the address representing the server - either the name (`www.myserver.com`) or the IP address (`78.128......`).

{% hint style="info" %}
Note that every controller/endpoint must have unique URL. If there are multiple endpoints mapped to the same URL, SpringBoot will show an error and will not start the project.
{% endhint %}

Once the controller class is created, we can continue with controllers endpoints.

### Creating End-Points

An **endpoint** represents and entry point into the application, over which an HTTP request can be invoked. The endpoints are created in a `@RestController` by defining a **HTTP-method** (see the table below)  and the target URL. In no URL is specified for the endpoint, the owning controller's url is used.

<table><thead><tr><th width="162">HTTP  Method</th><th width="216">Spring Boot Annotation</th><th>Common Usage</th></tr></thead><tbody><tr><td>GET</td><td><code>@GetMapping</code></td><td>Used to retrieve data from the server. Often used for fetching resources or querying information (e.g., <code>GET /user/list</code>).</td></tr><tr><td>POST</td><td><code>@PostMapping</code></td><td>Used to submit data to the server, often for creating resources or performing actions (e.g., <code>POST /user</code>).</td></tr><tr><td>PUT</td><td><code>@PutMapping</code></td><td>Used to update an existing resource with the data in the request body (e.g., <code>PUT /user/{id}</code>).</td></tr><tr><td>PATCH</td><td><code>@PatchMapping</code></td><td>Used to make partial updates to an existing resource (e.g., <code>PATCH /user/{id}</code>).</td></tr><tr><td>DELETE</td><td><code>@DeleteMapping</code></td><td>Used to delete a resource (e.g., <code>DELETE /user/{id}</code>).</td></tr></tbody></table>

{% hint style="info" %}
Note that all above mentioned annotations can be replaced with `@RequestMapping(method=RequestMethod....)` annotation.
{% endhint %}

TODO

### Passing data to a controller's endpoints

There are several ways how data can be passed into the controller.

#### Query String

You can pass data to a controller via the query string by using the `@RequestParam` annotation. Query string parameters are key-value pairs appended to the URL after a `?` character.

```
www.server.com/greet?name=John&message=Welcome
```

&#x20;The `@RequestParam` annotation maps these parameters to method arguments in your controller.

```java
@RestController
@RequestMapping("/greet")
public class GreetingController {

    // Endpoint to accept query parameters
    @GetMapping
    public String greet(@RequestParam String name, @RequestParam(defaultValue = "Hello") String message) {
        return message + ", " + name + "!";
    }
}
```

This approach is typical for GET requests for more detailed specification of filters or pagination.

#### Path Variable

You can pass data via part of the URL (also known as a **path variable**) by using the `@PathVariable` annotation. This approach is commonly used when the data being passed is part of the resource's unique identifier (e.g., `/users/{id}`).

```
www.server.com/users/42
```

```java
@RestController
@RequestMapping("/users")
public class UserController {

    // Endpoint to accept a path variable
    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id) {
        return "User ID: " + id;
    }
}
```

This approach is typically used for GET requests returning an item(s) related to the provided ID.

#### HTTP Form Data

Another option is to pass data via HTTP **form data** using the `application/x-www-form-urlencoded` content type. This is the default encoding for HTML forms and is used when submitting data from a form with fields such as text, checkboxes, and other form inputs.

To handle form data in Spring Boot, you can use the `@RequestParam` annotation in your controller to bind the form fields to method parameters.

```java
@RestController
@RequestMapping("/submit")
public class FormDataController {

    // Endpoint to accept form data
    @PostMapping
    public String handleFormData(
            @RequestParam("name") String name,
            @RequestParam("email") String email) {
        // Process the form data
        return "Name: " + name + ", Email: " + email;
    }
}
```

{% hint style="info" %}
Note that this approach is primarily used for posting data from HTML forms. However, using its key-value approach, it is conveniet to use it for small amount of key-value pairs.
{% endhint %}

#### JSON Body

Finally, you can pass data via a **JSON body** by sending a POST/PATCH/PUT request with the content type `application/json`. To handle JSON data in your controller, you typically use the `@RequestBody` annotation to bind the incoming JSON data to a Java object.

```java
@RestController
@RequestMapping("/submit")
public class JsonDataController {

    // Endpoint to accept JSON body data
    @PostMapping(consumes = "application/json")
    public String handleJsonData(@RequestBody User user) {
        // Process the JSON data (the User object is automatically populated from the JSON body)
        return "Received User: " + user.getName() + ", Email: " + user.getEmail();
    }
}
```

Having a simple `User` class with `name` and `email` properties, JSON body will look like:

```json
{
    "name": "John Doe",
    "email": "johndoe@example.com"
}
```

This approach is commonly used when passing more complex or structured data via POST, PATCH or PUT requests.

#### Cookie

A bit specific approach is a usage of cookies. You can access **cookies** sent with HTTP requests using the `@CookieValue` annotation. Cookies are small pieces of data stored by the client (browser or other HTTP client) and sent with every request to the server. They are often used for session management, authentication, or storing user preferences.

```java
@RestController
@RequestMapping("/cookies")
public class CookieController {

    // Endpoint to retrieve a cookie value
    @GetMapping("/get")
    public String getCookie(
      @CookieValue(name = "user_token", defaultValue = "not found") String userToken) {
        return "User Token: " + userToken;
    }
}
```

## UrlController

