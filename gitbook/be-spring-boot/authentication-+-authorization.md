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

TODO

## User Authentication - Login

TODO

## User Logout

TODO

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
