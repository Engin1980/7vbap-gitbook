---
icon: square-5
---

# Authentication + Authorization

Here, we will implement a user authentication and authorization mechanism in the backend.

To do so, we will need several new classes and approaches:

* with respect to the endpoints handling and management:
  * `AppUserView` as a DTO object used to transfer user info to front-end;
  * `AppUserController` as a controller with endpoints managing user registration, login, logout and token refresh;
* used for authentication and authorization:
  * `JwtTokenUtil` class to provide basic operations over JWT token;
  * `AuthenticationJwtFilter` as a layer providing JWT authorization check on incoming requests;
  * `AppUserDetails` class as an internal implementation for Spring Boot Security mechanism;
  * `AuthenticationService` as a service at the business layer providing support for register/login/logout and token refresh operations;
* exceptions:
  * `InvalidOrExpiredCredentialsException` used at `AuthenticationService` to process invalid login data;
  * `ForbiddenException` used at `UrlController` (or in any controller in general) to cancel requests not having required permissions.

{% hint style="info" %}
We also realised that we will not need token type in `Token` entity. Therefore, the attribute has been removed the entity and all the related references were updated.
{% endhint %}

## User Registration

To implement user registration, we need to do several things:

* implement an end point for incoming requests;
* implement a service registering user, including password security protection;
* choose a secure password algorithm and provide its implementation;

### End point - Register an user

To create the registration, we will need:

* a controller handling defining the registration endpoint,
* a service handling the request,
* a bean providing us an object responsible for password hashing,
* a DTO for the returned data about the user.

#### AppUserView (DTO)

Firstly, we create a simple DTO object returning data from `AppUser` entity:

```java
package cz.osu.vbap.favUrls.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.vbap.favUrls.model.entities.AppUser;
import lombok.Data;

@Data
public class AppUserView {
  @JsonIgnoreProperties({"urls", "tags", "tokens", "passwordHash"})
  private static class MixIn{
  }

  public static AppUserView of(AppUser appUser) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(AppUser.class, MixIn.class);
    AppUserView ret = mapper.convertValue(appUser, AppUserView.class);
    return ret;
  }

  private int appUserId;
  private String email;
}
```

Note ve have to use `MixIn` class as we need opt out reference properties to urls, tokens, tags and also do not process the password hash.

#### AppUserController

Firstly, we create a new class, `AppUserController` providing the end points used for registration.

{% hint style="info" %}
Note that the registration end point is not protected by Spring Boot Security. See the end of the previous chapter or the definition in the `SecurityConfig` class.
{% endhint %}

```java
// ...

@RestController
@RequestMapping("/v1/appUser")
public class AppUserController {
  @Autowired
  private AuthenticationService authenticationService;

  @PostMapping
  public AppUserView register(String email, String password) throws AppServiceException {
    AppUser appUser = authenticationService.register(email, password);
    AppUserView ret = AppUserView.of(appUser);
    return ret;
  }
}
```

To register a new user, we need an email and a password. Then, we just simpyl forward those information to the relevant service.

Once the registration succeeded, we return DTO of `AppUser` entity as a result, containing only id and email of the user (not the password hash or the other attributes).

#### PasswordEncoder (bean)

As next, we will define a password encoder bean - this allows later injection of an instance of this class via dependency injection. This instance will be used in `AuthenticationService`.

In the `SecurityConfiguration` class, we declare a new function returning `PasswordEncoder` and decorate this method with a `@Bean` tag. From now, whenever the instance of `PasswordEncoder` is required using `@Autowired`, the result of this method will be provided.

```java
// ...

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  
  // ...
}
```

{% hint style="info" %}
We have chosen `bcrypt` password function as a hashing function, as it is secure enough and implemented by default in Spring Boot.

Before chaning the hashing function, use the study sources (e.g., see the last section of this chapter) and ensure that your selection is safe enough to not allow a security breach to your application.
{% endhint %}

#### AuthenticationService

The last part is `AuthenticationService` class responsible for the stuff related with registration, log in, log out, etc.

