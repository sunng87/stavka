(ns stavka.resolvers.args
  (:require [stavka.protocols :as sp]
            [clojure.tools.cli :as cli]))

(defrecord CliArgumentResolver [parsed-options]
  sp/Resolver
  (resolve [_ _ k]
    (get (:options parsed-options) (keyword k)))
  (initial-state [_] nil))

(defn resolver
  "Resolve key from commandline options"
  [args cli-opts]
  (CliArgumentResolver. (cli/parse-opts args cli-opts)))
