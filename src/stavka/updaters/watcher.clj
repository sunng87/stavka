(ns stavka.updaters.watcher
  (:require [hawk.core :as hawk]
            [stavka.protocols :as sp]))

(defrecord FileWatchUpdater [file state reload-fn]
  sp/Updater
  (start! [this]
    (let [watcher (hawk/watch! [{:paths file
                                 :handler (fn [ctx e] (reload-fn))}])]
      (swap! (.-state this) assoc :watcher watcher)))
  (stop! [this]
    (when-let [watcher (:watcher @(.-state this))]
      (hawk/stop! watcher))))

(defn watch [file-source]
  (fn [reload-fn]
    ;; FIXME: extract file from file-source
    (FileWatchUpdater. file-source (atom {}) reload-fn)))
