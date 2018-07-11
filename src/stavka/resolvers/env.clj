(ns stavka.resolvers.env
  (:require [stavka.protocols :as sp]
            [clojure.string :as str]))

(defn- transform-env-key [k {:keys [disable-underscore-to-dot?]}]
  (as-> k k*
    (str/lower-case k*)
    (if-not disable-underscore-to-dot?
      (str/replace k* #"_" ".")
      k*)))

(defn transform-env-keys [m options]
  (into {} (map #(vector (transform-env-key (key %) options)
                         (val %))
                m)))

(defrecord EnvironmentVariableResolver [envs]
  sp/Resolver
  (resolve [_ _ key]
    (envs key))
  (initial-state [_]
    nil))

(defn resolver
  "Resolve key from environment variables."
  [options]
  (EnvironmentVariableResolver. (transform-env-keys (System/getenv) options)))
