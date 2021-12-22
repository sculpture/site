# Sculpture App



## Notes

  - backend:
    - [clojure](https://clojure.org/)
    - provides a REST API

    - data
      - [the data repo](https://github.com/sculpture/data) is "source of truth"
      - postgres is effectively used as a "cache" for the API (plus, makes it easy to do complicated queries and geospatial queries)
      - data is persisted both to postgres and to [the data repo](https://github.com/sculpture/data) (on [the web branch](https://github.com/sculpture/data/tree/web))

     - images
       - persisted to S3
       - transformed w/ imagemagick + mozjpeg


  - frontend:
    - [clojurescript](https://clojurescript.org/)
    - [re-frame](https://github.com/Day8/re-frame) / [reagent](https://github.com/reagent-project/reagent) / [react](https://facebook.github.io/react/) for client side state + UI
    - [garden](https://github.com/noprompt/garden) for CSS
    - [fuse](http://fusejs.io/) for client-side search index



## Getting Started

### Install System Dependencies

 - java
 - leiningen
 - imagemagick
 - mozjpeg (or, just cjpeg)
 - postgres w/ postgis


### Set Up Database

- install postgres and postgis
     (on Mac [PostgresApp](https://postgresapp.com/) is recommended)

- Create a database user:
  `createuser sculpture`

- Assign password to the user:
  `psql postgres -c "alter user sculpture with password 'sculpture';`

- Create the database:
  `createdb -O sculpture sculpture`


### Setting Up App

- In CLI, clone this repo:
  `git clone git@github.com:sculpture/site.git`


### Set Up Config Vars

- Modify the config vars:
  create an `config.edn` in your preferred editor
  (see `src/sculpture/config.clj` for schema)


### Standard Dev Session

- In CLI, start the REPL:
  `lein repl`

- In the REPL, launch the server:
  `(start-server!)`

- If necessary, re-import database:
  `(db/reload!)`

- In a second CLI, start client-side js compiler/watcher:
  `rlwrap lein figwheel`

- In browser, go to `http://localhost:2469/`

