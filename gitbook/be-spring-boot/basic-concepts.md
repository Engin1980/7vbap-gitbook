---
icon: square-3
description: >-
  This chapter explain, how basic implementation patterns are set up for further
  development, namely services and exception management, error handling, logging
  and object mapping.
---

# Basic concepts

In this section, we will explain some basic building concepts used in the rest of the implementation.

## Logging

**Logging** is the practice of recording messages or events generated by an application during its execution. These messages can provide critical insights into the application's behavior, helping developers and system administrators monitor and debug the application.

A log message usually includes several components: a timestamp indicating when the log entry was created, the log level showing the severity of the message, the message content itself describing the event, and any contextual information that provides additional insights, such as thread ID, class name, or user ID.

<table><thead><tr><th width="144">Log Level</th><th>Meaning</th></tr></thead><tbody><tr><td>DEBUG</td><td>Detailed information, typically of interest only when diagnosing problems.</td></tr><tr><td>INFO</td><td>General information about the application's operation (e.g., startup messages, configuration) or about a success.</td></tr><tr><td>WARN</td><td>Indications that something unexpected happened, but the application is able to resolve this issue (e.g. by using some default value) and is still functioning as expected.</td></tr><tr><td>ERROR</td><td>Error events means a part of the aplication crashed or will not work (e.g. missing e-mail module), but the rest of the application is able to continue running.</td></tr><tr><td>FATAL</td><td>Severe error events that might lead the application to abort.</td></tr></tbody></table>

{% hint style="info" %}
Some frameworks use more granular log levels. One of typically used log levels is TRACE/VERBOSE, which is more detailed and low level than debug, used for precise analysis.

The last item, FATAL, is also sometimes marked as CRITICAL.
{% endhint %}

Logs can be output to various destinations. Commonly, they are printed to the console, written to files on disk, sent to centralized logging systems for analysis, or stored in a database for further querying and reporting. Many programming languages offer libraries or frameworks to facilitate logging; for instance, Java has Log4j, SLF4J, Logback, and java.util.logging. We will show an usage of SFL4J.

### Set up Logging using SFL4J + Logback

Logging in Java is divided into an abstraction level and a current implementation. The abstraction is typically represented by SFL4J (Simple Logging Facade 4 Java). For implementation there are several options; however, the default option in Spring Boot is _Logback_.

As our selection is a default option for Spring Boot included in _spring-boot-starter_ dependency, there is no need for additional set up in Maven.

#### Setting up the configuration

To adjust the logging, you should set up the configuration in `application.properties` file:

```properties
...
logging.level.org.springframework.web: DEBUG
logging.level.org.hibernate: INFO
logging.file.name=log/log.txt  # comment this line to disable logging to file
```

#### Creating and usage of the loggers

A logger is an instance of a logging class, which is used to write into the log. To create an instance, a method of a factory class is invoked:

```java
Logger log = LoggerFactory.getLogger(this.getClass());
```

For an instance, a name is typically provided. The name can also be derived from the name of the class. Over this `logger` instance, methods `debug()`, `info()`, `warn()`, `error()` and `critical()` can ve invoked to pass the log record.

```java
log.info("This is a message.");
```

{% hint style="info" %}
The whole problematic is a bit more complicated. To see and understand the exact components of logging in Java, see the link below.
{% endhint %}

{% embed url="https://medium.com/@AlexanderObregon/enhancing-logging-with-log-and-slf4j-in-spring-boot-applications-f7e70c6e4cc7" %}
Introduction to Logging with Spring Boot and Java
{% endembed %}

### Logging + Aspects (AOP)

Aspect-Oriented Programming (AOP) allows the separation of cross-cutting concerns from the main business logic of an application. Cross-cutting concerns are functionalities that affect multiple parts of an application but are not the primary focus of the application, such as logging, security, error handling, transaction management, and performance monitoring.&#x20;

There are five main concepts in AOP:

* **Aspect**: An aspect is a module that encapsulates a cross-cutting concern. It defines a set of behaviors (advice) that can be applied at specific points in the application (join points).
* **Join Point**: A join point is a specific point in the execution of a program where an aspect can be applied. This could be at method entry or exit, exception handling, or even during field access.
* **Advice**: Advice is the action taken by an aspect at a particular join point. There are different types of advice:
  * **Before**: Executed before the join point.
  * **After**: Executed after the join point, regardless of its outcome.
  * **After Returning**: Executed after the join point only if it completes successfully.
  * **After Throwing**: Executed if the join point throws an exception.
  * **Around**: Wraps the join point, allowing you to execute code both before and after the join point, and control whether the join point is executed at all.
