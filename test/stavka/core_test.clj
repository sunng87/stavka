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
      (is (some? ($ conf :lein.java.cmd)))
      (is (= :a ($ conf :some.env.that.never.exists :a)))))
  (testing "environment with prefix"
    (let [conf (using (env :prefix "US"))]
      (is (some? ($ conf :er)))))
  (testing "cli options"
    (let [conf (using (cli ["-p" "123" "--config" "test.conf"]
                           [["-p" "--port PORT" "Port Number" :id :port :parse-fn #(Integer/parseInt %)]
                            [nil "--config CONFIG" "Config Name" :id :config]]))]
      (is (= 123 ($ conf :port)))))
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
    (let [port 30000
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
  (testing "get edn"
    (let [conf (using (edn (classpath "/test.edn")))]
      (is (= 1 ($ conf :object.child)))
      (is (= ["c0"] ($ conf :array)))))
  (testing "get from jvm options"
    (let [conf (using (options))]
      (is (= "yes" ($ conf :stavka.test.attr)))))
  (testing "file watch updater"
    (let [conf (using (json (watch (file "./dev-resources/test.json"))))]
      (is (= 1 ($ conf :object.child)))))
  (testing "file watch2 updater"
    (let [conf (using (json (watch2 (file "./dev-resources/test.json"))))]
      (is (= 1 ($ conf :object.child))))))

(deftest test-global
  (testing "no global config available"
    (is (thrown? clojure.lang.ExceptionInfo ($$ :user))))
  (global! (env)
           (properties (classpath "/test.properties")))
  (is (some? ($$ :user)))
  (is (= "1" ($$ :some.config)))
  (is (= 1 ($$l :some.config))))

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
          (io/delete-file path)))))
  (testing "same test with watch2"
    (let [path "./target/watch2-test.json"]
      (try
        (spit path (che/generate-string {:test 1}))
        (let [conf (using (json (watch2 (file path))))]
          (is (= 1 ($ conf :test)))
          (spit path (che/generate-string {:test 2}))
          (Thread/sleep 100)
          (is (= 2 ($ conf :test)))

          (stop-updaters! conf))
        (finally
          (io/delete-file path))))))

(deftest test-poller-updater
  (testing "poll a file for changes"
    (let [path "./target/poll-test.json"]
      (try
        (spit path (che/generate-string {:test 1}))
        (let [conf (using (json (poll (file path) 200)))]
          (is (= 1 ($ conf :test)))
          (spit path (che/generate-string {:test 2}))
          (Thread/sleep 500)
          (is (= 2 ($ conf :test)))

          (stop-updaters! conf))
        (finally
          (io/delete-file path)))))
  (testing "poll a url for change"
    (let [port 30001
          temp-url (format "http://localhost:%d/" port)
          counter (atom 0)
          server-fn (fn [req]
                      (case (swap! counter inc)
                        1 {:body (che/generate-string {:test {:config 1}})}
                        2 {:status 500 :body "Expected error"}
                        {:body (che/generate-string {:test {:config 2}})}))
          server (rj9a/run-jetty server-fn
                                 {:port port :join? false})]
      (try
        (let [conf (using (json (poll (url temp-url) 50)))]
          (is (= 1 ($ conf :test.config)))
          (Thread/sleep 200)
          (is (= 2 ($ conf :test.config))))
        (finally
          (rj9a/stop-server server)))))
  (testing "listeners"
    (let [port 30002
          temp-url (format "http://localhost:%d/" port)
          counter (atom 0)
          server-fn (fn [req]

                      (case (swap! counter inc)
                        1 {:body (che/generate-string {:test {:config 1}})}
                        2 {:status 500 :body "Expected error"}
                        {:body (che/generate-string {:test {:config 2}})}))
          server (rj9a/run-jetty server-fn
                                 {:port port :join? false})]
      (try
        (let [conf (using (json (poll (url temp-url) 50)))
              listener-called (atom false)]
          (on-change! conf :test.config (fn [current-value previous-value]
                                          (reset! listener-called true)
                                          (is (= current-value 2))
                                          (is (= previous-value 1))))
          (is (= 1 ($ conf :test.config)))
          (Thread/sleep 200)
          (is (= 2 ($ conf :test.config)))
          (is @listener-called))
        (finally
          (rj9a/stop-server server))))))

(deftest test-convertors
  (let [conf (using (properties (classpath "/test.properties")))]
    (is (= "1" ($ conf :some.config)))
    (is (= 1 ($l conf :some.config)))
    (is (= 1.0 ($f conf :some.config)))
    (is (= "1" ($s conf :some.config)))
    (is (= false ($b conf :some.config)))))

(deftest test-env-no-transform
  (let [conf (using (env :disable-underscore-to-dot? true))]
    (is (some? ($ conf :lein_java_cmd)))))

(deftest test-loader-option
  (testing "the quiet? option that ignore errors in loading from source"
    (try
      (using (json (url "http://localhost:1111")))
      (is false)
      (catch Exception e
        (is true)))
    (is (some? (using (json (url "http://localhost:1111" :quiet? true)))))

    (try
      (using (json (file "not-exists.json")))
      (is false)
      (catch Exception e
        (is true)))
    (is (some? (using (json (file "not-exists.json" :quiet? true)))))

    (is (some? (using (json (classpath "/not-exists.json" :quiet? true)))))))
