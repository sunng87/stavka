(ns stavka.sources.file
  (:require [stavka.protocols :as sp]
            [clojure.java.io :as io]))

(defrecord FileLoader [filepath options]
  sp/Source
  (reload [this]
    (try
      (io/input-stream (.-filepath this))
      (catch Throwable e
        (when-not (:quiet? options) (throw e))))))

(defn file
  "returns a file loader for a file in file system"
  [path & {:as options}]
  (FileLoader. path options))

(defrecord ClasspathLoader [classpath options]
  sp/Source
  (reload [this]
    ;; getResourceAsStream returns nil when file not found, so
    ;; it won't throw any exception except when path is nil
    (.getResourceAsStream (class this) (.-classpath this))))

(defn classpath
  "returns a file loader from a file in classpath"
  [path & {:as options}]
  (ClasspathLoader. path options))