{% hint style="info" %}
It is common to separate `UserService` handling user management (activate/deactivate, return list of all users, filtering, ...) from `AuthenticationService` handling security stuff (password hashing, security token management, ...).
{% endhint %}

We create this class in the directory with other services:

```java
package cz.osu.vbap.favUrls.services;

// ...

@Service
public class AuthenticationService extends AppService {
  @Autowired
  private JwtTokenUtil jwtTokenUtil;
  @Autowired
  private TokenRepository tokenRepository;
  @Autowired
  private AppUserRepository appUserRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  public AppUser register(String email, String password) throws AppServiceException {
    if (tryInvoke(() -> appUserRepository.findByEmail(email)).isPresent())
      throw new BadRequestException(this, "User already exists.");

    AppUser user = new AppUser(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    tryInvoke(() -> appUserRepository.save(user));

    return user;
  }
}
```

Here, we simply try to get if user is not already existing in the database. If so, we return a bad request. Otherwise, a new user is created with the necoded password and the whole user is stored into the database.

At the end, a new user is returned from the service (containing the newly assigned `appUserId`).

## User Authorization

Next part is user authorization. That means, how Spring Boot validates if there is a currently logged user.

In our approach, we expect that the _access token_ is represented by JWT (JSON Web Token) containing information about the logged user is stored in _http-only_ cookie, which is automatically send during any request. So, the Spring Boot security mechanism should look for this cookie, extract the token, validate its validity, and if the token is valid, it should inject the user info into the Spring Boot Security mechanism.

{% embed url="https://jwt.io/" %}
More info about JWT
{% endembed %}

To do so, we need to:

* Implement `JwtTokenUtil` class which helps us with the stuff regarding the JWT token.
* Implement `AuthenticationJwtFilter` , which will be added in the request processing queue. It looks and validates the JWT token. If the validation is successfull, it injects the info about the user into Spring Boot Security.
* Add the filter into the request processing queue.

### JwtTokenUtil

#### JWT

JWT (JSON Web Token) is a compact, URL-safe token format commonly used for securely transmitting information between parties. It’s widely used in web applications for authentication and authorization purposes. A JWT consists of three parts:

1. **Header**: Specifies the token type (JWT) and the signing algorithm (e.g., HMAC, SHA-256).
2. **Payload**: Contains the **claims**, which are statements about an entity (usually the user) and additional metadata. Claims might include user ID, roles, and permissions. Additionaly, it contains some times; most important are _nbf_ - _not before_ and _exp_ - _expires at_ defining the time window when the token is valid.
3. **Signature**: A cryptographic signature generated from the header, payload, and a secret key. This ensures that the token has not been altered.

In practice, JWTs are popular because they are stateless, which means they don’t require server-side storage for validation, making them scalable for distributed systems. However, JWTs should be securely managed and have limited lifespans to prevent misuse if intercepted.

#### JWT Dependencies

There is no internal implementation for JWT operations. To work with JWT, we need to add a dependency. The most common used one for JWT is "JJWT":

```xml
<!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

#### Configuration Settings

To work with tokens, we will use refresh-access token pattern. To encrypt the token, we will also need a key. For now, we store all those values in the `application.properties` file:

```properties
# security
app.security.privateKey=thisKeyShouldBeStoredInOperatingSystemEnvironmentVariable
app.security.accessTokenExpirationSeconds=60
app.security.refreshTokenExpirationSeconds=1800
```

{% hint style="info" %}
Note that private key **should not** be stored in `application.properties` file as this file is typically stored in the repository. In this case, source code leak will compromise the security of the application.

Typically, keys should be stored in secret vaults or environtment variables.
{% endhint %}

{% embed url="https://www.descope.com/blog/post/access-token-vs-refresh-token" %}
Access vs Refresh Tokens
{% endembed %}

#### JwtTokenUtil

As a next step, we will create a class providing basic token operations. This class will be stored in `.../src/security/` folder and will be named `JwtTokenUtil`:

```java
// ...

