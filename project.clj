(defproject stavka "0.7.0"
  :description "Stavka manages configuration, for your clojure application"
  :url "https://github.com/sunng87/stavka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.15.2" :scope "test"]
                                  [cheshire "5.10.1"]
                                  [clj-commons/clj-yaml "0.7.107"]
                                  [clj-http "3.12.3"]
                                  [hawk "0.2.11"]]
                   :jvm-opts ["-Dstavka.test.attr=yes"]
                   :test-selectors {:default [#(not= (str %) "stavka.minimal-test") (constantly true)]}}

             :example {:dependencies [;; jdbc
                                      [org.clojure/java.jdbc "0.7.12"]
                                      [org.hsqldb/hsqldb "2.6.0"]
                                      ;; kubernetes
                                      [io.fabric8/kubernetes-client "5.7.2"]
                                      ;; vault
                                      [amperity/vault-clj "1.0.6"]]
                       :source-paths ["examples"]}

             :minimal {:test-selectors {:default [#(= (str %) "stavka.minimal-test") :minimal]}}}
  :plugins [[lein-codox "0.10.4"]]
  :codox {:output-path "target/codox"
          :source-uri "https://github.com/sunng87/stavka/blob/master/{filepath}#L{line}"
          :metadata {:doc/format :markdown}}
  :deploy-repositories {"releases" :clojars})
