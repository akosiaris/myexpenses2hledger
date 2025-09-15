(ns akosiaris.myexpenses2hledger.importer
  (:require [clojure.data.json :as json]
            [java-time.api :as jt]
            [clojure.string :refer [join replace] :rename {replace str-replace}]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as t]
            [akosiaris.myexpenses2hledger.spec :as spec]))

(def ^:private dedup-struct (atom #{}))

(defn- transform-json-keys
  "To be called by json/read and friends. Renames MyExpenses keys to hledger keys"
  [k]
  (cond
    (= k "uuid") :code
    (= k "currency") :commodity
    (= k "label") :default-account
    (= k "category") :account
    (= k "methodLabel") :note
    :else (keyword k)))

(defn- transform-json-values
  "This function containers all the magic for transform from MyExpenses to hledger entities"
  [k v]
  (cond
    (= k :date) (jt/local-date v)
    (= k :status) (cond
                    (= v "RECONCILED") "*"
                    (= v "UNRECONCILED") ""
                    (= v "CLEARED") "!")
    ;; Switch account to hledger syntax
    (= k :account) (join ":" v)
    ;; Switch tags to the hledger syntax
    (= k :tags) (mapv #(format "%s:" (str-replace % " " "-")) v)
    ;; Force Decimal for accuracy, flip the sign since MyExpenses has a negative sign for all expenses
    (number? v) (* -1 (bigdec v))
    ;; Otherwise return value as is
    :else v))

(defn- produce-transaction
  "Takes a transaction in the MyExpenses JSON export and returns an Hledger one. Supports splits and transfers"
  [transaction balance-account commodity]
  ;; 3 different types of transactions are supported here
  (cond
    ;; First off, let's avoid duplicates, by checking MyExpenses UUIDs and only including them once
    (contains? @dedup-struct (:code transaction))
    (do (t/info ::duplicate-transaction
               :code (:code transaction))
        :clojure.spec.alpha/invalid)

    ;; Let's handles splits. We calculate all the splits, add the balancing transcation and flatten
    (some? (:splits transaction))
    (let [ps (map #(assoc (select-keys % [:amount :account :cost]) :commodity commodity) (:splits transaction))
          bp {:amount (* -1 (:amount transaction))
              :account balance-account
              :commodity commodity}
          postings (flatten [ps bp])
          t (select-keys transaction [:date :payee :status :code :comment :tags :note])
          ft (assoc t :postings postings)
          conform (s/conform ::spec/transaction ft)]
      (swap! dedup-struct conj (:code transaction))
      (when (s/invalid? conform)
        (t/warn ::non-conforming-split-transaction
               :problem (first (:clojure.spec.alpha/problems (s/explain-data ::spec/transaction ft)))))
      conform)
    ;; Then transfers. Those have the payee set to the empty string and we know the accounts of both postings
    (some? (:transferAccount transaction))
    (let [p1 (assoc (select-keys transaction [:amount :cost]) :account (:transferAccount transaction) :commodity commodity)
          p2 (assoc p1 :amount (* -1 (:amount transaction))
                    :account balance-account)
          t (assoc (select-keys transaction [:date :payee :status :code :comment :tags :note]) :payee "")
          ft (assoc t :postings [p1 p2])
          conform (s/conform ::spec/transaction ft)]
      (swap! dedup-struct conj (:code transaction))
      (when (s/invalid? conform)
        (t/warn ::non-conforming-transfer-transaction
               :problem (first (:clojure.spec.alpha/problems (s/explain-data ::spec/transaction ft)))))
      conform)
    ;; Finally normal ones
    :else
    (let [p1 (assoc (select-keys transaction [:amount :account :cost]) :commodity commodity)
          p2 (assoc p1 :amount (* -1 (:amount transaction))
                    :account balance-account)
          t (select-keys transaction [:date :payee :status :code :comment :tags :note])
          ft (assoc t :postings [p1 p2])
          conform (s/conform ::spec/transaction ft)]
      (swap! dedup-struct conj (:code transaction))
      (when (s/invalid? conform)
        (t/warn ::non-conforming-standard-transaction
               :problem (first (:clojure.spec.alpha/problems (s/explain-data ::spec/transaction ft)))))
      conform)))

(defn- create-opening-balance-transaction
  "Creates the Opening Balance transaction for the account, but only if there an opening balance"
  [data equity-account]
  (if (> 0 (:openingBalance data))
    (let [date (apply jt/min (map :date (:transactions data)))
          openingposting1 {:amount (:openingBalance data)
                           :account equity-account
                           :commodity (:commodity data)}
          openingposting2 {:amount (* -1 (:openingBalance data))
                           :account (:default-account data)
                           :commodity (:commodity data)}
          openingtransaction {:code (:code data)
                              :date date
                              :status "*" ;; TODO: Should it really be CLEARED?
                              :payee "Opening Balance"
                              :postings [openingposting1 openingposting2]}
          conform (s/conform ::spec/transaction openingtransaction)]
      (if (s/invalid? conform)
        (do
          (t/warn ::non-conforming-opening-balance-transaction
                 :data openingtransaction
                 :problem (first (:clojure.spec.alpha/problems (s/explain-data ::spec/transaction openingtransaction))))
          conform)
        (do
          (t/info ::opening-balance-transaction
                 :transaction openingtransaction)
          conform)))
        :clojure.spec.alpha/invalid))

(defn- load-account
  "Load 1 single MyExpenses exported account"
  [data equity-account]
  (let [commodity (:commodity data)
        obt (create-opening-balance-transaction data equity-account)
        transactions (cons obt (map #(produce-transaction % (:default-account data) commodity) (:transactions data)))]
    (filter #(not (s/invalid? %)) transactions)))

(defn load-my-expenses-json
  "Sets up the JSON loader, feeds it input and returns the result"
  [input equity-account]
  ;; TODO: Figure out whether this is the proper place for re-initilization of this atom
  (reset! dedup-struct #{})
  (let [data (json/read-str input
                            :key-fn transform-json-keys
                            :value-fn transform-json-values)
        merged (vector? data)]
    (if merged
      (flatten (map #(load-account % equity-account) data))
      (load-account data equity-account))))