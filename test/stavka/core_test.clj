(ns stavka.core-test
  (:require [clojure.test :refer :all]
            [stavka.core :refer :all]
            [ring.adapter.jetty9 :as rj9a]
            [cheshire.core :as che]
            [clojure.java.io :as io]))

(deftest test-apis
  (testing "get config from environment"
    (let [conf (using (env))]
      (is (some? ($ conf :user)))
      (is (= :a ($ conf :some.env.that.never.exists :a)))))
  (testing "get json config from classpath or file"
    (let [conf (using (json (classpath "/test.json")))]
      (is (= 1 ($ conf :object.child)))
      (is (= ["c0"] ($ conf :array))))
    (let [conf (using (json (file "./dev-resources/test.json")))]
      (is (= 1 ($ conf :object.child)))
      (is (= ["c0"] ($ conf :array)))))
  (testing "get properties"
    (let [conf (using (properties (classpath "/test.properties")))]
      (is (= "1" ($ conf :some.config)))
      (is (= :none ($ conf :some.env.that.never.exists :none)))))
  (testing "test json config from url"
    (let [port 30001
          temp-url (format "http://localhost:%d/" port)
          remote-config (che/generate-string {:server "jetty9"})
          server (rj9a/run-jetty (constantly {:body remote-config})
                                 {:port port :join? false})]
      (try
        (let [conf (using (json (classpath "/test.json"))
                          (json (url temp-url)))]
          (is (= 1 ($ conf :object.child)))
          (is (= "jetty9" ($ conf :server))))
        (finally
          (rj9a/stop-server server)))))
  (testing "get yaml"
    (let [conf (using (yaml (classpath "/test.yaml")))]
      (is (= "127.0.0.1" ($ conf :spring.datasource.host)))
      (is (= 3306 ($ conf :spring.datasource.port)))))
  (testing "get from jvm options"
    (let [conf (using (options))]
      (is (= "yes" ($ conf :stavka.test.attr)))))
  (testing "file watch updater"
    (let [conf (using (json (watch (file "./dev-resources/test.json"))))]
      (is (= 1 ($ conf :object.child))))))

(deftest test-global
  (global! (env)
           (properties (classpath "/test.properties")))
  (is (some? ($ :user)))
  (is (= "1" ($ :some.config))))

(deftest test-file-watch-updater
  (testing "create a file and watch for change"
    (let [path "./target/watch-test.json"]
      (try
        (spit path (che/generate-string {:test 1}))
        (let [conf (using (json (watch (file path))))]
          (is (= 1 ($ conf :test)))
          (spit path (che/generate-string {:test 2}))
          (Thread/sleep 100)
          (is (= 2 ($ conf :test)))

          (stop-updaters! conf))
        (finally
          (io/delete-file path))))))
