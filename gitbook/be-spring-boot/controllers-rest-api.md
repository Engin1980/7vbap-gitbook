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

### Creating the Endpoints

An **endpoint** represents and entry point into the application, over which an HTTP request can be invoked. The endpoints are created in a `@RestController` by defining a **HTTP-method** (see the table below)  and the target URL.&#x20;

The target endpoint URL is a combination of server's URL, controller's URL (specified in the `@RequestMapping` annotation) and endpoints's URL (specified in `@Get/Post/...Mapping` or `@RequestMapping` annotation). If no URL is specified either at controller's or endpoint's level, the URL of the server is used.

<table><thead><tr><th width="162">HTTP  Method</th><th width="216">Spring Boot Annotation</th><th>Common Usage</th></tr></thead><tbody><tr><td>GET</td><td><code>@GetMapping</code></td><td>Used to retrieve data from the server. Often used for fetching resources or querying information (e.g., <code>GET /user/list</code>).</td></tr><tr><td>POST</td><td><code>@PostMapping</code></td><td>Used to submit data to the server, often for creating resources or performing actions (e.g., <code>POST /user</code>).</td></tr><tr><td>PUT</td><td><code>@PutMapping</code></td><td>Used to update an existing resource with the data in the request body (e.g., <code>PUT /user/{id}</code>).</td></tr><tr><td>PATCH</td><td><code>@PatchMapping</code></td><td>Used to make partial updates to an existing resource (e.g., <code>PATCH /user/{id}</code>).</td></tr><tr><td>DELETE</td><td><code>@DeleteMapping</code></td><td>Used to delete a resource (e.g., <code>DELETE /user/{id}</code>).</td></tr></tbody></table>

{% hint style="info" %}
Note that all above mentioned annotations can be replaced with `@RequestMapping(method=RequestMethod....)` annotation.
{% endhint %}

If endpoint call is completed, typically, a HTTP status code, return body (and optionaly additional data, like cookies) are returned.&#x20;

{% hint style="info" %}
HTTP status codes are three-digit numbers indicating the outcome of an HTTP request. These codes are grouped into categories:&#x20;

* informational (`1xx`),&#x20;
* success (`2xx`),
* redirection (`3xx`),
* client errors (`4xx`) and
* server errors (`5xx`).&#x20;

The most common ones are `200 OK` (successful request), `404 Not Found` (requested resource is unavailable), and `500 Internal Server Error` (something went wrong on the server). Other notable codes include `301 Moved Permanently` for redirection, `400 Bad Request` for malformed requests, `401 Unauthorized` when authentication is required or `403 Forbidden` when authentication is done, but authorization fails (resource is unavailable for the current user).&#x20;
{% endhint %}

{% embed url="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status" %}
HTTP Status Codes
{% endembed %}

The HTTP status code and the result is specified by the return type of the endpoint's method. As we are using`@RestController` annotation, the returning value is automatically wrapped into the JSON. Commonly, there are two options to return the value:

* common type (like `int`, `string` or any class) or `void` - when autowrapping to JSON shoudl be used; moreover, the returned status code is always `200`, or
* `ResponseEntity<T>` when custom return code is required. Here, a programmer specifies the return HTTP code together with the response body to customise the behavior.

{% hint style="info" %}
As we will use exception handling to manage the illegal states during endpoints evaluation, we will stick with the first option and return errorneous HTTP status codes in exception handlers.
{% endhint %}

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

## Exception Handling

This section describes how exceptions invoked during endpoint processing are handled in SpringBoot. In general, you have two options:

* use custom `try-catch` blocks;
* use `@ControllerAdvice`and custom exception handler.

### Catching using try-catch

If you are familiar with the `try-catch` construct, you can always wrap any call inside the endpoint with it and use common exception handling, like:

{% code lineNumbers="true" %}
```java
@PutMapping("/{urlId}")
public ResponseEntity<UrlView> updateUrl(@PathVariable int urlId, 
            String title, 
            String address) throws AppServiceException {
  ResponseEntity<UrlView> ret;
  try {
    Url url = urlService.update(urlId, title, address);
    UrlView tmp = UrlView.of(url);
    ret = ResponseEntity.ok(tmp);
  } catch (BadRequestException ex) {
    ret = ResponseEntity.badRequest().build();
  } catch (Exception ex) {
    return ResponseEntity.internalServerError().build();
  }
  return ret;
}
```
{% endcode %}

In this case, we will return an instance of `ResponseEntity` as we need to handle exceptions and return custom HTTP status codes if required:

* firstly, we try to update the entity and return the entity with `OK` code - lines 7-9;
* if `BadRequestException` is thrown, the response is returning `400 Bad Request`;
* if any other exception is thrown, the response is returning `500 Internal Server Error`.

Although this approach is quite straighforward, it has some disadvantages, mainly:

* you are extending your code with the `try-catch` wrapping blocks;
* you are forced to handle the exception typically using repetitive code in catch blocks;
* the return type `ResponseEntity<UrlView>` will not be sufficient if you need to return some non-UrlView data, as the body is expected tobe stricly the `UrlView` instance and your error info may be a string or a custom error type; therefore, you will have to lift up the type to something like `ResponseEntity<Object>`;
* you will not be able catch issues thrown before the endpoint is invoked - bad typing, conversion errors, etc.

Therefore, the second approach based on `@ControllerAdvice` and custom exception handler is preferred.

### Custom @ControllerAdvice exception handler

In this approach, we create a class. In this class, we create methods, where each method is handling a specific exception type. The code may look like this:

```java
package cz.osu.vbap.favUrls.controllers;

// ...

@ControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorView> badRequestException(BadRequestException e, WebRequest request) {
    ResponseEntity<ErrorView> ret = new ResponseEntity<>(
            new ErrorView(e.getMessage()),
            HttpStatus.BAD_REQUEST);
    return ret;
  }

  @ExceptionHandler(BadDataException.class)
  public ResponseEntity<ErrorView> badDataException(BadDataException e, WebRequest request) {
    ResponseEntity<ErrorView> ret = new ResponseEntity<>(
            new ErrorView(e.getMessage()),
            HttpStatus.BAD_REQUEST);
    return ret;
  }

  @ExceptionHandler(InternalException.class)
  public ResponseEntity<Error> internalServerException(InternalException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Internal service error."),
            HttpStatus.INTERNAL_SERVER_ERROR
    );
    return ret;
  }

  // when URL is not ok
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Error> exception(NoResourceFoundException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Invalid request - path.", null),
            HttpStatus.NOT_FOUND
    );
    return ret;
  }

  // when URL is ok, but data params does not match
  @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Error> exception(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e, WebRequest request) {
    ResponseEntity<Error> ret = new ResponseEntity<>(
            new Error("Invalid request - data.", null),
            HttpStatus.NOT_FOUND
    );
    return ret;
  }

  // ...
}
```

This is a simple class annotated with `@ControllerAdvice` annotation.

Then, we create a function for every catched exception. This function is annotated with `@ExceptionHandler` annotation containing the definition of the captured type. Moreover, this method has the parameter of the same type containing the object of the thrown exception.&#x20;

In the method, we handle the exception. Typically, we log the exception and return the required HTTP status code together with error describing body.

## UrlController - demo implementation

Here, we provide the full implementation of our `UrlController` providing REST API endpoints to work with URLs. The expected endpoints are summarized in the following table:

<table><thead><tr><th width="212">Endpoint</th><th width="118">Params</th><th width="115">Returns</th><th>Description</th></tr></thead><tbody><tr><td>POST /v1/url</td><td>appUserId, title, address</td><td>UrlView</td><td>Creates a new URL and returns it's DTO representation</td></tr><tr><td>GET /v1/url/{appUserId}</td><td>appUserId</td><td>UrlView</td><td>Returns List of DTO of the URLs by ID of the owning user</td></tr><tr><td>DELETE /v1/url/{urlId}</td><td>urlId</td><td>(nothing)</td><td>Deletes the URL by ID</td></tr></tbody></table>

The implementatoin follows. Again, a good practice is to place controllers together - in our case, the target folder will be `/src/main/java/...favUrls/controllers`:

```java
package cz.osu.vbap.favUrls.controllers;

//...

@RestController
@RequestMapping("/v1/url")
public class UrlController  {

  @Autowired
  private UrlService urlService;

  @PostMapping
  public UrlView createUrl(int appUserId, String title, String address) throws AppServiceException {
    Url url = urlService.create(appUserId, title, address);
    UrlView ret = UrlView.of(url);
    return ret;
  }

  @GetMapping("/{appUserId}")
  public Collection<UrlView> getByUser(@PathVariable int appUserId, HttpServletRequest request) throws AppServiceException{

    int loggedAppUserId = (int) request.getAttribute("__appUserId");
    if (loggedAppUserId != appUserId) throw new ForbiddenException();

    List<Url> urls = urlService.getByUser(appUserId);
    List<UrlView> ret = urls.stream()
            .map(UrlView::of)
            .toList();
    return ret;
  }

  @DeleteMapping("/{urlId}")
  public void deleteUrl(@PathVariable int urlId) throws AppServiceException {
    urlService.delete(urlId);
  }
}
```

You can see that:

* most of the endpoints are simply using the service methods to reach required functionality;
* if some error occurs, it is "handled" by throwing the corresponding exception. The exception will be handled by `@ExceptionHandler` in `@ControllerAdvice`.

{% hint style="info" %}
There is a read out of attribute `__appUserId` in the listing. This attribute will be added in every request later, during authorization implementation - for more info, look at the chapter related to "Authentcation & Authorization". Note that this behavior is not the default SpringBoot implementation.
{% endhint %}