* **Pointcut**: A pointcut is an expression that specifies a set of join points where advice should be applied. It allows you to define the conditions under which the advice should be executed.
* **Weaving**: Weaving is the process of integrating aspects with the main codebase. This can occur at different times:
  * **Compile-time**: Aspects are woven into the code during compilation.
  * **Load-time**: Aspects are woven into the code when the classes are loaded into the JVM.
  * **Runtime**: Aspects are woven into the code while the application is running.

In our project, we will show how use AOP for logging the method invocation and result. We will log invocation of every method in any service and in any controller.

#### Adding AOP support to project

To support AOP, a dependency must be added to `pom.xml` Maven file:

```xml
<!-- for AOP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

#### Add Aspect for Controllers

Lets create a new class at `.../src/main/cz.osu.vbap/favUrls/lib/aop` named `ControllerAspect` with the following code:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.lib.aop;

import cz.osu.vbap.favUrls.services.AppService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ControllerAspect {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Pointcut("execution(* cz.osu.vbap.favUrls.controllers..*(..))")
  public void controllerMethods(){}

  @Before("controllerMethods()")
  public void logBefore(JoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() invoked with arguments: {}", className, methodName, methodArgs);
  }

  @AfterReturning(pointcut = "controllerMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() completed with arguments: {} and result: {}", className, methodName, methodArgs, result);
  }

  @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.error("AOP-C:: {}.{}() failed with arguments: {}", className, methodName, methodArgs, exception);
  }
}
```
{% endcode %}

Important notes:

* Every aspect must be introduced with `@Aspect` and `@Component` annotations to be active.
* A default logger is created for this aspect - line 15.
* A point cut defines, when the aspect is active - line 17. Our point cut activates the aspect for every class in the defined package.
* Three advices are created. The first advice defines when is it active (line 20) and a joint point parameter argument with the invocation context.

#### Add Aspect for AppServices

In our project, all business logic services will be inherited from a base class `AppService`. We will create a new aspect and configure it to be invoked when any method in of any `AppService` descendant is executed.

Lets create a new class at `.../src/main/cz.osu.vbap/favUrls/lib/aop` named `AppServiceAspect` with the following code:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.lib.aop;

import cz.osu.vbap.favUrls.services.AppService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ControllerAspect {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Pointcut("execution(* cz.osu.vbap.favUrls.controllers..*(..))")
  public void controllerMethods(){}

  @Before("controllerMethods()")
  public void logBefore(JoinPoint joinPoint) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() invoked with arguments: {}", className, methodName, methodArgs);
  }

  @AfterReturning(pointcut = "controllerMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.info("AOP-C:: {}.{}() completed with arguments: {} and result: {}", className, methodName, methodArgs, result);
  }

  @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    String methodArgs = Arrays.toString(joinPoint.getArgs());
    logger.error("AOP-C:: {}.{}() failed with arguments: {}", className, methodName, methodArgs, exception);
  }
}

