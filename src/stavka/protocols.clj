(ns stavka.protocols)

(defprotocol Source
  (reload [this]))

(defprotocol Updater
  (start! [this])
  (stop! [this]))

(defprotocol Resolver
  (resolve [this key]))