@Component
public class JwtTokenUtil extends AppService {
  @Value("${app.security.privateKey}")
  private String secretKey;
  @Value("${app.security.refreshTokenExpirationSeconds}")
  private int refreshTokenExpirationInSeconds;
  @Value("${app.security.accessTokenExpirationSeconds}")
  private int accessTokenExpirationInSeconds;
  private static final String APP_USER_ID_CLAIM_NAME = "appUserId";

  // ...
}
```

Note that the whole class is defined as a `@Component` - so it can use injected values from properties (`@Value` annotations) and also itself can be injected when required.

Now, we will explain its content methods part by part. Let's start with the token creation.&#x20;

```java
public String generateAccessToken(String refreshToken) {
  String email = extractUsername(refreshToken);
  int appUserId = getAppUserId(refreshToken);
  String ret = generateToken(email, appUserId, accessTokenExpirationInSeconds);
  return ret;
}

public String generateRefreshToken(String email, int appUserId) {
  String ret = generateToken(email, appUserId, refreshTokenExpirationInSeconds);
  return ret;
}

private String generateToken(String userName, int appUserId, int expirationInSeconds) {
  Map<String, Object> claims = new HashMap<>();
  claims.put(APP_USER_ID_CLAIM_NAME, appUserId);
  String ret = Jwts.builder()
          .claims(claims)
          .subject(userName)
          .issuedAt(new Date(System.currentTimeMillis()))
          .expiration(new Date(System.currentTimeMillis() + 1000L * expirationInSeconds))
          .signWith(getSignKey())
          .compact();
  return ret;
}

private SecretKey getSignKey() {
  byte[] keyBytes = Decoders.BASE64.decode(secretKey);
  return Keys.hmacShaKeyFor(keyBytes);
}
```

Here we have four methods:

* The first and second ones are public and used to generate a token. The _refresh_ token needs user info. The _access_ token needs valid refresh token and extracts the user info from its content.
* The fourth one is used to convert the key from `String` format to `byte[]` array and finally to `Key` instance.
* The third one is creating a token. It
  * Defines a custom claim with `appUserId`.
  * Sets other primary claims - subject (definition of the logged user, e-mail in our case), issue and expiration date (we do not use `nbf` claim as we need let the token is valid immediatelly)
  * Sign the data with the key.
  * Compact the token into the `String` representation.

The next part is token decoding and validation. Let's add another functions:

```java
public boolean isValid(String token) {
  if (token == null || token.isEmpty()) return false;
  try {
    extractAllClaims(token);
    return true;
  } catch (ExpiredJwtException e) {
    return false;
  }
}

public int getAppUserId(String jwt) {
  Claims claims = extractAllClaims(jwt);
  return (int) claims.get(APP_USER_ID_CLAIM_NAME);
}

public String getSubject(String jwt) {
  return this.extractUsername(jwt);
}

private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
  final Claims claims = extractAllClaims(token);
  return claimsResolver.apply(claims);
}