```
{% endcode %}

Code is very similar. There are two significant defferences:

* The point cut a line 17 is different, now aimed at the all descendants of the (not yet existing) `AppService` class.
* &#x20;The only difference is that the logger is taken from the invocating source - a descendant of `AppService` class. `AppService` defines the logger of the service, and we use this logger for logging.

{% hint style="info" %}
Note that as the class `AppService`does not exist yet; the project with `AppServiceAspect` class is not now compilable.

To overcome this issue, you can comment this whole file and uncomment it once the `AppService` class is created.
{% endhint %}

## Service

In Spring Boot, a **service** refers to a class that contains business logic and handles operations or calculations that the application needs to perform. It is a part of the **Service Layer** in the multi-layered architecture pattern (also known as a three-tier architecture), which is commonly used to organize the structure of enterprise applications.

The service layer typically sits between the **Controller** layer (which handles HTTP requests and responses) and the **Repository** layer (which interacts with the database). In Spring Boot, services are usually annotated with `@Service` to indicate that they belong to the service layer and to allow Spring to manage them as **Spring Beans**.

In our project, to give all the services some working framework, will intrododuce two main things:

1. _Service Exceptions_, which only can be invoked by any service - a service must invoke service exception only. A service cannot invoke non-service exceptions.
2. `AppService` abstract class providing functionality for other  service implementations.

Note again, that all services must have `@Service` annotation. Once annotated, they can be injected using **dependency injection** (see below).





### Service Exceptions

The idea here is let any service can invoke only service exception. If some operation in a method of a service may throw another (checked) exception, it must be encapuslated in `try-catch` block and the cause exception must be wrapped inside of some service exception. The following table introduces service exceptions (this list may be extended if required).

<table><thead><tr><th width="239">Class</th><th>Explanation</th></tr></thead><tbody><tr><td>AppServiceException</td><td>Abstract service exception class. This is the parent of all service exceptions (in the meaning of inheritance). It has a non-null property <code>source</code> defining the original service invoking the exception.</td></tr><tr><td>InternalException</td><td>An exception thrown in case of internal service error. This exception is thrown, when some operation, which was expected to be processed correctly, failed. For example, database access has failed as the database is not accessible. From the REST API point of view, this exception maps to HTTP state 500.</td></tr><tr><td>BadRequestException</td><td>An exception thrown in case if the service cannot process the request due to the current state. For example, the url cannot be assigned to the inactive user. From the REST API point of view, this exception maps to HTTP state 400.</td></tr><tr><td>BadDataException</td><td>A more specific BadRequestException. This exception is thrown if the servce cannot process the request due to invalid request data. For example, email is not in the valid format, or required user does not exist. From the REST API point of view, this exception maps to HTTP state 400.</td></tr></tbody></table>

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

### AppService class

`AppService` abstract class is a parent class for all services. It is used also as a parent type for the `source` property in the `AppServiceException`.

{% code lineNumbers="true" %}
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
{% endcode %}

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

## Repository

Repositories has been already introduced in the previous chapter.

In Spring Boot, a **repository** is a class or interface responsible for handling data access logic, often using a framework like **Spring Data JPA** to interact with databases. Repositories are part of the **Data Access Layer** in a typical three-layered architecture (Controller-Service-Repository) and focus solely on database operations such as querying, saving, updating, and deleting entities.

Note, that every repository is automatically annotated with `@Repository` tag. Similarly to services, this tag ensures that the repository can be injected using **dependency injection** (see below).

## Dependency Injection

D**ependency injection (DI)** is a design pattern used to achieve **Inversion of Control (IoC)**, which means that the responsibility of managing object creation and their dependencies is transferred to the underlying framework instead of being handled manually within the code. This helps to decouple different components of the application, making it more modular, easier to test, and maintain. Simply say, a programmer is not calling a constructor of a class, but the instance is somehow provided to him/her, already constructed.

When dependency injection is used, objects (dependencies) are injected into the classes that need them, typically through _constructors_, _setter methods_, or _directly into fields_. Spring Boot manages the lifecycle of these objects (also known as beans) and injects them where needed.

When a class (let's call it a **service**) requires another object (say, a **repository**) to function, instead of creating that object manually inside the class, you declare the dependency in the constructor or as a field, and Spring will automatically inject the required instance at runtime. Spring scans the application for components marked with annotations:

* `@Service`, `@Component`, `@Repository` as **class** annotations, or
* `@Bean` as a **method** annotation inside of class marked with `@Configuration`, `@SpringBootApplication`, `@Component`, `@Service`, `@Repository`and some other.

From those annotations, Spring Boot creates the necessary objects (beans) to inject where needed.

Simple example follows:

```java
@Service
public class MyService {
  // ...
}
```

```java
@Configuration
public class Config{
  @Bean
  public PasswordManager passwordManager() {
    // ...
  }
}
```

```java
@RestController
public class Controller{
  @Autowired private MyService myService;
  @Autowired private PasswordManager passwordManager;
}
```

The first code listing shows how a class `MyService` is annotated. From now, it will be available via DI.

The second code listing shows how a bean of type `PasswordManager` is provided. From now, it will be available via DI.

The last code listing shows how the values are injected into private fields of another class.&#x20;

{% hint style="info" %}
`@Autowired` tells the enviroment to fill in the instance. There are three options to use:

* Join `@Autowired` with setter - generally not recommended.
* Join `@Autowired` with private field - as seen above. It is very easy and straighforward usage, however still the third option is preferred.
* Join `@Autowired` with the argument of the constructor; the value is the injected directly into the constructor, and is later stored in a private field. Although this option is more verbose,  it is preferred.
{% endhint %}

## RestController

## DTO - Data Transfer Object

A **Data Transfer Object (DTO)** is a design pattern used to transfer data between different layers of an application, such as between the client and server or between different system modules. The DTO's primary role is to encapsulate and transfer data without exposing the internal details of the application's entities or models. DTOs are usually simple objects that only contain data, typically structured with getters and setters, without any business logic. By using DTOs, you can decouple domain models from the data being transferred, which keeps the presentation layer separate from underlying data structures or the database schema.

In addition to separating concerns, DTOs are often used to optimize data transfer by allowing developers to select only the fields necessary for a particular operation, which reduces the amount of data transmitted. This can improve performance by preventing the transfer of unnecessary or sensitive information, while also providing a security advantage by controlling which parts of the internal data are exposed.

In our project, we will use DTOs to transfer the data between Spring Boot backend and the front end part of the project. So, at the BE, we will convert the entity data into the DTO to transfer them to FE.

As DTO class has typically very similar structure as Entity, it is advantegous to use some kind of automatic mechanism controlling the conversion instead of writing a custom one. Such techniques are called _object mapping_.

There are several tools available for the object mapping, we will use **Object Mapper**.

### Configuring the Object Mapper

Firstly, we need to add a required dependency for _Object Mapper_ into the `pom.xml` file:

```xml
<!-- for simple transformation between objects -->
<dependency>
  <groupId>org.modelmapper</groupId>
  <artifactId>modelmapper</artifactId>
  <version>3.2.0</version>
