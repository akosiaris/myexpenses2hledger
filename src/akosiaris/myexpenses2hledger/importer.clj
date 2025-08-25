(ns akosiaris.myexpenses2hledger.importer
  (:require [clojure.data.json :as json]
            [java-time.api :as jt]
            [clojure.string :refer [join replace] :rename {replace str-replace}]
            [clojure.spec.alpha :as s]
            [com.brunobonacci.mulog :as m]
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
    ;; Delete methodLabel
    (= k :methodLabel) transform-json-values
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
    (do (m/log ::duplicate-transaction :level :WARN :code (:code transaction))
        :clojure.spec.alpha/invalid)

    ;; Let's handles splits. We calculate all the splits, add the balancing transcation and flatten
    (some? (:splits transaction))
    (let [ps (map #(assoc (select-keys % [:amount :account :cost]) :commodity commodity) (:splits transaction))
          bp {:amount (* -1 (:amount transaction))
              :account balance-account
              :commodity commodity}
          postings (flatten [ps bp])
          t (select-keys transaction [:date :payee :status :code :comment :tag :note])]
      (swap! dedup-struct conj (:code transaction))
      (s/conform ::spec/transaction (assoc t :postings postings)))
    ;; Then transfers. Those have the payee set to the empty string and we know the accounts of both postings
    (some? (:transferAccount transaction))
    (let [p1 (assoc (select-keys transaction [:amount :cost]) :account (:transferAccount transaction) :commodity commodity)
          p2 (assoc p1 :amount (* -1 (:amount transaction))
                    :account balance-account)
          t (assoc (select-keys transaction [:date :payee :status :code :comment :tag :note]) :payee "")]
      (swap! dedup-struct conj (:code transaction))
      (s/conform ::spec/transaction (assoc t :postings [p1 p2])))
    ;; Finally normal ones
    :else
    (let [p1 (assoc (select-keys transaction [:amount :account :cost]) :commodity commodity)
          p2 (assoc p1 :amount (* -1 (:amount transaction))
                    :account balance-account)
          t (select-keys transaction [:date :payee :status :code :comment :tag :note])]
      (swap! dedup-struct conj (:code transaction))
      (s/conform ::spec/transaction (assoc t :postings [p1 p2])))))

(defn- load-account
  "Load 1 single MyExpenses exported account"
  [data]
  (let [common (dissoc data :transactions)
        commodity (:commodity common)
        openingp {:amount (:openingBalance common)
                  :account (:default-account common)
                  :commodity commodity}
        openingt {:code (:code common)
                  :postings [openingp]}
        transactions (map #(produce-transaction % (:default-account common) commodity) (:transactions data))]
    (filter #(not (s/invalid? %)) transactions)))

(defn load-my-expenses-json
  "Sets up the JSON loader, feeds it input and returns the result"
  [input]
  ;; TODO: Figure out whether this is the proper place for re-initilization of this atom
  (reset! dedup-struct #{})
  (let [data (json/read-str input
                            :key-fn transform-json-keys
                            :value-fn transform-json-values)
        multi (vector? data)]
    (if multi
      (flatten (map load-account data))
      (load-account data))))