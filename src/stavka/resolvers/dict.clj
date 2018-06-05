(ns stavka.resolvers.dict
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defrecord DictionaryResolver []
  sp/Resolver
  (resolve [this dict key]
    (let [key-path (str/split key #"\.")]
      (get-in dict key-path))))

(def instance (DictionaryResolver.))

(defn resolver [] instance)
