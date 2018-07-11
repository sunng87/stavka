(ns stavka.resolvers.properties
  (:require [stavka.protocols :as sp])
  (:import [java.util Properties]))

(defrecord PropertiesResolver []
  sp/Resolver
  (resolve [this prop key]
    (.getProperty ^Properties prop ^String key))
  (initial-state [this]
    (Properties.)))

(def instance (PropertiesResolver.))

(defn resolver
  "Resolve java Properties"
  [] instance)
