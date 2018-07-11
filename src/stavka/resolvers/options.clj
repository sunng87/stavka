(ns stavka.resolvers.options
  (:require [stavka.protocols :as sp]))

(defrecord JavaOptionResolver []
  sp/Resolver
  (resolve [_ _ key]
    (System/getProperty key))
  (initial-state [_] nil))

(def instance (JavaOptionResolver.))

(defn resolver
  "Resolve key from JVM options."
  [] instance)
