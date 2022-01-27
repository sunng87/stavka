(ns stavka.updaters.watcher2
  (:require [stavka.protocols :as sp]
            [nextjournal.beholder :as beholder])
  (:import [java.nio.file Path Paths]))

(defn- into-path
  [^String file-path]
  (Paths/get file-path (into-array String [])))

(defn- parent-dir [^Path path]
  (.. path
      (getParent)
      (toString)))

(defrecord BeholderWatchUpdater [file state reload-fn]
  sp/Updater
  (start! [this]
    (let [path (into-path file)
          dir-name (parent-dir path)
          watcher (beholder/watch (fn [e]
                                    (when (= (:path e) path)
                                      (reload-fn)))
                                  dir-name)]
      (swap! (.-state this) assoc :watcher watcher)))
  (stop! [this]
    (when-let [watcher (:watcher @(.-state this))]
      (beholder/stop watcher))))

(defn watch
  "Watch file system change and reload source, works on JDK 8 and above. This function is based on [beholder](https://github.com/nextjournal/beholder) and [directory-watcher](https://github.com/gmethvin/directory-watcher), which has a watch service on Mac OS instead of default poll based implementation."
  [file-source]
  (if-let [file (:filepath file-source)]
    [file-source
     (fn [reload-fn]
       (BeholderWatchUpdater. file (atom {}) reload-fn))]
    (throw (IllegalArgumentException. "Watcher can only work with file source."))))
