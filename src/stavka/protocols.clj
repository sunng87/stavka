(ns stavka.protocols)

(defprotocol Source
  (reload! [this] "returns input stream for the source"))

(defprotocol Updater
  (start! [this])
  (stop! [this]))

(defprotocol Resolver
  (resolve [this key]))
