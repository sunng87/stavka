(ns stavka.formats.edn
  (:require [stavka.protocols :as sp]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(defrecord EdnFormat []
  sp/Format
  (parse [this ins]
    (edn/read (PushbackReader. (io/reader ins)))))

(def instance (EdnFormat.))

(defn the-format
  "Deserialize input-stream as EDN"
  []
  instance)
