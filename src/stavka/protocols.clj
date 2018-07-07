(ns stavka.protocols
  (:refer-clojure :exclude [resolve]))

(defprotocol Source
  (reload [this] "returns input stream for the source"))

(defprotocol Updater
  (start! [this] "start updater")
  (stop! [this] "stop updater"))

(defprotocol Resolver
  (resolve [this data key] "resolve key")
  (initial-state [this] "the default state for this type of resolver"))

(defprotocol Format
  (parse [this input-stream] "parse configuration from input-stream"))
