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
            [stavka.formats.edn]
            [stavka.formats.yaml]
            [stavka.formats.properties]
            [stavka.utils :as utils]))

(defrecord ConfigHolder [state source updater format resolver parent])

(defrecord ConfigHolders [holders listeners])

(defn config-holders [holders-coll]
  (let [holders (ConfigHolders. holders-coll (atom []))]
    (doseq [h holders-coll]
      (reset! (.-parent h) holders))
    holders))

(defrecord UpdaterSourceHolder [source updater])

;; source loaders
(utils/import-var file stavka.sources.file/file)
(utils/import-var url stavka.sources.url/url)
(utils/import-var classpath stavka.sources.file/classpath)

(declare $)

;; helper
(defn- load-from-source! [holder reload?]
  (when-let [stream (sp/reload (.-source holder))]
    (let [result (sp/parse (.-format holder) stream)]
      (if reload?
        (when-let [parent @(.parent holder)]
          (let [listeners @(.-listeners parent)
                interested (mapv #($ parent (:key %)) listeners)]
            (reset! (.-state holder) result)
            (let [updated-intereseted (mapv #($ parent (:key %)) listeners)]
              ;; check all monitored keys
              (doseq [[{config-key :key callback :callback} p c] (map list listeners interested updated-intereseted)]
                ;; value changed, trigger callback
                (when (not= p c)
                  (callback c p))))))
        (do
          (reset! (.-state holder) result))))))

(defn holder-from-source
  "Create a holder from source readable source, useful when creating your own
  configuration format"
  [source format resolver]
  (let [[source updater-factory] (if (vector? source)
                                   source
                                   [source nil])
        holder-ref (promise)
        updater (when updater-factory
                  (updater-factory #(when-let [inner @holder-ref]
                                      (load-from-source! inner true))))
        holder (ConfigHolder. (atom (sp/initial-state resolver))
                              source
                              updater
                              format
                              resolver
                              (atom nil))]
    (deliver holder-ref holder)
    (load-from-source! holder false)
    (when updater
      (sp/start! updater))
    holder))

;; resolvers
(defn env
  "Environment variables as configuration source. Environment variable key is
  lowercased and had `_` replaced by `.`.
  Options:
  `:disable-underscore-to-dot?` disabling transforming underscore to dot"
  [& {:as options}]
  (ConfigHolder. nil nil nil nil (stavka.resolvers.env/resolver options) (atom nil)))

(defn options
  "JVM option as configuration source."
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.options/resolver) (atom nil)))

(defn json
  "JSON configuration from some source"
  [source]
  (holder-from-source source
                      (stavka.formats.json/the-format)
                      (stavka.resolvers.dict/resolver)))

(defn edn
  "EDN configuration from some source"
  [source]
  (holder-from-source source
                      (stavka.formats.edn/the-format)
                      (stavka.resolvers.dict/resolver)))

(defn properties
  "java.util.Properties from some source"
  [source]
  (holder-from-source source
                      (stavka.formats.properties/the-format)
                      (stavka.resolvers.properties/resolver)))

(defn yaml
  "YAML configuration from source source"
  [source]
  (holder-from-source source
                      (stavka.formats.yaml/the-format)
                      (stavka.resolvers.dict/resolver)))

;; updaters
(utils/import-var watch stavka.updaters.watcher/watch)
(utils/import-var poll stavka.updaters.poller/poll)

(defmacro using
  "Put your configuration sources inside to create a configuration store."
  [& holders]
  `(config-holders (reverse (vector ~@holders))))

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
   (or (->> (.-holders holders)
            (map #(sp/resolve (.-resolver %) (utils/deref-safe (.-state %)) (name key)))
            (filter some?)
            first)
       default-value)))

(defn $l
  "Get configuration as long"
  ([key] (when-let [holders @global-config]
           ($l holders key)))
  ([holders key] ($l holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Long/valueOf c)
     (or default-value 0))))

(defn $f
  "Get configuration as double"
  ([key] (when-let [holders @global-config]
           ($f holders key)))
  ([holders key] ($f holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Double/valueOf c)
     (or default-value 0))))

(defn $s
  "Get configuration as string"
  ([key] (when-let [holders @global-config]
           ($s holders key)))
  ([holders key] ($s holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (str c)
     default-value)))

(defn $b
  "Get configuration as boolean"
  ([key] (when-let [holders @global-config]
           ($b holders key)))
  ([holders key] ($b holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Boolean/valueOf c)
     (or default-value false))))

(defn stop-updaters!
  "Stop updater associated with holders."
  ([] (when-let [holders @global-config]
        (stop-updaters! holders)))
  ([holders] (doseq [h (.-holders holders)]
               (when-let [updater (.-updater h)]
                 (sp/stop! updater)))))

(defn on-change!
  "Register a listener on configuration change"
  ([config-key callback]
   (when-let [holders @global-config]
     (on-change! holders config-key callback)))
  ([holders config-key callback]
   (swap! (.-listeners holders) conj {:key config-key
                                      :callback callback})))
