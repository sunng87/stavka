(ns stavka.core
  (:require [stavka.protocols :as sp]
            [stavka.resolvers.env]
            [stavka.resolvers.options]
            [stavka.resolvers.dict]
            [stavka.resolvers.properties]
            [stavka.sources.file]
            [stavka.sources.url]
            [stavka.updaters.watcher]
            [stavka.updaters.poller]
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

(defn holder-from-source
  "Create a holder from source readable source, useful when creating your own
  configuration format"
  [source initial-state format resolver]
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
  [& {:as options}]
  (ConfigHolder. nil nil nil nil (stavka.resolvers.env/resolver options) []))

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

;; updaters
(utils/import-var watch stavka.updaters.watcher/watch)
(utils/import-var poll stavka.updaters.poller/poll)

(defmacro using
  "Put your configuration sources inside to create a configuration store."
  [& holders]
  `(reverse (vector ~@holders)))

(defonce global-config (atom nil))

(defmacro global!
  "Create global stavka configuration."
  [& holders]
  `(let [config# (using ~@holders)]
     (reset! global-config config#)))

(defn $
  "Get configuration item from store."
  ([key] (when-let [holders @global-config]
           ($ holders key)))
  ([holders key] ($ holders key nil))
  ([holders key default-value]
   (or (->> holders
            (map #(sp/resolve (.-resolver %) (utils/deref-safe (.-state %)) (name key)))
            (filter some?)
            first)
       default-value)))

(defn stop-updaters! [holders]
  (doseq [h holders]
    (when-let [updater (.-updater h)]
      (sp/stop! updater))))
