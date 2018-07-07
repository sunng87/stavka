(ns stavka.resolvers.flatdict
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defrecord FlatDictionaryResolver []
  sp/Resolver
  (resolve [this dict k]
    (dict k)))

(def instance (FlatDictionaryResolver.))

(defn resolver [] instance)
