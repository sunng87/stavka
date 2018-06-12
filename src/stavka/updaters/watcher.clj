(ns stavka.updaters.watcher
  (:require [hawk.core :as hawk]
            [stavka.protocols :as sp]))

(defrecord FileWatchUpdater [file state reload-fn]
  sp/Updater
  (start! [this]
    (let [watcher (hawk/watch! [{:paths [file]
                                 :handler (fn [ctx e] (reload-fn))}])]
      (swap! (.-state this) assoc :watcher watcher)))
  (stop! [this]
    (when-let [watcher (:watcher @(.-state this))]
      (hawk/stop! watcher))))

(defn watch
  "Watch file system change and reload source, works on JDK7 and above. Note that watch updater can only work with file source."
  [file-source]
  (if-let [file (:filepath file-source)]
    [file-source
     (fn [reload-fn]
       (FileWatchUpdater. file (atom {}) reload-fn))]
    (throw (IllegalArgumentException. "Watcher can only work with file source."))))
