(ns stavka.formats.json
  (:require [stavka.protocols :as sp]
            [cheshire.core :as json]
            [clojure.java.io :as io]))

(defrecord JsonFormat []
  sp/Format
  (parse [this ins]
    (json/parse-stream (io/reader ins))))

(def instance (JsonFormat.))

(defn the-format [] instance)
