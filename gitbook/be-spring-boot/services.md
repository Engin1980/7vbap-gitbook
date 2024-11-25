---
icon: square-4
---

# Services

## Explanation

In Spring Boot, a **service** is a component marked with the `@Service` annotation. It is typically used to encapsulate business logic. Services are part of the service layer in a layered architecture and interact with the repository layer to perform operations on the database. From the opposite side of view, they are used by other services or controllers providing them operations over the model.

A simple service may look like:

import org.springframework.stereotype.Service;

@Service public class UserService { private final UserRepository userRepository;

```java
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

The service uses `userRepository` set in the constructor. The service instance is provided by dependency injection - therefore, the instance is typically created by SpringBoot container and is not created directly. As the SpringBoot container invokes the service constructor, all its parameters must be injectable by dependency injection too.







## New

In Spring Boot, a **service** refers to a class that contains business logic and handles operations or calculations that the application needs to perform. It is a part of the **Service Layer** in the multi-layered architecture pattern (also known as a three-tier architecture), which is commonly used to organize the structure of enterprise applications.

The service layer typically sits between the **Controller** layer (which handles HTTP requests and responses) and the **Repository** layer (which interacts with the database). In Spring Boot, services are usually annotated with `@Service` to indicate that they belong to the service layer and to allow Spring to manage them as **Spring Beans**.

In our project, to give all the services some working framework, will intrododuce two main things:

1. _Service Exceptions_, which only can be invoked by any service - a service must invoke service exception only. A service cannot invoke non-service exceptions.
2. `AppService` abstract class providing functionality for other service implementations.

Note again, that all services must have `@Service` annotation. Once annotated, they can be injected using **dependency injection** (will be shown in REST API implementation).

## Architecture Preparation

### Service Exceptions <a href="#service-exceptions" id="service-exceptions"></a>

The idea here is let any service can invoke only service exception. If some operation in a method of a service may throw another (checked) exception, it must be encapuslated in `try-catch` block and the cause exception must be wrapped inside of some service exception. The following table introduces service exceptions (this list may be extended if required).

| Class               | Explanation                                                                                                                                                                                                                                                                                                    |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| AppServiceException | Abstract service exception class. This is the parent of all service exceptions (in the meaning of inheritance). It has a non-null property `source` defining the original service invoking the exception.                                                                                                      |
| InternalException   | An exception thrown in case of internal service error. This exception is thrown, when some operation, which was expected to be processed correctly, failed. For example, database access has failed as the database is not accessible. From the REST API point of view, this exception maps to HTTP state 500. |
| BadRequestException | An exception thrown in case if the service cannot process the request due to the current state. For example, the url cannot be assigned to the inactive user. From the REST API point of view, this exception maps to HTTP state 400.                                                                          |
| BadDataException    | A more specific BadRequestException. This exception is thrown if the servce cannot process the request due to invalid request data. For example, email is not in the valid format, or required user does not exist. From the REST API point of view, this exception maps to HTTP state 400.                    |

The implementation of exceptions is very simple:

```java
package cz.osu.vbap.favUrls.services.exceptions;

import cz.osu.vbap.favUrls.services.AppService;
import lombok.Getter;

@Getter
public abstract class AppServiceException extends Exception {
  private final AppService source;

  public AppServiceException(AppService source, String message) {
    super(message);
    this.source = source;
  }

