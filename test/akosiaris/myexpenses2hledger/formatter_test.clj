(ns akosiaris.myexpenses2hledger.formatter-test
  (:require [clojure.test :refer [deftest testing is]]
            [java-time.api :as jt]
            [clojure.java.io :refer [resource]]
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
      (is (= "2025-08-20 * with comment  ; a comment" (f/format-transaction-header t2))))))

(deftest proper-postings
  (testing "Bare minimum postings"
    (let [p1 {:account "expenses" :amount 1M :commodity "EUR"}
          p2 {:account "assets" :amount -1M :commodity "$"}]
      (is (= "    expenses  1 EUR" (f/format-posting p1)))
      (is (= "    assets  -1 $" (f/format-posting p2)))))
  (testing "With a status"
    (let [p1 {:account "expenses" :amount 1M :commodity "EUR" :status "*"}]
      (is (= "    * expenses  1 EUR" (f/format-posting p1)))))
  (testing "With a cost"
    (let [p1 {:account "expenses" :amount 1M :commodity "EUR" :cost 1.5M}]
      (is (= "    expenses  1 EUR @ 1.5" (f/format-posting p1)))))
  (testing "With a comment"
    (let [p1 {:account "expenses" :amount 1M :commodity "EUR" :comment "A comment"}]
      (is (= "    expenses  1 EUR  ; A comment" (f/format-posting p1)))))
  (testing "With Unicode in the name"
    (let [p1 {:account "Έξοδα-你" :amount 1M :commodity "EUR" :comment "A comment"}]
      (is (= "    Έξοδα-你  1 EUR  ; A comment" (f/format-posting p1))))))


(deftest proper-transactions
  (testing "Bare minimum transactions"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Pending" :status "!"}
          t2 {:date (jt/local-date "2025-08-20") :payee "Cleared" :code "123" :status "*"}
          t3 {:date (jt/local-date "2025-08-20") :payee "Empty string" :code "123" :status ""}
          ;; t4 {:date (jt/local-date "2025-08-20") :payee "no status" :code "123"}
          ]
      (is (= "2025-08-20 ! Pending" (f/format-transaction t1)))
      (is (= "2025-08-20 * (123) Cleared" (f/format-transaction t2)))
      (is (= "2025-08-20 (123) Empty string" (f/format-transaction t3)))))
  (testing "Transactions with postings"
    (let [p1 {:account "expenses" :amount 1M :commodity "EUR"}
          p2 {:account "assets" :amount -1M :commodity "$"}
          t1 {:date (jt/local-date "2025-08-20")
              :payee "Pending"
              :status "!"
              :postings [p1, p2]}]
      (is (= "2025-08-20 ! Pending\n    expenses   1 EUR\n    assets    -1 $" (f/format-transaction t1))))))

(deftest against-fixture-transactions
  (testing "single-transaction"
    (let [p1 {:account "assets:bank:checking" :amount 1M :commodity "EUR"}
          p2 {:account "income:salary" :amount -1M :commodity "EUR"}
          t1 {:date (jt/local-date "2008-01-01")
              :payee "income"
              :status ""
              :postings [p1, p2]}
          fixture (-> "tests/single_transation.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "multiple-postings"
    (let [p1 {:account "expenses:food" :amount 90M :commodity "USD"}
          p2 {:account "expenses:supplies" :amount 11M :commodity "USD"}
          p3 {:account "assets:cash" :amount -101M :commodity "USD"}
          t1 {:date (jt/local-date "2008-06-03")
              :payee "eat & shop"
              :status "*"
              :postings [p1, p2, p3]}
          fixture (-> "tests/multiple_postings.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "pending-transaction"
    (let [p1 {:account "expenses:supplies" :amount 1M :commodity "$"}
          p2 {:account "assets:cash" :amount -1M :commodity "$"}
          t1 {:date (jt/local-date "2008-06-03")
              :payee "foo & bar"
              :status "!"
              :postings [p1, p2]}
          fixture (-> "tests/pending_transaction.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "cleared-transaction"
    (let [p1 {:account "expenses:food" :amount 1M :commodity "$"}
          p2 {:account "assets:cash" :amount -1M :commodity "$"}
          t1 {:date (jt/local-date "2008-06-03")
              :payee "eat & shop"
              :status "*"
              :postings [p1, p2]}
          fixture (-> "tests/cleared_transaction.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "a-note"
    (let [t1 {:date (jt/local-date "2024-02-01")
              :payee "note some event"
              :status ""}
          fixture (-> "tests/a_note.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "with-code"
    (let [p1 {:account "assets:checking" :amount -500M :commodity "$"}
          p2 {:account "expenses:rent" :amount 500M :commodity "$"}
          t1 {:date (jt/local-date "2024-01-03")
              :payee "pay rent"
              :status "!"
              :code "12345"
              :postings [p1, p2]}
          fixture (-> "tests/transaction_with_code.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "with-comment"
    (let [p1 {:account "assets:checking" :amount -500M :commodity "$"}
          p2 {:account "expenses:equipment" :amount 500M :commodity "$"}
          t1 {:date (jt/local-date "2024-01-03")
              :payee "e-corp"
              :status ""
              :comment "Domo arigato Mr. Roboto"
              :postings [p1, p2]}
          fixture (-> "tests/transaction_with_comment.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "with-tag"
    (let [p1 {:account "assets:checking" :amount -50M :commodity "€"}
          p2 {:account "expenses:food" :amount 50M :commodity "€"}
          t1 {:date (jt/local-date "2024-01-03")
              :payee "Artie's Deli"
              :status ""
              :tag "worktrips:"
              :postings [p1, p2]}
          fixture (-> "tests/transaction_with_tag.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "with-a-note"
    (let [p1 {:account "assets:bank:gold" :amount -10M :commodity "gold"}
          p2 {:account "assets:pouch" :amount 10M :commodity "gold"}
          t1 {:date (jt/local-date "2024-01-02")
              :payee "Gringott's Bank"
              :status ""
              :note "withdrawal"
              :postings [p1, p2]}
          fixture (-> "tests/transaction_with_a_note.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1)))))
  (testing "with-costs"
    (let [p1 {:account "assets:investments:2024-01-15" :amount 2.0M :commodity "AAAA"}
          p2 {:account "assets:investments:2024-01-15-02" :amount 3.0M :commodity "AAAA"}
          p3 {:account "assets:checking" :amount -7M :commodity "USD"}
          t1 {:date (jt/local-date "2024-01-15")
              :payee "buy some shares, in two lots"
              :status ""
              :comment "Cost can be noted."
              :postings [p1, p2, p3]}
          fixture (-> "tests/with_costs.hledger"
                      resource
                      slurp)]
      (is (= fixture (f/format-transaction t1))))))