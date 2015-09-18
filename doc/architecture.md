# Ruuvi-Server

# Components

Ruuvi-Server uses plain [Stuart Sierra style components](https://github.com/stuartsierra/component) to componentize the system and provice dependency injection. [system.clj](/src/ruuvi/system.clj) wires up the components to form the full application.

`repl.clj` -

# Routing and REST

Routing is handled by [rook](https://github.com/AvisoNovate/rook). [ring-handler.clj](/src/ruuvi/ring_handler.clj) sets up web stack, including authentication, generated Swagger documentation, [Schema](https://github.com/Prismatic/schema) based validation for incoming requests.

REST resources are defined in directory `src/ruuvi/resources`.

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

The idea is that once a migration file is released as a part of the application, it is never changed. All the fixes to the database are done by adding new migration files thus providing an upgrade path to later version. An old version of database can always be updated to lates version just by applying missing migrations in order.


# Domain objects


