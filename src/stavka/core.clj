(ns stavka.core
  (:require [stavka.protocols :as sp]
            [stavka.resolvers.env]
            [stavka.resolvers.options]
            [stavka.resolvers.dict]
            [stavka.resolvers.properties]
            [stavka.sources.file]
            [stavka.sources.url]
            [stavka.formats.json]
            [stavka.formats.yaml]
            [stavka.formats.properties]
            [stavka.utils :as utils])
  (:import [java.util Properties]))

(defrecord ConfigHolder [state source updater format resolver listeners])

;; source loaders
(utils/import-var file stavka.sources.file/file)
(utils/import-var url stavka.sources.url/url)
(utils/import-var classpath stavka.sources.file/classpath)

;; helper
(defn- load-from-source! [holder]
  (let [stream (sp/reload (.-source holder))
        result (sp/parse (.-format holder) stream)]
    ;; TODO: trigger listener
    (reset! (.-state holder) result)))

;; resolvers
(defn env
  "Environment variables as configuration source."
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.env/resolver) []))

(defn options
  "JVM option as configuration source."
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.options/resolver) []))

(defn json
  "JSON configuration from some source"
  [source]
  (let [holder (ConfigHolder. (atom {})
                              source
                              nil ;; updater
                              (stavka.formats.json/the-format)
                              (stavka.resolvers.dict/resolver)
                              [])]
    (load-from-source! holder)
    holder))

(defn properties
  "java.util.Properties from some source"
  [source]
  (let [holder (ConfigHolder. (atom (Properties.))
                              source
                              nil ;; updater
                              (stavka.formats.properties/the-format)
                              (stavka.resolvers.properties/resolver)
                              [])]
    (load-from-source! holder)
    holder))

(defn yaml
  "YAML configuration from source source"
  [source]
  (let [holder (ConfigHolder. (atom {})
                              source
                              nil ;; update
                              (stavka.formats.yaml/the-format)
                              (stavka.resolvers.dict/resolver)
                              [])]
    (load-from-source! holder)
    holder))

(defmacro using
  "Put your configuration sources inside to create a configuration store."
  [& holders]
  `(reverse (vector ~@holders)))

(defn get-config
  "Get configuration item from store."
  ([holders key] (get-config holders key nil))
  ([holders key default-value]
   (or (->> holders
            (map #(sp/resolve (.-resolver %) (utils/deref-safe (.-state %)) (name key)))
            (filter some?)
            first)
       default-value)))
