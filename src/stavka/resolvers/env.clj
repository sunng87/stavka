(ns stavka.resolvers.env
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defn transform-env-key [m options]
  (into {} (map #(vector (str/lower-case (key %)) (val %)) m)))

(defrecord EnvironmentVariableResolver [envs]
  sp/Resolver
  (resolve [_ _ key]
    (envs key)))

(defn resolver [options]
  (EnvironmentVariableResolver. (transform-env-key (System/getenv) options)))
