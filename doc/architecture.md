# Ruuvi-Server

# Components

Ruuvi-Server uses plain [Stuart Sierra style components](https://github.com/stuartsierra/component) to componentize the system and provice dependency injection. `system.clj` wires up the components to form the full application.

`repl.clj` -

# Routing and REST

Routing is handled by [rook](https://github.com/AvisoNovate/rook). `ring-handler.clj` sets up web stack, including authentication, generated Swagger documentation, [Schema](https://github.com/Prismatic/schema) based validation for incoming requests.

REST resources are defined in directory `src/ruuvi/resources`.

## Swagger

## Authentication

## Validation


# Database

## Migrations

Migrations are managed by [ragtime](https://github.com/weavejester/ragtime) library.

Information which migrations have been executed is stored to database table `ragtime_migrations`.

# Domain objects


