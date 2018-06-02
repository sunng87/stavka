(ns stavka.core
  (:require [stavka.protocols :as sp]
            [stavka.resolvers.env]
            [stavka.resolvers.dict]
            [stavka.sources.file]
            [stavka.sources.url]
            [stavka.formats.json]
            [stavka.utils :as utils]))

(defrecord ConfigHolder [state source updater format resolver listeners])

;; resolvers
(defn env
  "Environment variables as configuration source."
  []
  (ConfigHolder. nil nil nil nil (stavka.resolvers.env/resolver) []))

;; source loaders
(utils/import-var file stavka.sources.file/file)
(utils/import-var url stavka.sources.url/url)
(utils/import-var classpath stavka.sources.file/classpath)

(defn- load-from-source! [holder]
  (let [stream (sp/reload (.-source holder))
        result (sp/parse (.-format holder) stream)]
    ;; TODO: trigger listener
    (reset! (.-state holder) result)))

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

(defmacro using
  "Put your configuration sources inside to create a configuration store."
  [& holders]
  `(reverse (vector ~@holders)))

(defn get-config
  "Get configuration item from store."
  ([holders key] (get-config holders key nil))
  ([holders key default-value]
   (or (->> holders
            (map #(sp/resolve (.-resolver %) (utils/deref-safe (.-state %)) key))
            (filter some?)
            first)
       default-value)))
