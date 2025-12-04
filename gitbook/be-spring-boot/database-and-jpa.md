---
description: >-
  This page describes the database creation and connection via JPA. Also, the
  whole implementation will be tested.
icon: square-3
---

# Database & JPA

{% embed url="https://github.com/Engin1980/7vbap-gitbook/tree/be-spring-boot-database-entities-repositories" %}
Relative source codes to this chapter
{% endembed %}



## Expected database schema

{% hint style="info" %}
Database schema is not expected here to be fixed and is a subject of later changes. At the beginning of the project, you cannot define the whole database correctly, as the requirements by the costumer, or restrictions by the technologies may apply. So, at the beginning, only the skeleton of the database project is expected to be created.\
\
However, in our case we will create the whole database at once, so we do not need return to this chapter later in this course.
{% endhint %}

As the domain, we are creating application users. Every user can have multiple favourite `Url`s. For every url, its `title` and `address` can be set. Moreover, urls can be flagged using `tags`.

The following schema should look like on the next image.

![Proposed (and estimated) database schema](/broken/files/QAlqxl1TaZy8hRINiDPM)

{% hint style="warning" %}
Note that the real database schema will be created by JPA tool. It may not (and probably will not) match exactly the proposed schema. The proposed schema on the image above is only for the illustration.
{% endhint %}

## New database creation and connection

{% hint style="info" %}
Here we assume you have your database server already installed and running.
{% endhint %}

In this tutorial, we will name the database as `FavUrlsDB`. For testing purposes, you can also create `FavUrlsTestDB`.

{% hint style="info" %}
The way how to create a new database/user differs between different database servers. If you are not sure how to create a new database, check the documentation of you database server.

Some quick hints:

* MS-SQL - you can use direct connection without specified database. Or, you can use _Microsoft SQL Server Management Studio_ to manage database server and its databases.
* MariaDB/MySQL (service) - when installed as a service, you can connect the server using common third-party tools using specified name and password without existing database.
* MariaDB/MySQL (local) - when executed locally, only default user is created (see the tutorial at the end of this page if necessary). You have to connect using command-line tool, create a database and add a specific user to this database to get user/password access.
{% endhint %}

### Connecting IDEA to the database server

IntelliJ IDEA as bulit in database management support for several database systems. MariaDB is one of them.

In IDEA, you can choose, create and store predefined database connection to a specific database server (and database, if required). In this connection, you can open a console window for SQL commands, which will be submitted to the database server.

To create the database connection in IDEA:

1. From the right narrow menu, select the database icon.
2. In the `Database` window, expand the plus `+` sign and choose `Data source` option.
3. From the list of available database select the requested one (`MariaDB`in our case).
4. In the opened window, submit the required connection data:

![Creation of DB connection in IntelliJ IDEA](/broken/files/6CBuaynSzEGcnSFdZj8z)

You should enter:

* Name - how the connection will be called.
* Host - to set the target database server. Typically, use network computer name, IP address, or `localhost` for the database server on the local machine.
* Port - number of the port where the database server is listening to. Typically, this value is preset to the default with respect to the selected database provider. However, you can adjust this value if required. Default value for MariaDB is 3306.
* Authentication - choose the way how you will authenticate the database user. Common authentications are:
  * None - if no authentication is required.
  * User / Password - to provide username and password.
  * Windows Credentials - to authenticate user using current windows user (typical - but not only for - MS SQL Server).
* User + Password - if this authentication is selected, enter the username and password. Option `Save` defines, how the credentials are stored.
* Database - **as we do not have any yet, leave this blank**.
* URL - common JDBC connection string created by the data entered above.

5. Look for the `Missing Drivers` note above the `Test Connection` note. This button will download required database drivers to the IDEA (**not into the project!**). Once drivers are available, the note will vanish.
6. Test the connection using the `Test Connection` button. If everything is set up correctly, you should see a message confirming a successfull connection.
7. Finally, store the connection.

### Opening IDEA Console for SQL commands + database creation

{% hint style="info" %}
This section explains how to create a console window to submit SQL commands. Here, we only create a database, but you can use this console window to executed any SQL command later.
{% endhint %}

Open the context menu over the created connection in the `Database` window on the top right side and choose _New -> Query Console_.

A new window console window will open. Here, you can enter any SQL commands (DDL - `create`, `alter`, ..., DML - `update`, `insert`, ... or DQL - `select`) and execute them. The result will be displayed below the console window.

To create a new database, simply enter and submit:

```sql
create database FavUrlDB;
```

You should see reponse confirming the database creation, like:

```
[2024-10-01 23:12:43] Connected
> create database FavUrlDB
[2024-10-01 23:12:43] 1 row affected in 7 ms
```

### Adjusting IDEA Connection to work with the specific database

As the database is now created, you can:

* Update the connection created in IDEA in the previous step to connect to the newly created database. Open context menu over the connection and select _Properties_. Then, enter the name of the created database.
* Create a new connection in IDEA specific for the newly created database. Follow the steps in the section [Connecting IDEA to the database server](database-and-jpa.md#connecting-idea-to-the-database-server), but this time enter the database name. As you are creating a new connection, remember to adjust the name of the connection appropriatelly.

{% hint style="info" %}
Again, as mentioned above, we will not create database schema - tables and relations using SQL commands. We will use JPA to create the database schema for us. So, don't create database tables now, you can just close the window.
{% endhint %}

## JPA

JPA - Java Persistence API - is a framework used to work with relational databases from Java. It is based on the more generic technique called ORM - Object-Relation Mapping, aiming at the point, that the database tables are in object-oriented programming represened by classes and table rows/records are represented as instances of those classes.

A class representing a database table (or a view) is called **Entity.**

To work with entities, we will not use common SQL commands. Instead, we will use predefined mechanism called **Repository** (or JPA Repository), which provides all common operations with entities - storing, loading, updating or deletion.

So, in your project, we will firstly create some entites (moreles covering the schema presented at the beginning) and then we will create repositories to work with those entities.

## Creating entities

An Entity represents a class mapped into the database table. Such class must follow some rules:

* The class is marked with the `@Entity` annotation.
* The class must have a primary key attribute marked with `@Id` annotation.
* The class must have a public parameter-less constructor.
* (Mandatory only in distributed enviroment) The class should implement `Serializable` interface..

### Entity AppUser

To create a simple `AppUser`entity, we will create a class with required annotations and properties:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.model.entities;
import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Contract;
import cz.osu.vbap.favUrls.lib.ArgVal;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AppUser {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int appUserId;
  @Column(unique = true, nullable = false, length = 64)
  private String email;
  private String passwordHash;
}
```
{% endcode %}

We can see:

* The class is annotated using `@Entity` to define it as an entity class - line 13.
* The class has some fields - lines 17, 19, 20.
* For those fields, getters and setters are automatically created using Lombok annotations - lines 10, 11.
* Primary key is definedusing `@Id` annotation - line 15. Moreover, primary key value is generated by database server - line 16. For more info about this, see [Automatic primary keys in Sidenotes](database-and-jpa.md#automatic-primary-keys).
* There is also more precise definition of email column - it cannot be null and has predefined length - line 18.
* Finally, a public parameterless constructor is generated automatically using Lombok - line 10

We will also add a custom constructor for object creation with e-mail validation, and automatic email conversion to lowercase on save.

{% code lineNumbers="true" %}
```java
// ...
public class AppUser {
  // ...
  
  @Contract(pure = true)
  public AppUser(@NonNull String email) {
    ArgVal.matchRegex(email, ".+@.+", "email");
    this.email = email;
  }

  @PrePersist
  private void prePersistCheck(){
    if (email != null)
      email = email.toLowerCase();
  }
}
```
{% endcode %}

Here:

* The constructor is provided - lines 5-9. Inside, the e-mail is validated for valid format using [ArgVal class - see Sidenotes](database-and-jpa.md#checking-arguments-argval-class). The constuctor is annotated with a [contract Pure - see Sidenotes](database-and-jpa.md#contract-pure).
* A function `prePersistCheck()` is declared using `@PrePersist` attribute - line 11. This function is invoked just before persisting operation of an instance. In the function, we are simply converting the `email` value to the lowercase.

Finally, we will add relations to the `Url`and `Tag` entity. We do not have such entities yet, we will create them as next:

{% code lineNumbers="true" %}
```java
// ...
public class AppUser {
  // ...
  @OneToMany(mappedBy = "user")
  private Collection<Url> urls;

  @OneToMany(mappedBy = "user")
  private Collection<Tag> tags;
}
```
{% endcode %}

Here:

* `@OneToMany` annotation says, that **one** appUser will have **many** urls/tags. Because of this, `urls` and `tags` are represented as `java.util.Collection` of respective types. (See hint below.)
* `mappedBy` attribute defines, to which attribute in the target entity is this relation mapping. E.g., according to the line 4/5, it is expected that the `Url` entity will have some attribute `user` as an opposide side of this relation (see `Url`entity and its relations).

{% hint style="info" %}
If you need a target type for `...Many` relation, you can pick any type supporting several values - most common are `List`, `Set` or `Collection`. JPA will fill the field with the appropriate instance when required.
{% endhint %}

### Entity Url

The code of `Url` entity is similar:

```java
package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Url {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int urlId;
  @Column(nullable = false, length = 256)
  private String title;
  @Column(nullable = false)
  private String address;
}
```

The code is very similar to the previous entity.

Additionally, we create the relation to the `AppUser`:

{% code lineNumbers="true" %}
```java
// ...
public class Url{
  // ...
  @ManyToOne
  @JoinColumn(
    name = "app_user_id", 
    foreignKey = @ForeignKey(name = "FK_url_app_user"))
  private AppUser user;
}
```
{% endcode %}

Here, we declare:

* The opposite side of the relation to the `AppUser`- `@ManyToOne` as there are may be several Urls for a single user - line 4.
* Moreover, we need to declare, how this relation is stored in the database. We declare a `@JointColumn` representing the relation - line 6. This column will be named `app_user_id`.

{% hint style="info" %}
If not specified the `name` of the `@JointColumn` will be generated automatically. In most cases it may be fine, but sometime the generated name may be misleading, so you can specify a custom one.
{% endhint %}

{% hint style="warning" %}
A relation in relational database is represented as a table _constraint_. This constrain must have a unique name. Therefore, the name is automatically generated; and for JPA, it is a random string.

However, in such case, if issue occurs, you will get an error response telling you something like "A constraint A5FLK5LKFJ was violated." telling you directly nothing about the specific issue.

Therefore, we suggest to add a meaningfull name to the constraints too - see the previous listing, line 7.
{% endhint %}

Now, lets add the first side of M:N relation into the `Url`entity to the `Tag` entity:

```java
// ...
public class Url{
  // ...
  @ManyToMany
  @JoinTable(name = "url_tag",
          joinColumns = @JoinColumn(name = "url_id", foreignKey = @ForeignKey(name = "FK_url_tag_url")),
          inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "FK_url_tag_tag")))
  private Collection<Tag> tags;
 }