</dependency>
```

### Simple object mapping

Imagine we have two similar classes - one entity and one transfering class:

{% tabs %}
{% tab title="AppUser (Entity)" %}
```java
@Entity
public class AppUser {
  @Id
  private int appUserId;
  private String email;
  private String passwordHash;
  private boolean active;
}
```


{% endtab %}

{% tab title="AppUserView (DTO)" %}
```java
@Data
public class AppUserView {
  private int appUserId;
  private String email;
  private boolean active;
}
```

Note the Lombok `@Data` annotation.
{% endtab %}
{% endtabs %}

We need to do a simple "copy" of the data in entity into the DTO. To do so, we will use _Object Mapper_:

```java
public static AppUserView convert(AppUser appUser) {
  ObjectMapper mapper = new ObjectMapper();
  AppUserView ret = mapper.convertValue(appUser, AppUserView.class);
  return ret;
}
```

The code is simple. Firstly we create a new instance of `ObjectMapper`. Then, we invoke a simple  method `convertValue` with first argument as a source, and second argument as a target class.&#x20;

### Custom mapping handling

In many cases, there can be an issue with the simple copy of property from the source into the target. For example:

* If the source entity contains fields, which should not be included in the target DTO.
* If the source entity contains another entity (or a list of other entities), which needs to use their own custom mapping to respective DTOs.

To do so, we need to define that some source fields should be ignored and map them manually later.

For example, lets extend the previous Entity class:

```java
@Entity
public class AppUser {
  // ...
  @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
  private Set<Token> tokens = new HashSet<>();
}
```

Now, the mapping will try to map `tokens` into the target DTO. So, we need to specify to ignore `tokens` field. To do so, additional class must be created - the purpose of this class is only to have an annotation specifying the behavior; it will have no meaningful content. This class is called _a mixin_:

```java
@JsonIgnoreProperties({"tokens"})
static class AppUserMixIn{}
```

This mixin defines that "tokens" field/variable should be ignored during the mapping. How, we have to specify the mixin usage during the mapping:

{% code lineNumbers="true" %}
```java
public static AppUserView convert(AppUser appUser) {
  ObjectMapper mapper = new ObjectMapper();
  mapper.addMixIn(AppUser.class, AppUserMixIn.class);
  AppUserView ret = mapper.convertValue(appUser, AppUserView.class);
  return ret;
}
```
{% endcode %}

Note the order of the parameters at line 3 is important:

* The first parameter defines the relative class - `AppUser` entity in our example.
* The second parameter defines the mixin class with additional annotation - `AppUserMixIn`.

With a simple templating, we can build all the DTOs to have the same similar behavior, like:

{% code lineNumbers="true" %}
```java
// ...
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.eng.taskGrading.model.entities.AppUser;
import lombok.Data;
import java.util.List;

@Data
public class AppUserView {

  @JsonIgnoreProperties({"tokens"})
  static class MixIn{
  }

  public static AppUserView from(AppUser appUser) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(AppUser.class, MixIn.class);
    AppUserView ret = mapper.convertValue(appUser, AppUserView.class);
    ret.tokens = appUser.getTokens().stream()
      .filter(q -> TokenView.from(q))
      .toList();
    return ret;
  }

  private int appUserId;
  private String email;
  private boolean isActive;
  private List<TokenView> tokens;
}
```
{% endcode %}

In this case, we have merged everything related into the `AppUserView` - our DTO class.

We:

* define the mixin class - lines 13-15,
* add mixin class to the mapper - line 19,
* do the mapping (skippnig `tokens`) - line 20,
* do the custom mapping for tokens (expecting that `Token` is another entity and `TokenView` is its DTO wit the same behavior).

## Basic Concepts in Action

todo

### Repository

todo

### Service

todo

### REST API Controller

todo

## Sidenotes

TODO
