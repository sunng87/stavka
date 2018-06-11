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

(defrecord UpdaterSourceHolder [source updater])

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

(defn- holder-from-source [source initial-state format resolver]
  (let [[source updater-factory] (if (vector? source)
                                   source
                                   [source nil])
        holder-ref (promise)
        updater (when updater-factory
                  (updater-factory #(when-let [inner @holder-ref]
                                      (load-from-source! inner))))
        holder (ConfigHolder. (atom initial-state)
                              source
                              updater
                              format
                              resolver
                              [])]
    (deliver holder-ref holder)
    (load-from-source! holder)
    (when updater
      (sp/start! updater))
    holder))

;; resolvers
(defn env
  "Environment variables as configuration source."
  ;; TODO: env separator transformer
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.env/resolver) []))

(defn options
  "JVM option as configuration source."
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.options/resolver) []))

(defn json
  "JSON configuration from some source"
  [source]
  (holder-from-source source {}
                      (stavka.formats.json/the-format)
                      (stavka.resolvers.dict/resolver)))

(defn properties
  "java.util.Properties from some source"
  [source]
  (holder-from-source source (Properties.)
                      (stavka.formats.properties/the-format)
                      (stavka.resolvers.properties/resolver)))

(defn yaml
  "YAML configuration from source source"
  [source]
  (holder-from-source source {}
                      (stavka.formats.yaml/the-format)
                      (stavka.resolvers.dict/resolver)))

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