  public AppServiceException(AppService source, String message, Throwable cause) {
    super(message, cause);
    this.source = source;
  }
}
```

```java
public class InternalException extends AppServiceException {
  public InternalException(AppService source, String message, Throwable cause) {
    super(source, message, cause);
  }
}
```

```java
public class BadRequestException extends AppServiceException {
  public BadRequestException(AppService service, String message) {
    super(service, message);
  }
}
```

```java
// different parent here!
public class BadDataException extends BadRequestException {
  public BadDataException (AppService service, String message) {
    super(service, message);
  }
}
```

### AppService class <a href="#appservice-class" id="appservice-class"></a>

`AppService` abstract class is a parent class for all services. It is used also as a parent type for the `source` property in the `AppServiceException`.

```java
package cz.osu.vbap.favUrls.services;

import cz.osu.vbap.favUrls.lib.ArgVal;
import cz.osu.vbap.favUrls.services.exceptions.InternalException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class AppService {

  @Getter
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected void tryInvoke(Runnable runnable) throws InternalException {
    ArgVal.notNull(runnable, "runnable");
    try {
      runnable.run();
    } catch (Exception e) {
      logger.error("Error in 'tryInvoke'", e);
      throw new InternalException(this, "Error in 'tryInvoke'", e);
    }
  }

  protected <T> T tryInvoke(Supplier<T> supplier) throws InternalException {
    T ret;
    ArgVal.notNull(supplier, "supplier");
    try {
      ret = supplier.get();
    } catch (Exception e) {
      logger.error("Error in 'tryInvoke'", e);
      throw new InternalException(this, "Error in 'tryInvoke'", e);
    }
    return ret;
  }  
}
```

The class contains a logger (line 13/14), which is also published via getter as public. The reason to publish the logger outside is its usage in `AppServiceAspect` defined above.

The class also contains two complex overloads of a method `tryInvoke()`. The purpose of this method is to easily encapsulate the call of a content, which can throw an exception - typically a database operation. It allows the descendant to avoid the complex `try-catch` sequence and potentional incorrect exception handling (see below). The upper overload (lines 16-24) does not return a value, the lower overload (lines 26-36) is a generic method returning a value specified by a generic parameter.

```java
// original method call without "tryInvoke()"
// a programmer must catch the exception on his/her own
// moreover, he may throw incorrect exception
public void deleteCustom(int urlId) throws AppServiceException {
  try {
    urlRepository.deleteById(urlId);
  } catch (Exception e) {
    throw new InternalException(this, "Error in 'deleteCustom'", e);
  }
}
```

```java
// improved behavior
// invocation of "dangerous" method using "tryInvoke()"
// not try-catch needed
// invocation is passed as a lambda
public void delete(int urlId) throws AppServiceException {
  tryInvoke(() -> urlRepository.deleteById(urlId));
}
```

The more examples will be seen once the services are implemented.

## UrlService

As the first one, we will implement a service supporting Url operations - create, get by user, and delete. This service provides only a basic logic over the repository.

{% hint style="info" %}
Note that typically you first create REST API endpoint. Then, with respect to the features required by the endpoint, you create required methods in service class.
{% endhint %}

Let's create a new `UrlService` class in `src/main/.../services`:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.services;

// ...

@Service
public class UrlService extends AppService {

  @Autowired
  private UrlRepository urlRepository;
  @Autowired
  private AppUserRepository appUserRepository;

  public Url create(int appUserId, String title, String address) throws AppServiceException {
    AppUser appUser = tryInvoke(() -> appUserRepository.findById(appUserId))
            .orElseThrow(() -> new BadDataException(this, "User not found."));

    Url url = new Url(appUser, title, address);
    tryInvoke(() -> urlRepository.save(url));
    return url;
  }

  public void delete(int urlId) throws AppServiceException {
    tryInvoke(() -> urlRepository.deleteById(urlId));
  }

  public List<Url> getByUser(int appUserId) throws AppServiceException {
    AppUser appUser = tryInvoke(() -> appUserRepository.findById(appUserId))
            .orElseThrow(() -> new BadDataException(this, "User not found."));
    List<Url> ret = tryInvoke(() -> urlRepository.findByAppUser(appUser));
    return ret;
  }
}

```
{% endcode %}

The code is quite simple. Main notes:

* the `UrlService` class inherits from `AppService` class, so we have the logger and `tryInvoke()` functionality available - line 6;
* two repositories - for `Url` (line 9) and `AppUser` (line 11) entities are injected by `@Autowire`;
* `create(...)` will try to obtain an user from the repository. If the user has not been found, `BadRequest` exeception is invoked. If the user was found, new `Url` entity is created and stored;
* similarly, the rest of methods are implemented.

## More Services

More services can be created on request.

One more complex service is `AuthenticationService` created in the chapter aiming at _Security_.