```

Here, the content is very similar. The difference is that we are creating the whole `@JoinTable` with two columns representing M:N relation between `Url` and `Tag`.

At the end, lets add a custom construtor with checks and initialization:

```java
// ...
public class Url{
  // ...
  @Contract(pure = true)
  public Url(AppUser user, String title, String address, Tag... tags) {
    ArgVal.notNull(user, "user");
    ArgVal.notWhitespace(title, "title");
    ArgVal.isTrue(() -> title.length() <= 256, "Title must have 256 characters at most.");
    ArgVal.notWhitespace(address, "address");

    this.user = user;
    this.title = title;
    this.address = address;
    if (tags.length > 0) {
      this.tags = List.of(tags);
    }
  }
}
```

### Entity Tag

Finally, the `Tag`entity is build:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.util.Collection;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class Tag {
  private final static int COLOR_LENGTH = 3;

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int tagId;
  @Column(nullable = false)
  private String title;
  @Column(nullable = false, length = COLOR_LENGTH)
  @Size(min = COLOR_LENGTH, max = COLOR_LENGTH) // this needs "spring-boot-starter-validation" in pom.xml
  private String color;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_tag_app_user"))
  private AppUser user;

  @ManyToMany(mappedBy = "tags")
  private Collection<Url> urls;

  @Contract(pure = true)
  public Tag(AppUser user, String title, String color) {
    ArgVal.notNull(user, "user");
    ArgVal.notWhitespace(title, "title");
    ArgVal.matchRegex(color, "[0-9a-fA-F]{" + COLOR_LENGTH + "}", "color");

    this.title = title;
    this.user = user;
    this.color = color;
  }
}
```
{% endcode %}

{% hint style="info" %}
When creating entities, we are trying to lay as many validations as possible on database consistency checks to be sure that our data are always consistent.

Therefore, at line 26, the `@Size` annotation has been used to ensure that the column length is always 3.

Unfortunately, this annotation is not a part of common JPA libraries. To use it, we need to extend the Maven with a new library as follows:
{% endhint %}

```xml
<!-- for advanced SQL validation -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Entity Token

Now, we create an entity used to store refresh and password-reset tokens.

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.model.entities;

import cz.osu.vbap.favUrls.lib.ArgVal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"app_user_id", "type"}, name = "UQ_token_app_user_type")
})
public class Token {

  @Getter
  @AllArgsConstructor
  public enum Type {
    REFRESH('R'), PASSWORD_RESET('P');
    private final char code;
  }

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private int tokenId;
  @Column(unique = true, nullable = false)
  private String value;
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Type type;

  @ManyToOne
  @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "FK_token_app_user"))
  private AppUser appUser;

  @Contract(pure = true)
  public Token(AppUser appUser, Type type, String value) {
    ArgVal.notNull(appUser, "appUser");
    ArgVal.notNull(type, "type");
    ArgVal.notWhitespace(value, "value");

    this.type = type;
    this.value = value;
    this.createdAt = LocalDateTime.now();
    this.appUser = appUser;
  }
}

```
{% endcode %}

Entity is a bit more complicated. Except already known stuff, we have here:

* It contains inner enum type `Type` distinguishing between refresh or password-reset tokens. Every value of this enum can be transformed into a char - lines 22-27.
* Forced uniqueness of tuple of attributes `appUser` and `type`. That means only one combination for an user and a type is allowed. As this is complex unique constraint, it must be defined over the table (instead of a column) - lines 17-19.

Moeover, we need to define how `Token.Type` will be stored.&#x20;

{% hint style="info" %}
JPA has by default no idea how to store an enum into the database. There are several options available. For options and their explanations see the link below.
{% endhint %}

We will write a custom converter named `TokenTypeConverter` placed at `.../model/converters`:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.model.converters;

