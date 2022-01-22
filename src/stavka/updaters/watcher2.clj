(ns stavka.updaters.watcher2
  (:require [stavka.protocols :as sp]
            [nextjournal.beholder :as beholder]))

(defrecord BeholderWatchUpdater [file state reload-fn]
  sp/Updater
  (start! [this]
    (let [watcher (beholder/watch (fn [_] (reload-fn)) file)]
      (swap! (.-state this) assoc :watcher watcher)))
  (stop! [this]
    (when-let [watcher (:watcher @(.-state this))]
      (beholder/stop watcher))))

(defn watch
  "Watch file system change and reload source, works on JDK7 and above. Note that watch updater can only work with file source."
  [file-source]
  (if-let [file (:filepath file-source)]
    [file-source
     (fn [reload-fn]
       (BeholderWatchUpdater. file (atom {}) reload-fn))]
    (throw (IllegalArgumentException. "Watcher can only work with file source."))))