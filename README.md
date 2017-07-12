# Sculpture App





## Getting Started

create a `profiles.clj` (see `profiles.sample.clj`)

- In CLI, start the REPL:
  `lein with-profiles +sculpture repl`

- In the REPL, launch the server:
  `(start! 2469)`

- In a second CLI, start client-side js compiler/watcher:
  `rlwrap lein figwheel`

- In browser, go to `http://localhost:2469/`


## Other Dependencies

 - imagemagick
 - mozjpeg
