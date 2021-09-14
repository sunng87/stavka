(ns stavka.minimal-test
  (:require [clojure.test :refer :all]
            [stavka.core :refer :all]))

(deftest ^:minimal optional-features-disable
  (testing "json is disabled"
    (is (thrown? UnsupportedOperationException (using (json (classpath "/test.json"))))))

  (testing "yaml is disabled"
    (is (thrown? UnsupportedOperationException (using (yaml (classpath "/test.yaml"))))))

  (testing "url is disabled"
    (is (thrown? UnsupportedOperationException (using (edn (url "http://localhost/"))))))

  (testing "watch is disabled"
    (is (thrown? UnsupportedOperationException (using (edn (watch (file "test.edn"))))))))