import cz.osu.vbap.favUrls.model.entities.Token;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class TokenTypeConverter
        implements AttributeConverter<Token.Type, Character> {
  @Override
  public Character convertToDatabaseColumn(Token.Type type) {
    Character ret = type == null ? null : type.getCode();
    return ret;
  }

  @Override
  public Token.Type convertToEntityAttribute(Character character) {
    Token.Type ret;
    if (character == null)
      ret = null;
    else
      ret = Arrays.stream(Token.Type.values())
              .filter(t -> t.getCode() == character)
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Unknown token type: " + character));
    return ret;
  }
}
```
{% endcode %}

Converter is pretty straighforward:

* Forward conversion is simple
* Backward conversion tries to extrac the correct enum value from char based on token values using collection lambda.&#x20;
* The attribute value `autoApply=true` tells JPA to use this converter everytime it meets with `Token.Type` datatype - line 9.

{% embed url="https://www.baeldung.com/jpa-unique-constraints" %}
How an unique constraint can be defined in JPA
{% endembed %}

{% embed url="https://www.baeldung.com/jpa-persisting-enums-in-jpa" %}
How to work with enum in JPA
{% endembed %}

## Creating Repositories

JPA based repositories inherits from the interface `JpaRepository` with two generic arguments. The first one is the entity type, the second is the type of the primary key. So, in our case, we can simply create all repository interfaces:

```java
package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> getByEmail(String email);
}
```

```java
package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UrlRepository extends JpaRepository<Url, Integer> {
    Collection<Url> getByUser(AppUser appUser);
}
```

```java
package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {}
```

```java
package cz.osu.vbap.favUrls.model.repositories;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
  Optional<Token> findByAppUserAndType(AppUser appUser, Token.Type type);
  Optional<Token> findByValue(String value);
}
```

{% hint style="info" %}
Note that repositories are **interfaces**. The specific implementation is provided by JPA on request when needed.
{% endhint %}

{% hint style="info" %}
Note that before was mandatory to append `@Repository` annotation. Now, it is added by default and is not needed.
{% endhint %}

You can specify additional custom query methods into the interfaces when needed - like `getByEmail()` in `AppUserRepository`. For those methods, you can use specific JPA-query-language JPQL to specify the query request.

TODO: Quering explanation and more examples will be added in the future.

{% embed url="https://www.baeldung.com/spring-data-jpa-query" %}
Introduction to custom JPA queries
{% endembed %}

{% embed url="https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html" %}
Exhaustive explanation of JPA query methods
{% endembed %}

{% embed url="https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html" %}
Exhaustive explanation of JPA query methods using JPQL
{% endembed %}

## Adding data on app startup

To add data on app startup, you can use _CommandLineRunner_.&#x20;

A `CommandLineRunner` is a functional interface used to run a block of code at the application startup, after the Spring application context has been initialized. It is often used for tasks like executing some startup logic, initializing resources, or running database checks when the application starts.

A `CommandLineRunner` instance returns **a function (!)** invoked when the app is started. To invoke the instance on startup, its declared as a `@Bean`. Also, you can use Dependency Injection (DI) in arguments.

The following example will create an `initDatabase()` runner, which will fill the database with some initial data.

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.entities.Tag;
import cz.osu.vbap.favUrls.model.entities.Url;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import cz.osu.vbap.favUrls.model.repositories.TagRepository;
import cz.osu.vbap.favUrls.model.repositories.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FavUrlsApplication {

  public static void main(String[] args) {
    SpringApplication.run(FavUrlsApplication.class, args);
  }


  /**
   * Initializes the database with sample data.
   *
   * @param appUserRepository the repository for managing `AppUser` entities
   * @param tagRepository the repository for managing `Tag` entities
   * @param urlRepository the repository for managing `Url` entities
   * @return a `CommandLineRunner` that initializes the database
   */
  @Bean
  public CommandLineRunner initDatabase(
          @Autowired AppUserRepository appUserRepository,
          @Autowired TagRepository tagRepository,
          @Autowired UrlRepository urlRepository) {
    return _ -> {
    
      if (appUserRepository.findByEmail("marek.vajgl@osu.cz").isPresent())
        return; // data already exist
    
      AppUser user = new AppUser("marek.vajgl@osu.cz");
      appUserRepository.save(user);

      Tag privateTag = new Tag(user, "private", "F00");
      tagRepository.save(privateTag);

      Tag publicTag = new Tag(user, "public", "0F0");
      tagRepository.save(publicTag);

      Url url = new Url(user, "OU", "https://www.osu.cz", privateTag, publicTag);
      urlRepository.save(url);
    };
  }
}

```
{% endcode %}

You can see:

* Existing repository instances will be automatically added when `initDatabase()` is invoked. Its the responsibility of SpringBoot, as those arguments are annotated with `@Autoired` - lines 33-35.
* The function is annotated with `@Bean`; otherwise, the code will not be executed - line 31.
* The `initDatabase()` method returns a lamba expression - a reference to another anonymous metod - see the `return _ -> {};` statement at lines 36 to 52.
* The inner anonymous method will do the database initialization.

## Testing with Unit Tests

You can use Unit Testing to test the database. To do so, you typically need to:

* create an alternating or replacing version of `application.properties` file with the property values updated w.r.t. the test, and
* create appropriate unit testing methods.

### Replacing default properties

Create a new configuration file at `.../src/test/resources` called e.g. `test.properties`. Adjust the content of the file by replacing the original `application.properties`,  e.g.:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/favUrlsTestDB
```

Here, we have changed the original database.

Now, create a unit testing class with the new annotations marking a spring boot test with specific testing properties:

{% code lineNumbers="true" %}
```java
package cz.osu.vbap.favUrls.model.db;

// ...
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest()
@TestPropertySource(locations =
        {"classpath:application.properties", "classpath:test.properties"}) 
public class AppUserTest {

}
```
{% endcode %}

Here, the annotation at line 17/18 tells "load `application.properties` and **then** load `test.properties`, what will replace/extend the original configuration).

### Writing a test

A test is simple and is no different from the common unit test:

```javascript
package cz.osu.vbap.favUrls.model.db;

import cz.osu.vbap.favUrls.model.entities.AppUser;
import cz.osu.vbap.favUrls.model.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@TestPropertySource(locations =
        {"classpath:application.properties", "classpath:test.properties"})
