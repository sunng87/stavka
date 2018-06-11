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
  (if-let [file (:file file-source)]
    [file-source
     (fn [reload-fn]
       (FileWatchUpdater. file (atom {}) reload-fn))]
    (throw (IllegalArgumentException. "Watcher can only work with file source."))))
