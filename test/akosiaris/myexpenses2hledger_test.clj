(ns akosiaris.myexpenses2hledger-test
  (:require [clojure.test :refer [deftest is testing]]
            [akosiaris.myexpenses2hledger :as sut])) ; system under test

(deftest validate-args-test
  (testing "Proper option"
    (is (map? (sut/validate-args '("--input" "foo" "--output" "bar"))))))