public class AppUserTest {

  @Autowired
  AppUserRepository appUserRepository;

  @Test()
  void duplicateUser() {

    AppUser a = new AppUser("john.doe@osu.cz");
    appUserRepository.save(a);

    AppUser b = new AppUser("jane.doe@osu.cz");
    appUserRepository.save(b);

    AppUser c = new AppUser("jane.doe@osu.cz");
    try {
      appUserRepository.save(c);
      fail("Duplicate email should not be saved");
    } catch (DataIntegrityViolationException ex) {
      assertTrue(ex.getMessage().toLowerCase().contains("duplicate"));
      assertTrue(ex.getMessage().toLowerCase().contains("jane.doe@osu.cz"));
    }
  }

  @Test()
  void emailStoredLowerCase(){
    String email = "MIKE.WHITE@OSU.CZ";
    AppUser a = new AppUser(email);
    appUserRepository.save(a);
    int aId = a.getAppUserId();

    Optional<AppUser> oa = appUserRepository.findById(aId);
    assertTrue(oa.isPresent());
    assertEquals(oa.get().getEmail(), email.toLowerCase());
  }
}
```

## Database Migrations

In the context of applicaition development, database migration is the process of evolving your database schema in a controlled, versioned manner. Just like versioning code, database versioning involves keeping track of changes made to the database structure (schema) over time, ensuring that your development, testing, and production environments stay in sync.

Typically, each change to the database schema (like adding a column, modifying a table, or creating an index) is written as a migration script. These scripts are assigned a version number (e.g., `V1__Create_User_Table.sql`, `V2__Add_Email_To_User.sql`), allowing the system to track which migrations have been applied. As the application evolves, new migration scripts are added. When the application is deployed, the migration tool automatically applies any new changes (if necessary) to bring the database to the latest version. This ensures that your database schema is always compatible with your application code. Also, many migration tools also provide a way to rollback migrations in case an error is found. This can revert the database to a previous version.

In the Java and Spring Boot ecosystem, two primary tools are commonly used for database migration and versioning: _Flyway_ and _Liquibase_. Both tools are well-integrated with Spring Boot and provide features to manage database schema changes efficiently.

In this tutorial, we will work with _Flyway_.

{% hint style="info" %}
Flyway is typically closely connected and related to another common JPA support addon in Idea - _Jpa Buddy_. Also, lot of tutorials show database migration based on Flywy and JPA Buddy.

Unfortunately, up to today (2024-10-03) JPA Buddy has a bug causing crash on many IDEA instalations. So, in this tutorial, we will use direct IDEA support for Flyway and database migrations.
{% endhint %}

{% embed url="https://www.red-gate.com/products/flyway/community/" %}
Flyway website
{% endembed %}

{% embed url="https://jpa-buddy.com/" %}
JPA Buddy website
{% endembed %}

### Adding Flyway support to IDEA

Idea in Ultimate edition has already installed _Flyway_ plugin already. Check if the plugin is among your active plugins, or install it if necessary.

![Flyway plugin in IDEA](/broken/files/pIaJAJUnRkBDb604Ugde)

### Adding Flyway support to the project

Initially, you need to add required dependencies into the Maven (`pom.xml`):

```xml
<!-- core functionality -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- database-server-specific dependency -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

Note that the first dependency is no related to the selected database server. Otherwise, the second dependency is database specific and needs to be adjusted for the selected database. As _MariaDB_ is a fork of _MySQL_ database, it uses the same dependecy here.

### Project preparation

Create a new SpringBoot project with dependencies:

* Lombok
* Spring Data JPA
* MariaDB Driver
* Flyway Migrations

What is new in the created project is a new folder structure at `src/java/main/resources/db/migrations`. The migrations SQL scrips will be stored here.

Next, set up the configration in `application.properties` file:

{% code lineNumbers="true" %}
```properties
spring.application.name=FavUrlMigrationsDemo

# JPA properties
spring.datasource.url=jdbc:mariadb://localhost:3306/FavUrlsMigrations
spring.datasource.username=root
spring.datasource.password=sa
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none

# Flyway properties
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```
{% endcode %}

Note here that we **disabled JPA database re-creation on startup** at line 8. The Flyway mechanism itself will care for the database state.

Now, create a new database called `FavUrlsMigrations` and create IDEA database connection to this DB.

{% hint style="info" %}
Note: As the database is empty at the beginning, there is no need to create specific initial migration script.

However, for non-empty database right now you should do the initial Flyway migration.
{% endhint %}

### Create a migration

Firstly, lets create a simple entity in the code (simiarly to the previous context):

```java
// ...

@Getter
@Setter
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String passwordHash;
}
```

Now, we can create a first migration - from the empty state to the state with respect to the current entities defined in our project.

Open the context menu over IDEA database connection and select _Create Flyway Migration..._:

![Creation of the new migration in IDEA](/broken/files/rEBb1ZapRgGV6gviZIYv)

The new window will appear with the source and target definition. As a source (the required state) we address the current entities definitions in the project (set by default). As a target (current state of the model) we address a current database state, which is now empty (set by defualt). Then we confirm the dialog.

![Initial migration dialog in IDEA](/broken/files/bXNiEXsGCP7t9h0HROVJ)

