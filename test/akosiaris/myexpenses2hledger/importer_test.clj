(ns akosiaris.myexpenses2hledger.importer-test
  (:require [clojure.java.io :refer [resource]]
            [clojure.test :refer [testing deftest is]]
            [akosiaris.myexpenses2hledger.importer :refer [load-my-expenses-json]]))

(deftest load-my-expenses-json-test
  (testing "Importing a basic export works"
    (let [data (-> "tests/exporting.json"
                   resource
                   slurp
                   load-my-expenses-json)]
      (is (some? (seq data))))))