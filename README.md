# Sculpture App



## Notes

  - backend:
    - [clojure](https://clojure.org/)
    - provides a REST API

    - data
      - at run time, data is stored in:
         - postgres (for geospatial queries)
         - datascript (for all other data)
      - ...bridged by pathom3
      - data is persisted to:
         - yaml files in a git repo (https://github.com/sculpture/data) (on [the web branch](https://github.com/sculpture/data/tree/web))
         - which is considered the "source of truth"

     - images
       - persisted to S3
       - transformed w/ imagemagick + mozjpeg

  - frontend:
    - [clojurescript](https://clojurescript.org/)
    - [re-frame](https://github.com/Day8/re-frame) / [reagent](https://github.com/reagent-project/reagent) / [react](https://facebook.github.io/react/) for client side state + UI
    - [garden](https://github.com/noprompt/garden) for CSS



## Getting Started

### Install System Dependencies

 - java
 - leiningen
 - imagemagick
 - mozjpeg
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
  `(start!)`

- If necessary, re-import database:
  `(db/reload!)`

- In browser, go to `http://localhost:2469/`