A new window - Migration Preview Window - will appear. Here we check the correctness of the generated SQL script and **set the migration name**. Then name should contain the version number (generated automatically) and a description of the migration operation:

![Migration Preview Window](/broken/files/w1Zrh1gi5QWDSFm0NcD8)

Once confirmed, there will be a new file in the `.../db/migrations` folder. The generated content of the file `V1__create_AppUser.sql` is:

```sql
CREATE TABLE app_user
(
    id            INT AUTO_INCREMENT NOT NULL,
    email         VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    CONSTRAINT pk_appuser PRIMARY KEY (id)
);
```

### Applying the migration

The migration is applied automatically once the application is executed. Flyway will check the current database version and if required, apply the missing migrations to get the database to the latest version.

So, to apply the changes, just simply start app. The output will be like:

```
...
2024-10-03T13:56:32.386+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.mariadb.jdbc.Connection@11e355ca
2024-10-03T13:56:32.387+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2024-10-03T13:56:32.407+02:00  WARN 2912 --- [FavUrlMigrationsDemo] [           main] o.m.jdbc.message.server.ErrorPacket      : Error: 1193-HY000: Unknown system variable 'WSREP_ON'
.. here starts the interesting part...
2024-10-03T13:56:32.421+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:mariadb://localhost/FavUrlsMigrations (MariaDB 11.4)
2024-10-03T13:56:32.433+02:00  WARN 2912 --- [FavUrlMigrationsDemo] [           main] o.f.c.internal.database.base.Database    : Flyway upgrade recommended: MariaDB 11.4 is newer than this version of Flyway and support has not been tested. The latest supported version of MariaDB is 11.2.
2024-10-03T13:56:32.447+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.c.i.s.JdbcTableSchemaHistory         : Schema history table `favurlsmigrations`.`flyway_schema_history` does not exist yet
2024-10-03T13:56:32.449+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 1 migration (execution time 00:00.010s)
2024-10-03T13:56:32.462+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.c.i.s.JdbcTableSchemaHistory         : Creating Schema History table `favurlsmigrations`.`flyway_schema_history` ...
2024-10-03T13:56:32.523+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema `favurlsmigrations`: << Empty Schema >>
2024-10-03T13:56:32.527+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.core.internal.command.DbMigrate      : Migrating schema `favurlsmigrations` to version "1 - create AppUser"
2024-10-03T13:56:32.547+02:00  WARN 2912 --- [FavUrlMigrationsDemo] [           main] o.f.c.i.s.DefaultSqlScriptExecutor       : DB: Name 'pk_appuser' ignored for PRIMARY key. (SQL State:  - Error Code: 1280)
2024-10-03T13:56:32.562+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema `favurlsmigrations`, now at version v1 (execution time 00:00.020s)
... here ends the interesting part...
2024-10-03T13:56:32.618+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2024-10-03T13:56:32.657+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2024-10-03T13:56:32.684+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2024-10-03T13:56:32.899+02:00  INFO 2912 --- [FavUrlMigrationsDemo] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
```

We can see that the Flyway found that there are no migrations applied yet, creates a helper table and then apply the migration.

If we check the database, there are two tables. The first one is `AppUser` for the entity, and the second one is `flyway_schema_history` containing the migration data.

### Creating next migration

Now, we can create next migration. Lets add a new entity `Url` with its column and also a 1:N relation between `AppUser` and `Entity`.

```java
// ...
@Getter
@Setter
@Entity
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 2048)
    private String address;

    @ManyToOne
    @JoinColumn(name = "app_user_id", foreignKey = @ForeignKey(name = "fk_url_app_user"))
    private AppUser appUser;
}
```

Now, we can again create a new migration using the procedure described above.&#x20;

{% hint style="info" %}
It is expected that the database is up-to-date when the migration is created!

Remember, that the migration is generated with respect to the current database state.
{% endhint %}

The content of newly generated file `V2__create_Url.sql` is adding the table and also the reference:

```sql
CREATE TABLE url
(
    id          INT AUTO_INCREMENT NOT NULL,
    address     VARCHAR(2048)      NOT NULL,
    app_user_id INT                NULL,
    CONSTRAINT pk_url PRIMARY KEY (id)
);

ALTER TABLE url
    ADD CONSTRAINT FK_URL_APP_USER FOREIGN KEY (app_user_id) REFERENCES app_user (id);
```

### More details

As you can see, the migrations are done by applying a SQL script on the database. You are free to adjust the migration script. This feature is typically used when there are column changes in the database and you need to preserve the existing data. In this case you can define how the new column is created and how are the data transfered from the old column(s) into the new one(s).

To see the more complex usage, you can follow the documentation or IDEA tutorials.

{% embed url="https://www.jetbrains.com/help/idea/flyway.html" %}
Flyway in IDEA - more detailed usage description
{% endembed %}

## Sidenotes

### Automatic primary keys

There are several ways, how the uniqueness of the primary key (PK) can be handled. The most common approaches are:

* Autogenerated PK - JPA will create its own, unique primary key value whenever needed. The programmer should never provide his own primary key. How this can be achieved:
  * IDENTITY - The database will generate the primary key value automatically w.r.t. to the values in the table. This approach is not supported by all database systems and is typical for MySQL, MariaDB or MS-SQL.
  * SEQUENCE - The database will generate the primary key value based on some sequencer object producing unique sequence of data. This approach is typical for Oracle, PostgreSQL or H2.
  * TABLE - JPA will create custom table to hold the sequence/next value for the key for the table by its own. This approach is database independent and is used, when no other option is available.
  * AUTO - JPA will determine the appropriate mechanism from the three above on its own.
