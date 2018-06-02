# stavka

Stavka (Ставка) is the high command of your clojure application,
which manages configuration from various sources.

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
        (properties (file "classpath:/default.properties"))
        ;; and fetch a remote json configuration. check every 10 seconds
        ;; for update.
        (poll (json (url "http://somehost/configuration/my.json"))
            10000)))

;; get configuration
(get-int-config config :some.config.key)
```

## License

Copyright © 2018 Ning Sun

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
