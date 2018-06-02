(ns stavka.sources.file
  (:require [stavka.protocols :as sp]
            [clojure.java.io :as io]))

(defrecord FileLoader [filepath]
  sp/Source
  (reload [this]
    (io/input-stream (.-filepath this))))

(defn file
  "returns a file loader for a file in file system"
  [path]
  (FileLoader. path))

(defrecord ClasspathLoader [classpath]
  sp/Source
  (reload [this]
    (.getResourceAsStream (class this) (.-classpath this))))

(defn classpath
  "returns a file loader from a file in classpath"
  [path]
  (ClasspathLoader. path))
