(ns stavka.formats.yaml
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [stavka.protocols :as sp]))

(defrecord YamlFormat []
  sp/Format
  (parse [this ins]
    (yaml/parse-string (slurp (io/reader ins)) :keywords false)))

(def instance (YamlFormat.))

(defn the-format
  "Deserialize input-stream as Yaml to map"
  [] instance)
