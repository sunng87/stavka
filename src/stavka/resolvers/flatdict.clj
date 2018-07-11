(ns stavka.resolvers.flatdict
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defrecord FlatDictionaryResolver []
  sp/Resolver
  (resolve [this dict k]
    (dict k))
  (initial-state [this] {}))

(def instance (FlatDictionaryResolver.))

(defn resolver
  "Flat dictionary resolver. Get the value via key directly."
  [] instance)
