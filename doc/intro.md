# Introduction to stavka

Stavka (Ставка) is the high command of your clojure application,
which manages configuration from various sources. It also handles auto
update so you can adjust your application behavior at runtime.

## Getting Started

The stavka API has reloadable workflow in mind. You can integrate it
into your mount or component application.

```clj
(require '[stavka.core :as sta :refer :all])
```

Create the configuration by declaring format and source of it.

```clj
(def config
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
        (json (poll (url "http://somehost/configuration/my.json") 10000))))

```

Access the configuration item:

```clj
($ config :some.config.key)
```

By accessing `some.config.key` we will lookup this item first in the
json url, then two properties and finally fall back to environment
variables.
