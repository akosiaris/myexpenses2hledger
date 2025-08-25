(ns akosiaris.myexpenses2hledger.spec-test
  (:require [clojure.test :refer [deftest testing is]]
            [java-time.api :as jt]
            [clojure.spec.alpha :as s]
            [akosiaris.myexpenses2hledger.spec :as espec]))

;; Testing transactions without postings
(deftest test-transactions-with-no-postings
  (testing "Wrong transaction - no payee"
    (let [t1 {:date (jt/local-date "2025-08-20") :status ""}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Wrong transaction - date as a string"
    (let [t1 {:date "2025-08-20" :status ""}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Empty transaction with a payee"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :status ""}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a bad status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Bad bot" :status "?"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Transaction with a cleared status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cleared Check" :status "*"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a pending status"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Pending pear" :status "!"}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a note"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Bar Foo" :note "foobar" :status ""}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a bad tag"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Awful tag" :tag "bad tag:" :status ""}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Transaction with a valid tag"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Nice ztag" :tag "good-tag:" :status ""}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Transaction with a code"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Cool code" :code "someuuid" :status ""}]
      (is (s/valid? ::espec/transaction t1)))))

;; Test transactions with postings
(deftest test-transactions-with-simple-postings
  (testing "Bad transaction - postings not an collection"
    (let [t1 {:date (jt/local-date "2025-08-20") :payee "Bad poster" :status "" :postings "123"}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Bad transaction - malformed account"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Bad account" :status "" :postings [p1]}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Bad transaction - 1 posting"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Bad poster2" :status "" :postings [p1]}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Balanced transaction - 2 postings"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          p2 {:amount -1M :commodity "EUR" :account "Assets:Cash"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Cool caffé" :status "" :postings [p1 p2]}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Balanced transaction - 3 postings"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          p2 {:amount 1M :commodity "EUR" :account "Expenses:Food:Takeaway"}
          p3 {:amount -2M :commodity "EUR" :account "Assets:Cash"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Cool Coffeeshop" :status "" :postings [p1 p2 p3]}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Unbalanced transaction - 2 postings"
    (let [p1 {:amount 1M :commodity "EUR" :account "Expenses:Coffee"}
          p2 {:amount -2M :commodity "EUR" :account "Assets:Cash"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Unbalanced coffee" :status "" :postings [p1 p2]}]
      (is (not (s/valid? ::espec/transaction t1)))))
  (testing "Accounts,payees with slashes"
    (let [p1 {:amount 10.1M :commodity "EUR" :account "Expenses:Coffee/Croissants"}
          p2 {:amount -10.1M :commodity "EUR" :account "Assets:PocketMoney"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Payee with sla/sh" :status "" :postings [p1 p2]}]
      (is (s/valid? ::espec/transaction t1))))
  (testing "Accounts,payees with dots"
    (let [p1 {:amount 33.1M :commodity "EUR" :account "Expenses:B.A.R.F"}
          p2 {:amount -33.1M :commodity "EUR" :account "Assets:PocketMoney"}
          t1 {:date (jt/local-date "2025-08-20") :payee "Payee with dot.dot.dot" :status "" :postings [p1 p2]}]
      (is (s/valid? ::espec/transaction t1))))
  )