* UUID/GUID - a primary key will be represented by a random (or partially random) 128bit long sequence of bytes. It may be generated by a programmer and provided on persist operation, or generated by a database and returned as a primary key. The way how the value is generated is trying to ensure that there are no conflicts between randomly generated primary keys. This approach is typically used in distributed environment.
* A programmer will provide a primary key
  * Data-related primary key - this approach is typically used when the primary key is not represented by a numerical sequence, but is related to a data (production number, insurance number, etc.) In this case, the value of the primary key is defined by the stored data.
  * Numerical-sequence primary key - this approach means that a programmer must provide the primary key value and ensure its uniqueness. This is typcially related to some significant performance loss caused by table lock handling and is not recommended.

A non-data related primary key is a preferred approach, because:

* You are not related to the data definition. If in the future a situation changes and the previously unique value used as primary key loses its uniqueness, it envokes a significant architecture changes in the whole application. If the primary key is generated automatically by the database system, its value is independent on data and not affected by their definition change.
* You can still ensure the uniquess of the required non-primary key data by adding the UNIQUE constraint in the database.
* You avoid the complex primary key, which can be harder to maintain and handle. Again, to ensure that some attribute set is unique, the UNIQUE constraint can be used.

To generate the value automatically, a `@GeneratedValue` annotation with the chosen `strategy` can be used, as can be seen in the examples above.

### Database creation using MariaDB command line tools

You may download MariaDB server as a ZIP file and simply extract it into the folder. As such server is not installed as a OS service/daemon, it is not initially configured.

{% hint style="info" %}
This tutorial is aimed on Windows OS. However, it is applicable on Linux-based systems.
{% endhint %}

To configure the server and create a database initially, several steps must be taken:

1. Navigate to the MariaDB `bin` folder with executables
2. Do an initial installation by executing:

```powershell
.\mariadb-install-db.exe
```

3. Start the MariaDB server instance using (this proces should remain running - you cannot enter additional commands):

```powershell
.\mariadbd.exe
```

4. Open **a new** console window and navigate to the same folder.
5. In the new window, connect to the server as a _root_ user using:

```powershell
.\mariadb.exe -u root
```

6. Create a new user with a password:

```sql
create user USERNAME identified by 'PASSWORD';
```

7. Optionally, you can check the user was created and see password token:

```sql
select password('USERNAME');
```

8. Now, create a new database using SQL:

```sql
create database DATABASE_NAME;
```

9. Finally, grand access rights to the database for your user:

```sql
grant all PRIVILEGES on DATABASE_NAME.* to 'USERNAME' with grant OPTION;
```

9. Apply the changed privileges:

```sql
flush privileges;
```

Now, you should be able to connect to the `DATABASE_NAME` with username `USERNAME` and password `PASSWORD`.

### Checking arguments - ArgVal class

It is important to check the arguments of the functions - at least for the public ones, as you must ensure that there is no nonsense incoming into the function.

Typically, you check those arguments at the beginning of the function using a sequence of `if` conditions followed by `throw` statements with the exception data. As `if` statements typically multiline, the code of the function easily became long, especially for more complex checks:

```java
// ...
public class Tag {
  // ...

  public Tag(AppUser user, String title, String color) {
    if (user == null)
      throw new IllegalArgumentException("User is null");
    if (title == null || title.trim().isEmpty())
      throw new IllegalArgumentException("Title is empty.");
    if (color.matches("[0-9a-fA-F]{" + COLOR_LENGTH + "}"))
      throw new IllegalArgumentException("Color does not match the specified regular expression.");
	
    // ...
  }
}
```

Even in this example the intro checks immediatelly look complicated.

Therefore, it is common to use some additional library or create a custom one to get rid of the complex `if` calls.

The updated code may look like:

```java
// ...
public class Tag {
  // ...

  public Tag(AppUser user, String title, String color) {
    ArgVal.notNull(user, "user");
    ArgVal.notWhitespace(title, "title");
    ArgVal.matchRegex(color, "[0-9a-fA-F]{" + COLOR_LENGTH + "}", "color");
	
    // ...
  }
}
```

The code above is easier to read - not in the meaning that it you need to read less data for understanding, but in the meaning that you can optically "skip" all the lines starting with `ArgVal` and ignore them, what cannot be so easily visually achieveable in the case of more complex `if` blocks.

Of course, if the validation section is too long, you can consider the creation of some sanity checking function to remove all the checks, like:

```java
// ...
public class Tag {
  // ...

  public Tag(AppUser user, String title, String color) {
    validateArgs(user, title, color);
	
    // ...
  }
}
```

However, in most cases a custom method validating arguments for another method may be an overkill.

In our case we created a simple `ArgVal` class, which contains some basic methods, but will be extended in the future. Check the Git repository for the most current implementation if necessary:

