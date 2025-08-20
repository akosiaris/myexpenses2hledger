(ns akosiaris.myexpenses2hledger.spec-test
  (:require [clojure.test :refer [deftest testing is]]
            [java-time.api :as jt]
            [clojure.spec.alpha :as s]
            [akosiaris.myexpenses2hledger.spec :as espec]))

;; Testing transactions without postings
(deftest test-transactions-with-no-postings
  (testing "Wrong transaction - no payee"
    (let [t1 {:date (jt/local-date "2025-08-20")}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Wrong transaction - date as a string"
    (let [t1 {:date "2025-08-20"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Empty transaction with a payee"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a bad status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :status "?"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Transaction with a cleared status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :status "*"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a pending status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :status "!"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a note"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :note "foobar"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a bad tag"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :tag "bad tag:"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Transaction with a valid tag"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :tag "good-tag:"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a code"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :code "someuuid"}]
      (is (s/valid? ::espec/transaction t1)))))

;; Test transactions with postings
(deftest test-transactions-with-simple-postings
  (testing "Bad transaction - postings not an collection"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :postings "123"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Bad transaction - malformed account"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :postings [p1]}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Good transaction - 1 posting"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :postings [p1]}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Good transaction - 2 postings"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          p2 {:amount 2M :commodity "EUR" :account "Expenses:Food:Takeaway"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :postings [p1 p2]}]
      (is (s/valid? ::espec/transaction t1)))))