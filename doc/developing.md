# Developers Guide


## Required software

* [Java 8 Development kit](http://www.oracle.com/technetwork/java/javase/overview/index.html) (or newer)
* [Leiningen](http://leiningen.org/) - build tool
* [git](https://git-scm.com/) - version control tool
* Some editor: e.g
  * [LightTable](http://lighttable.com/)
  * [Emacs](https://www.gnu.org/software/emacs/) + [CIDER](https://github.com/clojure-emacs/cider) plugin
  * [IntelliJ IDEA](https://www.jetbrains.com/idea/) + [Cursive](https://cursiveclojure.com/) plugin
  * [Eclipse](https://eclipse.org/) + [Counterclockwise](http://doc.ccw-ide.org/) plugin

Optionally you also need
* [PostgreSQL](http://www.postgresql.org/) database engine
* Account in [GitHub](https://github.com/)

## Getting the code

Clone repository:

```
git clone git@github.com:jsyrjala/turbulent-sniffle.git
```

Get dependencies and libraries:
```
lein deps
```

Run application:
```
lein run --config dev/config.edn
```

Point your web browser to http://localhost:7000/doc/index.html and you should see Swagger documentation for the REST api. The server is now running againts H2 database.

## Generating HTML documentation for source code

You can generate (Marginalia)[https://github.com/MichaelBlume/marginalia] based html documentation of the source code by running
```
lein marg
```
