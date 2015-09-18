# Ruuvi-Server

# Components

Ruuvi-Server uses plain [Stuart Sierra style components](https://github.com/stuartsierra/component) to componentize the system and provice dependency injection. [system.clj](/src/ruuvi/system.clj) wires up the components to form the full application.

`repl.clj` -

# Routing and REST

Web stack is based on [Ring](https://github.com/ring-clojure).
Routing is handled by [rook](https://github.com/AvisoNovate/rook). [ring-handler.clj](/src/ruuvi/ring_handler.clj) sets up web stack, including authentication, generated Swagger documentation, [Schema](https://github.com/Prismatic/schema) based validation for incoming requests.

REST resources are defined in directory `/src/ruuvi/resources`.

## Swagger

REST routes are documented with [Swagger](http://swagger.io/). The documentation is available at [http://localhost:7000/doc/index.html](http://localhost:7000/doc/index.html).

## Authentication

## Validation

# Configuration

# Logging

Logging is handled by [clojure.tools.logging](https://github.com/clojure/tools.logging) and [Logback](http://logback.qos.ch/). See logback instructions where the log messages are stored.

# Database

## Migrations

Migrations are changs to database schema. Migrations are managed by [ragtime](https://github.com/weavejester/ragtime) library.

Information which migrations have been executed is stored to database table `ragtime_migrations`.

Each migration consists of two *.sql files: *.up.sql is executed to apply a migration, *.down.sql is executed to rollback a migration. The sql files are stored in `resources/migrations` directory and are thus accessible via classpath. Naming of the files is important. The files are applied in alphabetical order.

The idea is that once a migration file is released as a part of the application, it is never changed. All the fixes to the database are done by adding new migration files thus providing an upgrade path to later version. An old version of database can always be updated to latest version just by applying missing migrations in order.


# Domain objects

Domain objects are implemented with (Schema)[https://github.com/Prismatic/schema].

# Code structure

- `src/ruuvi/`
  - [main.clj](/src/ruuvi/main.clj) is the starting point. It handles commandline argument and starts the system.
  - [system.clj](/src/ruuvi/system.clj) wires components together to form a full application.
  - [ring_handler.clj](/src/ruuvi/ring_handler.clj) contains the setup for web stack implemented as a Component.
  - [middleware.clj](/src/ruuvi/middleware.clj) contains various middlewares used by web stack.
  - [swagger.clj](/src/ruuvi/swagger.clj) has utility methods and middlewares for generating Swagger documentation.
  - [config.clj](/src/config.clj) has utility methods for handling configuration files.

- `src/ruuvi/database/`
  - [connection.clj](/src/ruuvi/database/connection.clj) implements a JDBC database connection pool as a Component.
  - [migration.clj](/src/ruuvi/database/migration.clj) implements database migration management as a Component

- `src/ruuvi/resources/` contains REST resource implementations
  - [domain.clj](/src/ruuvi/resources/domain.clj) contains description of domain objects that the REST routes handle. Domain objects are also used when validating messages and in Swagger documentation generation.
  - [auth.clj](/src/ruuvi/resources/auth.clj) provides REST resources for authenticating and obtaining auth tokens.
  - [events.clj](/src/ruuvi/resources/auth.clj) TODO

- `src/ruuvi/services/`
  - [http_server.clj](/src/ruuvi/http_server.clj) is a Component that provides a web server. The web server is implemented with [Jetty](http://www.eclipse.org/jetty/).
  - [nrepl.clj](/src/ruuvi/http_server.clj) is a Component starts up a [nREPL](https://github.com/clojure/tools.nrepl) server. You can connect to the server and inspect and execute code in a running system.

- `dev/
  - [user.clj](/dev/user.clj) contains functions that are used when developing. Mainly starting and stopping components.

- `test/` contains unit and integration tests.
  - tests are implemented using [midje](https://github.com/marick/Midje) framework.
