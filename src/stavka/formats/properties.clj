(ns stavka.formats.properties
  (:require [stavka.protocols :as sp])
  (:import [java.util Properties]))

(defrecord PropertiesFormat []
  sp/Format
  (parse [this ins]
    (doto (Properties.)
      (.load ins))))

(def instance (PropertiesFormat.))

(defn the-format
  "Parse input-stream as Java properties"
  [] instance)
