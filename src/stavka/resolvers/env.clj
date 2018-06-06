(ns stavka.resolvers.env
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defn transform-env-key [m]
  (into {} (map #(vector (str/lower-case (key %)) (val %)) m)))

(defrecord EnvironmentVariableResolver [envs]
  sp/Resolver
  (resolve [_ _ key]
    (envs key)))

(def instance (EnvironmentVariableResolver. (transform-env-key (System/getenv))))

(defn resolver [] instance)
