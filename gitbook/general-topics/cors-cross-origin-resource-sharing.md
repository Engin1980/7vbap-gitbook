# CORS - Cross-Origin Resource Sharing

## What and Why

Cross-Origin Resource Sharing (CORS) is a security feature implemented in web browsers that controls how resources (pages, images, styles, ...) can be requested from a domain different than the **one serving the web page**. By default, browsers enforce the **same-origin policy**, which restricts web applications from making requests to a different origin (a combination of protocol, domain, and port) to prevent malicious behavior.&#x20;

{% hint style="info" %}
In the context of CORS, an **Origin** refers to the unique combination of a web page’s **scheme (protocol), host (domain), and port**. Together, these three elements define where a request is coming from. For example, `https://example.com:443` has a different origin than `http://example.com:80` because the protocol and port differ, even though the domain is the same. The browser uses this origin value to enforce the **same-origin policy**, meaning that scripts running on one origin cannot freely access resources from another origin unless explicitly permitted. This concept is fundamental to web security, as it prevents unauthorized cross-site interactions while still allowing controlled communication through CORS headers.
{% endhint %}

If CORS were disabled or did not exist, any website could freely make requests to other domains without restriction. This would expose users to **Cross-Site Request Forgery (CSRF)** and **data theft attacks**, where a malicious site could silently send requests to another site where the user is authenticated and steal sensitive information.

For example, imagine you are logged into your online banking account in one browser tab. If CORS did not exist, a malicious website you visit in another tab could automatically send hidden requests to your bank’s API using your active session cookies. The attacker’s site might trigger a transfer of money or request sensitive account details without your knowledge

## **CORS vs. Front/Backend Web Application**

Why is it important w.r.t to the web applications? Imagine that the front-end and back-end share the same domain but differ by port, for example, a React front-end running on `https://shop.com:3000` and a SpringBoot backend API hosted on `https://shop.com:5000`. Even though they share the same domain name (`shop.com`), the **port numbers are different**, which means the browser treats them as **different origins**.

If the browser downloads the front-end app from `https://shop.com:3000`, its origin is `https://shop.com:3000` and the **non-cross** requests can be sent only at `https://shop.com:3000`. But if the frontend makes a request at backend URL `https://shop.com:5000`, it requests a different origin (due to a different port number) and activates the CORS.

## Setting Up

### **Front-end**

On the **front-end side**, you generally cannot “allow” or configure CORS directly because CORS is enforced by the browser and controlled by the **server’s response headers**. The front-end simply makes requests, and the browser checks whether the back-end has permitted them.

### **Back-end**

Allowing CORS on a back-end involves configuring the server to send specific HTTP headers that tell the browser which cross-origin requests are permitted. The most important header is `Access-Control-Allow-Origin`, which should be set to the exact front-end origin (e.g., `http://localhost:3000`) rather than a wildcard in production. In addition, the server must specify allowed HTTP methods with `Access-Control-Allow-Methods` (such as `GET, POST, PUT, DELETE`) and permitted request headers with `Access-Control-Allow-Headers` (like `Content-Type, Authorization`). For requests that include credentials (cookies or authentication tokens), the server must also send `Access-Control-Allow-Credentials: true`. Finally, the back-end should correctly handle **preflight OPTIONS requests**, which browsers automatically send before certain cross-origin calls, by responding with the same CORS headers. This setup ensures that the browser recognizes the back-end as a trusted source and allows the front-end to communicate securely.

#### SpringBoot

In Spring Boot, enabling CORS typically means configuring the server to send the right headers back to the browser. This can be done in a few ways: you can annotate individual controller methods with `@CrossOrigin` to allow specific origins, or you can define a global CORS configuration using a `WebMvcConfigurer` bean. In both cases, you specify which origins, HTTP methods, and headers are permitted. This ensures that when your React front-end (running on a different port) makes requests, the Spring Boot back-end responds with the proper CORS headers so the browser allows the communication.

An example of controller-level CORS setup:

```java
@RestController
@RequestMapping("/api")
public class ProductController {

    // Allow requests from http://localhost:3000
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/products")
    public List<String> getProducts() {
        return List.of("Laptop", "Phone", "Tablet");
    }
}
```

And an example of global CORS setup:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // apply to all endpoints
                        .allowedOrigins("http://localhost:3000") // front-end origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // allowed HTTP methods
                        .allowedHeaders("*") // allow all headers
                        .allowCredentials(true); // allow cookies/auth headers if needed
            }
        };
    }
}
```

#### .NET

In a .NET REST API, enabling CORS is typically done by configuring it in the application’s startup pipeline. You register CORS policies in the service configuration (e.g., `builder.Services.AddCors(...)`) and then apply them in the middleware (`app.UseCors(...)`). Within the policy, you specify which origins, HTTP methods, and headers are allowed so that your front-end (running on a different port) can communicate with the back-end. This ensures the browser receives the proper CORS headers and permits cross-origin requests securely.

```cs
var builder = WebApplication.CreateBuilder(args);

// Register CORS services
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend",
        policy =>
        {
            policy.WithOrigins("http://localhost:3000") // your React front-end origin
                  .AllowAnyMethod()                     // allow GET, POST, PUT, DELETE, etc.
                  .AllowAnyHeader()                     // allow all headers
                  .AllowCredentials();                  // allow cookies/auth headers if needed
        });
});

var app = builder.Build();

// Enable CORS middleware
app.UseCors("AllowFrontend");

// definitions of endpoints or add controllers mapping here
app.MapGet("/api/products", () =>
{
    return new[] { "Laptop", "Phone", "Tablet" };
});

app.Run();
```
