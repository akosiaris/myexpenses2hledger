(ns akosiaris.myexpenses2hledger.formatter
  (:require [akosiaris.myexpenses2hledger.spec :as spec]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [blank?]]
            [java-time.api :as jt]))

(def date-format :iso-local-date)

(defn format-posting
  "Formats a transaction's posting in the hledger syntax"
  [posting]
  {:pre [(s/valid? ::spec/posting posting)]}
  (let [ffs [[:status, identity]
             [:account, identity]
             [:amount, #(format " %s" %)] ; TODO: solve this for proper alignment
             [:commodity, #(if (re-matches #" " %) (format "\"%s\"" %) (identity %))] ; TODO: solve this for proper alignment
             ; TODO: This needs more work to reflect both unit price and total price as well as cost commodity
             [:cost, #(if % (format "@ %s" %) (identity %))] ; TODO: solve this for proper alignment
             [:comment, #(if % (format " ; %s" %) (identity %))] ; TODO: solve this for proper alignment
             [:tag, identity]] ; TODO: solve this for proper alignment
        r (mapv #((second %) (get posting (first %))) ffs)]
    (reduce #(if (blank? %2) %1 (str %1 " " %2)) (apply str (repeat 3 " ")) r)))

(defn format-transaction-header
  "Formats the transaction header, that is date, payee, status, code, comment, tag"
  [transaction]
  {:pre [(s/valid? ::spec/transaction transaction)]}
  (let [ffs [[:date, #(jt/format date-format %)]
             [:status, identity]
             [:code, #(if % (format "(%s)" %) (identity %))]
             [:payee, identity]
             [:note, #(if % (format "| %s" %) (identity %))]
             [:comment, #(if % (format " ; %s" %) (identity %))] ; TODO: solve this for proper alignment
             [:tag, identity]] ; TODO: solve this for proper alignment
        r (mapv #((second %) (get transaction (first %))) ffs)]
    (reduce #(if (blank? %2) %1 (str %1 " " %2)) r)))

(defn format-transaction
  "Formats a transaction in the hledger format"
  [transaction]
  {:pre [(s/valid? ::spec/transaction transaction)]}
  (let [header (format-transaction-header transaction)
        postings (mapv format-posting (:postings transaction))
        lines (if (empty? postings) [header] (cons header postings))]
    (clojure.string/join "\n" lines)))