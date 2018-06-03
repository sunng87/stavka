(defproject stavka "0.1.1-SNAPSHOT"
  :description "Stavka manages configuration, for your clojure application"
  :url "https://github.com/sunng87/stavka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [environ "1.1.0"]
                 [cheshire "5.8.0"]
                 [circleci/clj-yaml "0.5.6"]
                 [clj-http "3.9.0"]
                 [hawk "0.2.11"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.11.1" :scope "test"]]}}
  :plugins [[lein-codox "0.9.5"]]
  :codox {:output-path "target/codox"
          :source-uri "https://github.com/sunng87/stavka/blob/master/{filepath}#L{line}"
          :metadata {:doc/format :markdown}}
  :deploy-repositories {"releases" :clojars})
