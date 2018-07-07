(defproject stavka "0.4.0-SNAPSHOT"
  :description "Stavka manages configuration, for your clojure application"
  :url "https://github.com/sunng87/stavka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.0"]
                 [circleci/clj-yaml "0.5.6"]
                 [clj-http "3.9.0"]
                 [hawk "0.2.11"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.11.1" :scope "test"]]
                   :jvm-opts ["-Dstavka.test.attr=yes"]}
             :example {:dependencies [[org.clojure/java.jdbc "0.7.7"]
                                      [org.hsqldb/hsqldb "2.4.1"]]
                       :source-paths ["examples"]}}
  :plugins [[lein-codox "0.10.4"]]
  :codox {:output-path "target/codox"
          :source-uri "https://github.com/sunng87/stavka/blob/master/{filepath}#L{line}"
          :metadata {:doc/format :markdown}}
  :deploy-repositories {"releases" :clojars})
