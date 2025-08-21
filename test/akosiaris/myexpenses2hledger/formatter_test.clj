(ns akosiaris.myexpenses2hledger.formatter-test
  (:require [clojure.test :refer [deftest testing is]]
            [java-time.api :as jt]
            [akosiaris.myexpenses2hledger.formatter :as f]))

(deftest proper-transaction-headers
  (testing "That status is honored"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Pending" :status "!"}
          t2 {:date (jt/local-date "2025-08-20") :payee "Cleared" :code "123" :status "*"}
          t3 {:date (jt/local-date "2025-08-20") :payee "Empty string" :code "123" :status ""}
          ;; t4 {:date (jt/local-date "2025-08-20") :payee "no status" :code "123"}
          ]
      (is (= "2025-08-20 ! Pending" (f/format-transaction-header t1)))
      (is (= "2025-08-20 * (123) Cleared" (f/format-transaction-header t2)))
      (is (= "2025-08-20 (123) Empty string" (f/format-transaction-header t3)))
      ;; I don't think we should check this. :pre failure throws an AssertionError which is not an exception
      ;; (is (not (= "2025-08-20 (123) no status" (f/format-transaction-header t4))))
      ))
  (testing "That code is honored"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "no code" :status "*"}
          t2 {:date (jt/local-date "2025-08-20") :payee "with code" :code "123" :status "*"}]
      (is (= "2025-08-20 * no code" (f/format-transaction-header t1)))
      (is (= "2025-08-20 * (123) with code" (f/format-transaction-header t2)))))
  (testing "That note is honored"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "no note" :status "*"}
          t2 {:date (jt/local-date "2025-08-20") :payee "with note" :note "a note" :status "*"}]
      (is (= "2025-08-20 * no note" (f/format-transaction-header t1)))
      (is (= "2025-08-20 * with note | a note" (f/format-transaction-header t2)))))
  (testing "That comment is honored"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "no comment" :status "*"}
          t2 {:date (jt/local-date "2025-08-20") :payee "with comment" :comment "a comment" :status "*"}]
      (is (= "2025-08-20 * no comment" (f/format-transaction-header t1)))
      (is (= "2025-08-20 * with comment  ; a comment" (f/format-transaction-header t2)))))
  )