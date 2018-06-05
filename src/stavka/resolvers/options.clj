(ns stavka.resolvers.options
  (:require [stavka.protocols :as sp]))

(defrecord JavaOptionResolver []
  sp/Resolver
  (resolve [_ _ key]
    (System/getProperty key)))

(def instance (JavaOptionResolver.))

(defn resolver [] instance)
