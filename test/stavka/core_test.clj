(ns stavka.core-test
  (:require [clojure.test :refer :all]
            [stavka.core :refer :all]))

(deftest test-apis
  (testing "get config from environment"
    (let [conf (using (env))]
      (is (some? (get-config conf :user)))
      (is (= :a (get-config conf :some.env.that.never.exists :a)))))
  (testing "get json config from classpath or file"
    (let [conf (using (json (classpath "/test.json")))]
      (is (= 1 (get-config conf :object.child)))
      (is (= ["c0"] (get-config conf :array))))
    (let [conf (using (json (file "./dev-resources/test.json")))]
      (is (= 1 (get-config conf :object.child)))
      (is (= ["c0"] (get-config conf :array)))))
  (testing "get properties"
    (let [conf (using (properties (classpath "/test.properties")))]
      (is (= "1" (get-config conf :some.config)))
      (is (= :none (get-config conf :some.env.that.never.exists :none))))))
