(ns stavka.resolvers.env
  (:require [environ.core :as e]
            [stavka.protocols :as sp]))

(defrecord EnvironmentVariableResolver []
  sp/Resolver
  (resolve [_ _ key]
    (e/env key)))

(def instance (EnvironmentVariableResolver.))

(defn resolver [] instance)
