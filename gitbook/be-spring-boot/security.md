---
icon: square-4
description: >-
  In this section, we will cover basic security settings for BE in SpringBoot
  application.
---

# Security

## Setting up Spring Security

To enable security in springboot, you need to do two basic things:

1. Add _Spring Security_ dependency into the project.
2. Create a security configuration class and set-up the configuration properly.

First step is to add a _spring-security_ depenency into the project. The easiest way in the newest IDEA versions are:

1. Open the `pom.xml` maven file
2. Find the `<dependencies>` section.
3. You should see a small label `Edit Starters...` . Press the label to open the starters configuration.
4. In the wizard, select **Spring Security**.
5. Save the file and refresh Maven project if required.

If the starters cannot be edited, you can manually add dependencies into the `dependencies` section of `pom.xml` file:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

The second steps needs to create a new class in the project. The name is arbitrary, but commonly used are `[Web]SecurityConfig[uration]` and add the default content:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(q -> q.disable());
    http.cors(q -> q.disable());
    http.authorizeHttpRequests(q -> q.anyRequest().permitAll());

    return http.build();
  }
}
```
{% endcode %}

Here, we define:

* The class is marked with the annotations - lines 8,9.
* The class has a method marked with a `@Bean` annotation - line 12. This method is the main security method called `securityFilterChain()`. Into this method a `http` parameter used for the further configuration is passed.
* For the beginning, we disable _cross-site-refresh-forgery_ (CSRF/XSRF) security.
* For the beginning, we disable _cross-origin-resource-sharing_ (CORS) security.
* For the beginning, we disable all authentication-based protection.

{% hint style="danger" %}
Note that this settings makes our application **totally UNSECURE** and under any circumstances cannot be used in the production.

By applying these settings is your application vulnerable to common security leaks!
{% endhint %}

We disable all the security protection only for the beginning. Now, we will adjust all of them step by step.

## CSRF

CSRF (Cross-Site Request Forgery) is a security vulnerability where an attacker tricks a user into performing unwanted actions on a website where they're already authenticated. Essentially, it exploits the user's active session to make unauthorized requests, like submitting a form or making a transaction, without users knowledge.

Websites defend against CSRF by using tokens (CSRF tokens), which are unique to each session and must be included in sensitive requests (like form submissions, non-HTTP-GET requests). If the token is missing or incorrect, the server rejects the request.

CSRF protection is crucial to prevent unauthorized actions in web applications, especially ones involving sensitive data.

In Spring boot, CSRF uses CSRF token to prevent CSRF attacks. For REST-API applications (with Java/TypeScript based frontends), it generates a cookie with the token. This token is later expected to be in the HTTP Header during the request. In this example, we will create a CSRF-cookie based security. To do so, we need:

1. Define a custom filter, which checks for the CSRF token; if token is missing, a new token is generated and returned during the request.
2. Define a custom handler resistant to CSRF BREACH attacks.
3. Set-up security to use the filter, handler and appropriate CSRF cookie management.

### CsrfCookieFilter

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public final class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
    csrfToken.getToken(); // Render the token value to a cookie by causing the deferred token to be loaded
    filterChain.doFilter(request, response);
  }
}

```
{% endcode %}

We create a new class `CsrfCookieFilter` extending `OncePerRequestFilter`. This class contains a simple method `doFilterInternal(...)`, which will be processed during every request.

Here we get the current valid CSRF token from `_csrf` attribute in request - line 17. Then, a bit strange call `csrfToken.getToken()` will inject a new `XSRF-TOKEN` cookie into the response (if not already provided in the HTTP Header) - line 18.

Finally, a request processing is forwarded on the following filter - line 19.

### SpaCsrfTokenRequestHandler

```java
package cz.osu.vbap.favUrls.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
  private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
    this.delegate.handle(request, response, csrfToken);
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
      return super.resolveCsrfTokenValue(request, csrfToken);
    }
    return this.delegate.resolveCsrfTokenValue(request, csrfToken);
  }
}

```

Next step is a specific implementation of CSRF token request handler to protect against BREACH attacks.&#x20;

The more detailed explanation of this behavior is beyond the scope of this text. See the links below for further info.

### Updating security configuration

Now, we can enable the CSRF protection in the `SecurityConfiguration` class.

{% code lineNumbers="true" %}
```java
// ...

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(q -> q
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()));
    http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);
    
    http.cors(q -> q.disable());
    http.authorizeHttpRequests(q -> q.anyRequest().permitAll());

    return http.build();
  }
}
```
{% endcode %}

Here, we:

* set up the token repository as cookie token repository (line 8). Moreover, we are setting the cookie to be `httpOnly=false`, co we can read it using JavaScript at the front end;
* set up our custom token handler - line 9;
* add our custom filter among the the other filters, before the `CsrfFilter` class.

From now, the app is CSRF secured. If you have not implemented the CSRF behavior at the front end, you will no more be able to do other requests than HTTP GET. All other requests will return HTTP status 403 - Forbidden.

{% hint style="info" %}
To continue with security, we suggest firstly implement the CSRF protection at front-end, so you can validate the correct behavior. Then continue with the implementation of the next security parts.
{% endhint %}

{% embed url="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html" %}
SpringBoot - CSRF Implementation Description
{% endembed %}

{% embed url="https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html" %}
CSRF Prevention
{% endembed %}

## CORS

CORS (Cross-Origin Resource Sharing) is a security feature implemented by web browsers to control how resources, like APIs or web pages, are accessed from different origins (domains, protocols, or ports). It prevents web pages from making requests to a domain different from the one that served the web page unless explicitly allowed by the server. The key points are:

* **Same-Origin Policy**: By default, browsers block cross-origin requests for security reasons. For example, a page on `https://example.com` can't make AJAX requests to `https://api.anotherexample.com` unless CORS is configured and enabled.
* **CORS Headers**: Servers can allow cross-origin requests by sending specific HTTP headers, such as:
  * **`Access-Control-Allow-Origin`**: Specifies which origin(s) are allowed to access the resource.
  * **`Access-Control-Allow-Methods`**: Lists the allowed HTTP methods (GET, POST, etc.).
  * **`Access-Control-Allow-Credentials`**: Indicates whether credentials like cookies or HTTP authentication are allowed.

## Protecting REST API Endpoints

TODO