private Claims extractAllClaims(String token) {
  return Jwts
          .parser()
          .verifyWith(getSignKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
}

private String extractUsername(String token) {
  return extractClaim(token, Claims::getSubject);
}

private Date extractExpiration(String token) {
  return extractClaim(token, Claims::getExpiration);
}

private Boolean isTokenExpired(String token) {
  return extractExpiration(token).before(new Date());
}
```

Here we have several functions:

* `isValid()` checks the validity of the JWT. If the token is empty or null, it is invalid. Then, if token claims **cannot be extracted**, the token is invalid or expired.
* `getAppUserId()` and `getSubject()` returns user's id and e-mail.
* `extractAllClaims()` extract all token info using dependecies.
* The other functions just simply extract claims from the token data.

### AuthenticationJwtFilter

The second step is the filter, which will be inserted among the other filters processing the requests.

{% hint style="info" %}
**Request Filters** in Spring Boot are used to intercept and process HTTP requests and responses. Filters can modify request and response headers, log request details, perform authentication, or add cross-cutting concerns like security and logging before or after reaching the controller layer. In Spring Boot, filters are implemented by creating classes that implement the `Filter` interface or by using the `@Component` annotation to mark a filter as a Spring-managed bean.
{% endhint %}

To create a filter, create `AuthenticationJwtFilter`in the `security` folder in the app:

{% code lineNumbers="true" %}
```java
// ...

@Component
public class AuthenticationJwtFilter extends OncePerRequestFilter {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final String APP_USER_ID_REQUEST_ATTRIBUTE_NAME = "__appUserId";

  private enum TokenState {
    NO_TOKEN,
    VALID,
    INVALID,
    ERROR
  }

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationJwtFilter.class);

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain)
          throws ServletException, IOException {

    logger.debug("AuthenticationJwtFilter invoked");

    String jwt = tryExtractJwtFromRequest(request);
    TokenState state;
    if (jwt == null || jwt.isEmpty()) {
      state = TokenState.NO_TOKEN;
    } else if (jwtTokenUtil.isValid(jwt)) {
      state = TokenState.VALID;
    } else {
      state = TokenState.INVALID;
    }

    if (state == TokenState.VALID) {
      try {
        processValidToken(request, jwt);
      } catch (Exception ex) {
        logger.error("Failed to process authentication procedure: {}", ex.toString());
        state = TokenState.ERROR;
      }
    }

    logger.info("JWT for {} is : {}", request.getRequestURL(), state);

    switch (state) {
      case ERROR:
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        break;
      case INVALID:
      case VALID:
      case NO_TOKEN:
        filterChain.doFilter(request, response);
        break;
      default:
        throw new java.lang.EnumConstantNotPresentException(TokenState.class, state.toString());
    }
  }

  private void processValidToken(HttpServletRequest request, String jwt) {
    String email = jwtTokenUtil.getSubject(jwt);

    AppUserDetails userDetails = new AppUserDetails(email);
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    int appUserId = jwtTokenUtil.getAppUserId(jwt);
    request.setAttribute(APP_USER_ID_REQUEST_ATTRIBUTE_NAME, appUserId);
  }

  private String tryExtractJwtFromRequest(HttpServletRequest request) {
    String ret = null;
    if (request.getCookies() != null) {
      ret = Arrays.stream(request.getCookies())
              .filter(q -> q.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
              .findFirst()
              .map(q -> q.getValue()).orElse(null);
    }
    return ret;
  }
}

```
{% endcode %}

The whole class is more-less straighforward. The most important behavior is in the `processValidToken` function at lines 66+. Here we:

* extract the e-mail of the user - line 67,
* create a new instance of `AppUserDetails` containing the user info (see hint box below) - line 69 and create Spring-Boot-Security authentication token (lines 70-74).
* set the create user info into the Spring Boot Security context (line 76). From now, Spring Boot will know that there is some authenticated user.

{% hint style="info" %}
Spring Boot uses its own authentication mechanism inside to distinguish the authenticated user. This mechanism uses an instance of the interface `UserDetails`, where all the necessary data are provided. So, we need to create an instance of this interface, fill it with data and pass those data into the Spring Boot Security context.

There are typically two ways how to create an instance of the `UserDetails` interface:

* you create a custom class derived from `UserDetails` and use it, or
* you inherit your user-data-class (`AppUser` in our case) from `UserDetails`, so you can later provide it as an interface implementation.
{% endhint %}

In our case, we stick with the first approach - we will create one additional class used to pass user data into Spring Boot security realm - `AppUserDetails` with again quite simple and straighforward code:

```java
// ...

