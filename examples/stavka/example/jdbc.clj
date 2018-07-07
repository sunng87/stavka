(ns stavka.example.jdbc
  (:require [clojure.java.jdbc :as j]
            [stavka.core :as s]
            [stavka.protocols :as sp]
            [stavka.resolvers.flatdict]
            [stavka.formats.none]))

(defrecord DBLoader [dbspec options]
  sp/Source
  (reload [this]
    (try
      (let [results (j/query dbspec ["SELECT key,value FROM config"])]
        (into {} (map #(vector (:key %) (:value %)) results)))
      (catch Throwable e
        (when-not (:quiet? options) (throw e))))))

(defn db-source [dbspec & {:as options}]
  (DBLoader. dbspec options))

;; an example on extending stavka sources
(defn jdbc [db-spec]
  (s/holder-from-source (db-source db-spec) {}
                        (stavka.formats.none/the-format)
                        (stavka.resolvers.flatdict/resolver)))

(defn -main [& args]
  (let [db-spec {:user "SA"
                 :password ""
                 :connection-uri "jdbc:hsqldb:mem:mymemdb"}
        ddl (j/create-table-ddl :config
                                [[:key "VARCHAR(64)"]
                                 [:value "VARCHAR(128)"]])]
    (j/db-do-commands db-spec ddl)
    (j/insert-multi! db-spec :config [{:key "test.key1" :value "yes"}
                                      {:key "test.key2" :value "some,other,values"}])
    (s/global! (jdbc db-spec))
    (println "Getting config :test.key1" (s/$ :test.key1))))
