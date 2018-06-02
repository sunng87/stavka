(ns stavka.resolvers.properties
  (:require [stavka.protocols :as sp])
  (:import [java.util Properties]))

(defrecord PropertiesResolver []
  sp/Resolver
  (resolve [this prop key]
    (.getProperty ^Properties prop ^String (name key))))

(def instance (PropertiesResolver.))

(defn resolver [] instance)