@Getter
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {
  private final Collection<? extends GrantedAuthority> authorities = new HashSet<>();
  private final String username;
  private final String password = null;
}
```

{% hint style="info" %}
Note that `authorities` collection is typically used to pass roles or other related data to the user account. As we do not use roles in our example, we keep this list empty.
{% endhint %}

### Registering the filter

The last step is to register the filter into the request filter queue, so it is applied. The filter will be registered just before `UsernamePasswordAuthenticationFilter` and the registration is again done in `SecurityConfiguration.securityFilterChain(..)` method:

```java
// ...
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  // ...
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // ...  
    // http.authorizeHttpRequests(...   
    http.addFilterBefore(authenticationJwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
```

## User Authentication - Login

To do the user login, the flow will be as follows:

1. REST API accepts a login request at `AppUserController`.
2. Controller forwards the login request to `AuthenticationService`.
3. Service validates the user. If the user is valid, refresh and access tokens are created. Moreover, refresh token is stored in the database.
4. If user is successfully validated, the controller creates two cookies - for refresh and access tokens and return them in the request
5. Finally, the controller creates a DTO containing user data and returns it to the caller.

TODO add image Imgs/login-schema.png

### Controller update

The respective code of the controller is:

```java
// ...

@RestController
@RequestMapping("/v1/appUser")
public class AppUserController {
  // ...

  private Cookie buildTokenCookie(String name, String value, int expiration) {
    final Cookie ret = new Cookie(name, value);
    ret.setHttpOnly(true);
    ret.setPath("/");
    ret.setMaxAge(expiration);
    ret.setAttribute("SameSite", "Strict");
    return ret;
  }

  // ...

  @PostMapping("/login")
  public AppUserView login(String email, String password, HttpServletResponse response) throws AppServiceException {
    AppUserView ret;
    AuthenticationService.LoginResponse tmp;
    try {
      tmp = authenticationService.login(email, password);

      final Cookie accessTokenCookie = buildTokenCookie(
        ACCESS_TOKEN_COOKIE_NAME, tmp.accessToken(), accessTokenExpirationInSeconds);
      response.addCookie(accessTokenCookie);

      final Cookie refreshTokenCookie = buildTokenCookie(
        REFRESH_TOKEN_COOKIE_NAME, tmp.refreshToken(), refreshTokenExpirationInSeconds);
      response.addCookie(refreshTokenCookie);

      ret = AppUserView.of(tmp.appUser());
    } catch (Exception ex) {
      deleteTokenCookies(response);
      throw ex;
    }

    return ret;
  }  
}
```

A small notes here regarding the `buildTokenCookie()`:

* The path of the cookie should be set to `/`. By default, the path is set with respect to the calling URL, so `/v1/appUser` in our case, and will work only for this (or nested) paths. By setting the path as mentioned above we ensure the cookie will be valid for the whole appliation.
* There were some significant changes how cookies are handled by browsers w.r.t. to their `SameSite` policy. By default, this policy is not set. We set the policy to `Strict` ensuring that only the original site can use the cookie.
* We also set the max cookie age. The content of the cookie (the JWT token) will expire regardless the cookie expiration time. However, by setting the expiration we ensure that the token is automatically deleted from the browser when expires.

{% embed url="https://web.dev/articles/samesite-cookies-explained" %}
Cookies Same-Site attribute explanation
{% endembed %}

### Service update

The next part is the implementation of the login feature in the `AuthenticationService`. We simply add the login function together with a record ecapsulating returned data:

{% code lineNumbers="true" %}
```java
public record LoginResponse(
  String refreshToken, 
  String accessToken, 
  AppUser appUser) {}

public LoginResponse login(String email, String password) throws AppServiceException {
  LoginResponse ret;
  Optional<AppUser> appUserOpt = tryInvoke(() -> appUserRepository.findByEmail(email));

  if (appUserOpt.isEmpty() || !isValidCredentials(appUserOpt.get(), password))
    throw new BadRequestException(this, "Invalid credentials.");

  AppUser appUser = appUserOpt.get();
  String refreshToken = jwtTokenUtil.generateRefreshToken(appUser.getEmail(), appUser.getAppUserId());
  String accessToken = jwtTokenUtil.generateAccessToken(refreshToken);
  try {
    storeRefreshToken(appUser, refreshToken);
  } catch (Exception e) {
    throw new InternalException(this, "Failed to login", e);
  }
  ret = new LoginResponse(refreshToken, accessToken, appUser);

  return ret;
}

private boolean isValidCredentials(AppUser appUser, String password) {
  return passwordEncoder.matches(password, appUser.getPasswordHash());
}

private void storeRefreshToken(AppUser appUser, String refreshToken) {
  // exceptions handled at upper level
  tokenRepository.findByAppUser(appUser).ifPresent(tokenRepository::delete);
  Token token = new Token(appUser, refreshToken);
  tokenRepository.save(token);
}
```
{% endcode %}

Here, we:

* Try to get the user by e-mail (line 6).
* Ensure the user exists and the password is valid (lines 8-9).
* Generate refresh and access tokens (lines 12-13).
* Store the refresh token in the database (lines 14-18).

Note also that we try to delete any previous existing refresh token for the specified user before creating the new one (line 32).

## Refresh Access Token

The next parts are easily built on the previous implementation.

The _access_ _token_ is expected to expire very often. In that case, a client front-end app should ask for a new access token using its valid _refresh token_. To do so, it needs an endpoint.

```java
// ...

@RestController
@RequestMapping("/v1/appUser")
public class AppUserController {
  // ...

  @PostMapping(path = "/refresh")
  public void refreshAccessToken(
          HttpServletRequest request, 
          HttpServletResponse response) throws AppServiceException {
    Optional<Cookie> refreshTokenCookie = Arrays.stream(request.getCookies())
            .filter(q -> q.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
            .findFirst();
    if (refreshTokenCookie.isPresent()) {
      String accessToken = authenticationService.refreshAccessToken(refreshTokenCookie.get().getValue());

      final Cookie accessTokenCookie = buildTokenCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, accessTokenExpirationInSeconds);
      response.addCookie(accessTokenCookie);
    } else {
      deleteTokenCookies(response);
    }
  }
}
```

The controller needs a new REST API for refresh request. To do the refresh, http request and http response are needed.

Firstly, we extract the refresh cookie (if it exists) from the request and ask for a new access token via authentication service. If there is a success, a cookie with the new access token is created.

{% hint style="info" %}
Note that `HttpServletRequest` and `HttpServletResponse` objects are injected into the REST API method via dependency injection.
{% endhint %}

Similarly in service:

```java
// ...

@Service
public class AuthenticationService extends AppService {
  // ...

  public String refreshAccessToken(String refreshToken) throws AppServiceException {
    String ret;

    Optional<Token> tokenOpt = tryInvoke(() -> tokenRepository.findByValue(refreshToken));

    if (tokenOpt.isEmpty() || !jwtTokenUtil.isValid(refreshToken))
      throw new InvalidOrExpiredCredentialsException(this);

    ret = jwtTokenUtil.generateAccessToken(refreshToken);

    return ret;
  }

  // ...
}

```

Here we try to get the token from the token repository. Then we validate, if the token exists and is valid. If so, we generate a new access token.

{% hint style="info" %}
Experiencing "Row Was Updated Or Deleted By Another Transaction"? See "Side Notes" at the end of the chapter.
{% endhint %}

## User Logout

The last part is the user logout. Now, it is simple:

```java
// ...

@RestController
@RequestMapping("/v1/appUser")
public class AppUserController {
  // ...
  
  private void deleteTokenCookies(HttpServletResponse response) {
    final Cookie accessTokenCookie = buildTokenCookie(ACCESS_TOKEN_COOKIE_NAME, null, 0);
    response.addCookie(accessTokenCookie);
    final Cookie refreshTokenCookie = buildTokenCookie(REFRESH_TOKEN_COOKIE_NAME, null, 0);
    response.addCookie(refreshTokenCookie);
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request, HttpServletResponse response) throws AppServiceException {
    Optional<String> optExistingRefreshToken = Arrays.stream(request.getCookies())
            .filter(q -> q.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
            .findFirst().map(q -> q.getValue());

    //TODO resolve if exception changes already set cookies
    deleteTokenCookies(response);

    if (optExistingRefreshToken.isPresent()) {
      authenticationService.logout(optExistingRefreshToken.get());
    }
  }
}
```

In the REST endpoint, we only try to get the refresh token from the cookie. If some token is found, we delete it from the database using the service. Anyway, we always remove the (potential) _access_ and _refresh_ token cookies from the browser by setting their expiration to zero.

In the service, we only simply delete the token from the repository:

```java
// ...

@Service
public class AuthenticationService extends AppService {
  // ...

  private void deleteRefreshToken(String refreshToken) {
    tokenRepository.findByValue(refreshToken).ifPresent(tokenRepository::delete);
  }

  public void logout(String refreshToken) throws InternalException {
    tryInvoke(() -> deleteRefreshToken(refreshToken));
  }
}
```

{% hint style="info" %}
Note that for the simplicity we did not implement here any mechanism to delete expired tokens, which were not logged out explicitly.

However, in the case of a new login, the old _refresh token_ is deleted from the database as there is unique constraint saying "only one token per user". See the "login" implementation in the service.
{% endhint %}

{% hint style="info" %}
Experiencing "Row Was Updated Or Deleted By Another Transaction"? See "Side Notes" at the end of the chapter.
{% endhint %}

## Side Notes

### Row Was Updated Or Deleted By Another Transaction

When you perform an update or delete operation, you may experience this issue. In our example, we need to deal with it when updating or logging out the user. In those operations, we **delete** the existing token.

In React debug mode, all the `useState` updates are invoked twice to ensure the correct behavior in multiple invocations.

{% embed url="https://stackoverflow.com/questions/50819162/why-is-my-function-being-called-twice-in-react" %}
Why Reacti nvokes useEffect twice and how to disable this behavior.
{% endembed %}

If the logout is invoked twice, the request on the BackEnd is also invoked twice, causing that there are two **simultanous** requests to delete the request token entity from the database. As the first request will delete it and the second request is having already deleted token, it complains that "Row (=Entity) was deleted by another transaction".

How to resolve this issue?

#### **Disable React double-useEffect-invoation**

You can follow the link above and disable React _strict_ state, so the invocation will be done only once.

#### **Ensure sequential processing of requests**

Another approach is to ensure that the content of the  `deleteRefreshToken()` method is invoked in a sequence way. If there are multiple simultaneous requests, it will serve the first request, then the second one, then the third one, ...

To do so, you can use Java `synchronize` keyword and locks. The explanation goes beyond the content of this chapter, but for the illustration:

```java
private void deleteRefreshToken(String refreshToken) {
  synchronized (AuthenticationService.class) {
    tokenRepository.findByValue(refreshToken).ifPresent(tokenRepository::delete);
  }
}
```

The `synchronized` block cannot be executed simultaneously.&#x20;

For more info, look for Java locking or `synchronized` keyword:

{% embed url="https://www.w3schools.com/java/ref_keyword_synchronized.asp" %}
Java Synchronized Keyword
{% endembed %}

#### **Use Pure SQL Deletion**

The issue is invoked, because two requests at once are trying to get an entity of the request token and manipulate with it (delete it). Even if we use a simple deletion JPA query:

```java
// ...
public interface TokenRepository extends JpaRepository<Token, Integer> { 
  void deleteByValue(String value);
}
```

The `deleteByValue` will **extract the entity** and then delete it. Therefore, doing this operation simulatenously will cause the entity-deleted-in-another-transaction issue.

The option is to use a simple SQL query, bypassing the entity creation:

```java
// ...
public interface TokenRepository extends JpaRepository<Token, Integer> {
  @Query(value = "delete from Token where value = ?1", nativeQuery = true)
  void deleteByValue(String value);
}
```

Here, we directly delete the data from the database using native SQL query.

{% hint style="info" %}
Be aware how the native SQL queries are constructed and how the arguemtn values are passed to them, as the incorrect usage may lead to susceptibility to **SQL Injection** attacks.
{% endhint %}

## Interesting Links

{% embed url="https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html" %}
How to properly handle authentication
{% endembed %}

{% embed url="https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html" %}
How to properly handle forgotten password
{% endembed %}

{% embed url="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#maximum-password-lengths" %}
How to properly store password
{% endembed %}
