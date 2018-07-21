# stavka

[![Build Status](https://travis-ci.org/sunng87/stavka.png?branch=master)](https://travis-ci.org/sunng87/stavka)
[![Clojars](https://img.shields.io/clojars/v/stavka.svg)](https://clojars.org/stavka)
[![License](https://img.shields.io/badge/license-eclipse-blue.svg)](https://github.com/sunng87/stavka/blob/master/LICENSE)
[![Donate](https://img.shields.io/badge/donate-liberapay-yellow.svg)](https://liberapay.com/Sunng/donate)

Stavka (Ставка) is the high command of your clojure application,
which manages configuration from various sources.

## Features

* Extensible configuration sources and formats
  * Sources:
    * Classpath `(classpath)`
    * File system `(file)`
    * URL `(url)`
    * JDBC (see [examples](https://github.com/sunng87/stavka/blob/master/examples/stavka/example/jdbc.clj))
  * Formats:
    * Environment variables `(env)`
    * JVM options (-D) `(options)`
    * JSON `(json)`
    * YAML `(yaml)`
    * Properties `(property)`
* Reloading by
  * Watching file system `(watch)`
  * Polling the source `(poll)`
* Listeners for value changing `(on-change!)`
* Type conversion `($l) ($f) ($b) ($s)`


## Usage

Use stavka with [component](https://github.com/stuartsierra/component)
or [mount](https://github.com/tolitius/mount). You can have multiple
config instance and manage life-cycle of updater.

```clj
(require '[stavka.core :as sta :refer :all])

;; Use stavka with mount
(defstate config
    :start
    ;; load configuration from multiple sources and merge them like
    ;; clojure.core/merge.
    (sta/using
        ;; using environment variables by default
        (env)
        ;; also load properties from classpath
        (properties (classpath "/default.properties"))
        ;; load another properties from filesystem, and watch is for change
        (properties (watch (file "/etc/stavka.properties")))
        ;; and fetch a remote json configuration. check every 10 seconds
        ;; for update.
        (json (poll (url "http://somehost/configuration/my.json") 10000)))

    :stop (stop-updaters! config))

;; Use stavka with component
(defrecord StavkaConfiguration [config]
    component/Lifecycle
    (start [component]
        (assoc component :config
            (sta/using
                (env)
                (properties (classpath "/default.properties"))
                (properties (watch (file "/etc/stavka.properties")))
                (json (poll (url "http://somehost/configuration/my.json") 10000)))))
    (stop [component]
        (stop-updaters! config)
        (assoc component :config nil)))
```

Get configuration item:

```clj
;; get configuration
($ config :some.config.key)

;; get configuration with type convertion
;; $l: as long
;; $f: as double
;; $s: as string
;; $b: as boolean
($l config :some.config.key)
```

And you can still use stavka globally:

```clj
(sta/global!
    (env)
    (properties (classpath "/default.properties"))
    (properties (watch (file "/etc/stavka.properties")))
    (json (poll (url "http://somehost/configuration/my.json") 10000)))

($l :some.config.key)
```

Add change listener on some key when you have updater configured:

```clj
(on-change! config :some.config.key
    (fn [new-value previous-value]
        ))
```

## License

Copyright © 2018 Ning Sun

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## Donation

I'm now accepting donation on [liberapay](https://liberapay.com/Sunng/donate),
if you find my work helpful and want to keep it going.
