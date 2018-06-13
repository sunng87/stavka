(ns stavka.updaters.poller
  (:require [stavka.protocols :as sp])
  (:import [java.util.concurrent Executors ThreadFactory TimeUnit]))

(def stavka-poller-thread-factory
  (let [prefix "stavka-poller-thread-"
        counter (atom -1)]
    (reify ThreadFactory
      (newThread [this r]
        (doto (Thread. r)
          (.setName (str prefix (swap! counter inc)))
          (.setDaemon true)
          (.start))))))

(defonce poller-pool
  (delay (Executors/newScheduledThreadPool 8 stavka-poller-thread-factory)))

(defrecord PollingUpdater [interval-ms state reload-fn]
  sp/Updater
  (start! [this]
    (let [future (.scheduleAtFixedDelay @poller-pool
                                        (cast Runnable (fn []
                                                         (try
                                                           (reload-fn)
                                                           (catch Throwable _))))
                                        interval-ms
                                        interval-ms
                                        TimeUnit/SECONDS)]
      (swap! (.-state this) assoc :future future)))
  (stop! [this]
    (when-let [future (:future @(.-state this))]
      (.cancel future true))))

(defn poll
  "Updater configuration by polling source at given interval"
  [source interval]
  [source
   (fn [reload-fn]
     (PollingUpdater. interval (atom {}) reload-fn))])
