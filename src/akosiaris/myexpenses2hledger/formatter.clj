(ns akosiaris.myexpenses2hledger.formatter
  (:require [akosiaris.myexpenses2hledger.spec :as spec]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [blank?]]
            [java-time.api :as jt]))

(def date-format :iso-local-date)

(defn- pad-account
  "Calculates the amount of required padding"
  [mlength account]
  (let [padding-length (- mlength (count account))
        padding (apply str (repeat padding-length " "))]
    (format "%s%s" account padding)))

(defn- pad-amount
  "Pads amount with the proper spaces depending on the sign"
  [amlength amount]
  (let [leftpad (- amlength (count (str amount)))]
    (format " %s%s" (apply str (repeat leftpad " ")) amount)))

(defn format-posting
  "Formats a transaction's posting in the hledger syntax"
  ([posting]
   (format-posting 0 1 posting))
  ([acmlength amlength posting]
   {:pre [(s/valid? ::spec/posting posting)]}
   (let [ffs [[:status, identity]
              [:account, #(pad-account acmlength %)]
              [:amount, #(pad-amount amlength %)]
              [:commodity, #(if (re-matches #" " %) (format "\"%s\"" %) (identity %))] ; TODO: solve this for proper alignment
             ; TODO: This needs more work to reflect both unit price and total price as well as cost commodity
              [:cost, #(if % (format "@ %s" %) (identity %))] ; TODO: solve this for proper alignment
              [:comment, #(if % (format " ; %s" %) (identity %))] ; TODO: solve this for proper alignment
              [:tag, identity]] ; TODO: solve this for proper alignment
         r (mapv #((second %) (get posting (first %))) ffs)]
     (reduce #(if (blank? %2) %1 (str %1 " " %2)) (apply str (repeat 3 " ")) r))))

(defn format-transaction-header
  "Formats the transaction header, that is date, payee, status, code, comment, tag"
  [transaction]
  {:pre [(s/valid? ::spec/transaction transaction)]}
  (let [ffs [[:date, #(jt/format date-format %)]
             [:status, identity]
             [:code, #(if % (format "(%s)" %) %)]
             [:payee, identity]
             [:note, #(if % (format "| %s" %) %)]
             [:comment, #(if % (format " ; %s" %) %)] ; TODO: solve this for proper alignment
             [:tag, identity]] ; TODO: solve this for proper alignment
        r (mapv #((second %) (get transaction (first %))) ffs)]
    (reduce #(if (blank? %2) %1 (str %1 " " %2)) r)))

(defn format-transaction
  "Formats a transaction in the hledger format"
  [transaction]
  {:pre [(s/valid? ::spec/transaction transaction)]}
  (let [header (format-transaction-header transaction)
        postings (:postings transaction)
        account_lengths (map #(count (:account %)) postings)
        ;; We deploy a small trick here. We want to align on the last non decimal digit.
        ;; So we make it a string and only keep the non-decimal part
        amount_lengths (map #(count (-> (:amount %)
                                        str
                                        (clojure.string/split #"\.")
                                        first))
                                    postings)
        acmlength (if (empty? account_lengths) 0 (apply max account_lengths))
        ammlength (if (empty? amount_lengths) 0 (apply max amount_lengths))
        fpostings (mapv #(format-posting acmlength ammlength %) postings)
        lines (if (empty? fpostings) [header] (cons header fpostings))]
    (clojure.string/join "\n" lines)))