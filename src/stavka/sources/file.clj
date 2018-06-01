(ns stavka.sources.file
  (:require [stavka.protocols :as sp]
            [clojure.java.io :as io]))

(defrecord FileLoader [filepath]
  sp/Source
  (reload! [this]
    (io/input-stream (.-filepath this))))

(defn file [path]
  (FileLoader. path))
