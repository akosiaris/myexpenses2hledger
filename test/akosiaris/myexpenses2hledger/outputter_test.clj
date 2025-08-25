(ns akosiaris.myexpenses2hledger.outputter-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest testing is]]
   [akosiaris.myexpenses2hledger.outputter :refer [write-hledger-journal]]
   [akosiaris.myexpenses2hledger.importer :refer [load-my-expenses-json]]))

(deftest write-hledger-journal-test-single-account
  (testing "That a single account export is properly transformed"
    (let [output "/tmp/write-hledger-journal-test-single-account.ledger"
          jsonfile "tests/MyExpensesJSONs/single_account_export.json"
          input "tests/MyExpensesJSONs/single_account_export.ledger"
          content (-> input
                      io/resource
                      slurp)
          data (-> jsonfile io/resource slurp load-my-expenses-json)
          temp-file (io/file output)]
      (write-hledger-journal data temp-file)
      (is (= (-> temp-file slurp) content))
      )))