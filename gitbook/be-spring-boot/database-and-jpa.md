---
icon: square-2
description: >-
  This page describes the database creation and connection via JPA. Also, the
  whole implementation will be tested.
---

# Database & JPA

## Expected database schema

{% hint style="info" %}
Database schema is not expected here to be fixed and is a subject of later changes. At the beginning of the project, you cannot define the whole database correctly, as the requirements by the costumer, or restrictions by the technologies may apply. So, at the beginning, only the skeleton of the database project is expected to be created.\
\
However, in our case we will create the whole database at once, so we do not need return to this chapter later in this course.
{% endhint %}

As the domain, we are creating application users. Every user can have multiple favourite `Url`s. For every url, its `title` and `address` can be set. Moreover, urls can be flagged using `tags`.

The following schema should look like on the next image.

TODO image

{% hint style="info" %}
Note that the real database schema will be created by JPA tool. It may not (and probably will not) match exactly the proposed schema. The proposed schema on the image above is only for the illustration.
{% endhint %}

## Database connection and initialization

{% hint style="info" %}
Here we assume you have your database server already installed and running.
{% endhint %}

### Database creation

The first step is to create your database. In this tutorial, we will name the database as `FavUrlsDB`. For testing purposes, you can also create `FavUrlsTestDB`. There are several possibilities how to create the database:

* You have some external tool to access the database system. In this case, start the tool, connect to the database server and create a new database.
* You have access credentials to the database server and no external tool. In this case, you can use IDEA to access the database server. Follow the tutorial mentioned later "Database creation using IDEA database tools", but omit the database name. Then, use console window to create a new database.

#### Database creation using MariaDB command line tools

1. Open a command line/console shell at the path of MariaDb and its `bin` folder, like:

```powershell
 C:\Program Files\MariaDB 11.4\bin>
```

2. Start the `mariadb.exe` console SQL client, use appropriate username and password if required:&#x20;

```
C:\Program Files\MariaDB 11.4\bin>./mariadb.exe -u user -p password
```

You can omit the password if not required.

3. Create the database using common SQL statements:

```sql
create table FavUrlsDB;
```

4. Add required grants to the user (or create a new user) if required for the database.
5. Quit the console client.
6. Follow the next section, but enter the database name when asked to do so.

#### Database creation using IDEA database tools

IntelliJ IDEA as  bulit in database management support for several database systems. MariaDB is one of them.

TODO



