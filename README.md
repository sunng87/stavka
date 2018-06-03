# stavka

[![Build Status](https://travis-ci.org/sunng87/stavka.png?branch=master)](https://travis-ci.org/sunng87/stavka)
[![Clojars](https://img.shields.io/clojars/v/stavka.svg)](https://clojars.org/stavka)
[![License](https://img.shields.io/badge/license-eclipse-blue.svg)](https://github.com/sunng87/stavka/blob/master/LICENSE)
[![Donate](https://img.shields.io/badge/donate-liberapay-yellow.svg)](https://liberapay.com/Sunng/donate)

Stavka (Ставка) is the high command of your clojure application,
which manages configuration from various sources.

This project is still a work in progress. Not all APIs were implemented.

## Usage

```clj
(require '[stavka.core :as sta :refer :all])

(def config
    ;; load configuration from multiple sources and merge them like
    ;; clojure.core/merge.
    (sta/using
        ;; using environment variables by default
        (env)
        ;; also load properties from classpath
        (properties (classpath "/default.properties"))
        ;; and fetch a remote json configuration. check every 10 seconds
        ;; for update.
        (poll (json (url "http://somehost/configuration/my.json"))
            10000)))

;; get configuration
(get-int-config config :some.config.key)
```

## Features

* Extensible configuration sources and formats
  * Sources:
    * Classpath
    * File system
    * URL
    * JDBC (to be provided as example of extending stavka)
  * Formats:
    * Environment variables
    * JSON
    * YAML
    * Properties
* Reloading by
  * Watching file system
  * Polling the source
  * JMX
* Listeners for source changing
* Type conversion

## License

Copyright © 2018 Ning Sun

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## Donation

I'm now accepting donation on [liberapay](https://liberapay.com/Sunng/donate),
if you find my work helpful and want to keep it going.
