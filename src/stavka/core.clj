(ns stavka.core
  (:require [stavka.protocols :as sp]
            [stavka.resolvers.env]
            [stavka.resolvers.options]
            [stavka.resolvers.dict]
            [stavka.resolvers.properties]
            [stavka.sources.file]
            [stavka.updaters.poller]
            [stavka.formats.edn]
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

(utils/if-provided
 'clj-http.client

 ;; when clj-http
 (do
   (require 'stavka.sources.url)
   (let [the-url-source (resolve 'stavka.sources.url/url)]
     (def url the-url-source)))

 ;; otherwise throws exception
 (defn url [_]
   (throw (UnsupportedOperationException.
           "clj-http not found on classpath. Url source is disbaled."))))

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

(utils/if-provided
 '[clojure.tools.cli]

 ;; when tools.cli is on classpath
 (do
   (require 'stavka.resolvers.args)
   (let [rsv @(resolve 'stavka.resolvers.args/resolver)]
     (defn cli
       "tools.cli configuration parser"
       [args parser]
       (ConfigHolder. nil nil nil nil (rsv args parser) (atom nil)))))

 (defn cli [& _]
   (throw (UnsupportedOperationException.
           "tools.cli is not on classpath, cli resolver is disabled"))))

(utils/if-provided
 '[cheshire.core]

 ;; when cheshire is on classpath
 (do
   (require 'stavka.formats.json)
   (let [fmt @(resolve 'stavka.formats.json/the-format)]
     (defn json
       "JSON configuration from some source"
       [source]
       (holder-from-source source (fmt)
                           (stavka.resolvers.dict/resolver)))))

 ;; else
 (defn json [_]
   (throw (UnsupportedOperationException.
           "cheshire is not on classpath, json format disabled."))))

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

(utils/if-provided
 '[clj-yaml.core]

 ;; clj-yaml provided
 (do
   (require 'stavka.formats.yaml)
   (let [fmt @(resolve 'stavka.formats.yaml/the-format)]
    (defn yaml
      "YAML configuration from source source"
      [source]
      (holder-from-source source (fmt)
                          (stavka.resolvers.dict/resolver)))))

 ;; else
 (defn yaml [_]
   (throw (UnsupportedOperationException.
           "clj-yaml is not on classpath, yaml format disabled."))))


;; updaters
(utils/if-provided
 'hawk.core

 ;; hawk is provided
 (do
   (require 'stavka.updaters.watcher)
   (let [the-watcher (resolve 'stavka.updaters.watcher/watch)]
     (def watch the-watcher)))

 ;; otherwise disable fs watcher
 (defn watch [_]
   (throw (UnsupportedOperationException.
           "hawk is not found on classpath, file system watching is disabled."))))

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
  ([holders key] ($ holders key nil))
  ([holders key default-value]
   (or (->> (.-holders holders)
            (map #(sp/resolve (.-resolver %) (utils/deref-safe (.-state %)) (name key)))
            (filter some?)
            first)
       default-value)))

(defn $$
  "Get configuration item from the global store."
  ([key] ($$ key nil))
  ([key default-value]
   (if-let [holders @global-config]
     ($ holders key default-value)
     (throw (ex-info "No global config available. Use stavka.core/global! to setup" {})))))

(defn $l
  "Get configuration as long"
  ([holders key] ($l holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Long/valueOf c)
     default-value)))

(defn $$l
  "Get configuration from the global store as long"
  ([key] ($$l key nil))
  ([key default-value]
   (if-let [holders @global-config]
     ($l holders key default-value)
     (throw (ex-info "No global config available. Use stavka.core/global! to setup")))))

(defn $f
  "Get configuration as double"
  ([holders key] ($f holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Double/valueOf c)
     default-value)))

(defn $$f
  "Get configuration from the global store as double"
  ([key] ($$f key nil))
  ([key default-value]
   (if-let [holders @global-config]
     ($f holders key default-value)
     (throw (ex-info "No global config available. Use stavka.core/global! to setup" {})))))

(defn $s
  "Get configuration as string"
  ([holders key] ($s holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (str c)
     default-value)))

(defn $$s
  "Get configuration from the global store as string"
  ([key] ($$s key nil))
  ([key default-value]
   (if-let [holders @global-config]
     ($s holders key default-value)
     (throw (ex-info "No global config available. Use stavka.core/global! to setup" {})))))

(defn $b
  "Get configuration as boolean"
  ([holders key] ($b holders key nil))
  ([holders key default-value]
   (if-let [c ($ holders key)]
     (Boolean/valueOf c)
     default-value)))

(defn $$b
  "Get configuration from the global store as boolean"
  ([key] ($$b key nil))
  ([key default-value]
   (if-let [holders @global-config]
     ($b holders key default-value)
     (throw (ex-info "No global config available. Use stavka.core/global! to setup" {})))))

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