```java
package cz.osu.vbap.favUrls.lib;

import org.jetbrains.annotations.Contract;

import java.util.function.Function;
import java.util.function.Supplier;

public class ArgVal {
  @Contract(pure = true)
  public static void notNull(Object value, String argName) {
    if (value == null) {
      throw new IllegalArgumentException(argName + " must not be null");
    }
  }

  @Contract(pure = true)
  public static void matchRegex(String text, String regex, String argName) {
    if (text == null || !text.matches(regex)) {
      throw new IllegalArgumentException(argName + " must match regex " + regex + ". Invalid value: " + text);
    }
  }

  @Contract(pure = true)
  public static void notWhitespace(String text, String argName) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException(argName + " must not be empty.");
    }
  }

  public static void isTrue(Supplier<Boolean> validator, String argName) {
    boolean res;
    try{
      res = validator.get();
      if (!res) throw new IllegalArgumentException(argName + " failed to pass 'isTrue' validation");
    }catch (Exception ex){
      throw new IllegalArgumentException(argName + " crashed when passing 'isTrue' validation");
    }
  }
}
```

The explanation will be provided for the last method `isTrue` only.

The method accepts a special argument - `Supplier<Boolean>`, what in general is a function accepting no arguments and returning bool value. The purpose of this supplier is to return true or false. If `false` value is returned, the method `isTrue` will invoke an exception.

The method is supposed to be called with a lambda expression of type `Supplier<Boolean>`, that is accepting no arguments and returning null, e.g.:

```java
ArgVal.isTrue(() -> title.length() <= 256, "Title is too long.");
```

{% hint style="info" %}
A **lambda expression** in Java is a way to express instances of single-method interfaces (functional interfaces) in a more concise and readable way. Introduced in **Java 8**, lambda expressions help reduce the amount of boilerplate code, especially when working with functional-style operations on collections, like `map`, `filter`, and `forEach`.
{% endhint %}

{% embed url="https://www.geeksforgeeks.org/lambda-expressions-java-8/" %}
More detailed explanation of Java Lambda expressions
{% endembed %}

### @Contract Pure

The `@Contract` annotation with `pure=true` in Java is part of JetBrains' **IntelliJ IDEA** annotations, used primarily for static code analysis. It provides additional metadata about a method's behavior to help tools like IntelliJ IDEA and static analyzers understand the method better.

`@Contract` is an annotation used to define a **method contract** — a formal description of the method's behavior in terms of input/output relationships, side effects, and nullability. This allows tools to perform advanced static analysis and catch potential bugs by understanding how the method behaves under various conditions.

When `pure=true` is specified in the `@Contract` annotation, it indicates that the method is **pure**, meaning it:

* **Does not modify** any state (either of the object or any external state).
* **Has no side effects**.
* Always returns the same result when given the same inputs (idempotent).

{% hint style="info" %}
A **side effect** occurs when a function or method performs an action that affects the outside world or modifies some state beyond returning a value. Side effects are typically any observable effects outside the function itself, like modifying global or external variables, performing I/O operations, or altering the state of objects.
{% endhint %}

So, you can use `@Contract(pure=true)` to mark functions without supposed side effect, what can improve further code analysis and make code more error-proof.

However, to use this annotation, you need to add additional dependency to `pom.xml` file:

```xml
<dependency>
    <groupId>org.jetbrains</groupId>
    <artifactId>annotations</artifactId>
    <version>RELEASE</version>
</dependency>
```

{% embed url="https://www.baeldung.com/jetbrains-contract-annotation" %}
Java @Contract explanation in more detail
{% endembed %}

### Custom Converters (storing Enum)

Sometimes, you have your custom type you would like to be stored in a single column. The type may be more complex (like GPS coordinate stored as `latitude;longitude` string, but this approach is also very common for enum types. In this case, JPA has no clue how to save the type instance into the db column or vice versa. There, you need _AttributeConveters_.

An **attribute conveter** is a class defining how a custom entity type is converted into a database column and back. It uses two methods, `convertToDatabaseColumn(...)` and the opposite one, `convertToEntityAttribute(..)`.

**Example**

Imagine a simple enum:

```java
public enum Genre {
  TRANCE,
  HOUSE,
  TECHNO
}
```

And the entity using it:

```java
@Entity
public class Track{
  @Id private int Id;
  private String title;
  private Genre genre;
}
```

Here, by default, JPA has no idea how to store `Genre` type.&#x20;

The easiest way is to mark the enum attribute with `@Enumerate` attribute:

```java
// ...
@Enumerated(EnumType.ORDINAL)  // or STRING
private Genre genre;
// ...
```

Using this annotation, the enum will be stored as integer ordinal, or a string value. However, lets store it by its first three characters. Let's create a class:

{% code lineNumbers="true" %}
```java
@Converter(autoApply = true)
public class GenreConverter implements AttributeConverter<Genre, String> {
 
    @Override
    public String convertToDatabaseColumn(Genre genre) {
        if (category == null) {
            return null;
        }
        return category.substring(0,3);
    }

    @Override
    public Genre convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(Genre.values())
          .filter(c -> c.name().substring(0,3).equals(code))
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);
    }
}
```
{% endcode %}

Here:

* definition at line 1 says that this converter will be used always for type `Genre`;
* generic arguments at the end of line 2 says that this is a definition of mapping from `Genre` into `String`;
* and two methods define forward and inverse implementation of the conversion.

{% embed url="https://www.baeldung.com/jpa-persisting-enums-in-jpa" %}
Persisting Enums in JPA - tutorial
{% endembed %}
