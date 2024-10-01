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

![Proposed (and estimated) database schema](imgs/db-schema.png)

{% hint style="info" %}
Note that the real database schema will be created by JPA tool. It may not (and probably will not) match exactly the proposed schema. The proposed schema on the image above is only for the illustration.
{% endhint %}

## Database connection and initialization

{% hint style="info" %}
Here we assume you have your database server already installed and running.
{% endhint %}

In this tutorial, we will name the database as `FavUrlsDB`. For testing purposes, you can also create `FavUrlsTestDB`. 

{%hint style="info" %} The way how to create a new database/user differs between different database servers. If you are not sure how to create a new database, check the documentation of you database server.

Some quick hints:
* MS-SQL - you can use direct connection without specified database. Or, you can use Microsoft SQL Server Management Studio to manage database server and its databases.
* MariaDB/MySQL (service) - when installed as a service, you can connect the server using common third-party tools using specified name and password without existing database.
* MariaDB/MySQL (local) - when executed locally, only default user is created (see the tutorial at the end of this page if necessary). You have to connect using command-line tool, create a database and add a specific user to this database to get user/password access.

{% endhint %}

### Connecting IDEA to the database server

IntelliJ IDEA as  bulit in database management support for several database systems. MariaDB is one of them.

In IDEA, you can choose, create and store predefined database connection to a specific database server (and database, if required). In this connection, you can open a console window for SQL commands, which will be submitted to the database server.

To create the database connection in IDEA:

1. From the right narrow menu, select the database icon.
2. In the `Database` window, expand the plus `+` sign and choose `Data source` option.
3. From the list of available database select the requested one.
4. In the opened window, submit the required connection data:

![Creation of DB connection in IntelliJ IDEA](imgs/db-idea.png)

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

5. Look for the `Missing Drivers` note above the `Test Connection` note. This button will download required database drivers to the IDEA (not into the project!). Once drivers are available, the note will vanish.
6. Test the connection using the `Test Connection` button. If everything is set up correctly, you should see a message confirming a successfull connection.
7. Finally, store the connection.

### Opening IDEA Console for SQL commands + database creation

/hint This section explains how to create a console window to submit SQL commands. Here, we only create a database, but you can use this console window to executed any SQL command later.



### Sidenote: Database creation using MariaDB command line tools

TODO



Some

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