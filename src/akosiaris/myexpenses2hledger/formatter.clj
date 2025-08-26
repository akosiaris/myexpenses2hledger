(ns akosiaris.myexpenses2hledger.formatter
  (:require [akosiaris.myexpenses2hledger.spec :as spec]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [blank? split]]
            [java-time.api :as jt]))

(def date-format :iso-local-date)

(defn amount-integer-part
  "Turns amount into a string, cuts out the dot and the decimals, keeping only the integer part. Useful for alignments"
  [amount]
  (-> amount
      str
      (split #"\.")
      first
      count))

(defn transaction-max-lengths
  "Return a map containing a transaction's max amount and account length across all postings"
  [transaction]
  (let [postings (:postings transaction)
        accounts (map :account postings)
        amounts (map :amount postings)
        ;; Use a 0 in a vector for the default return
        max-account-length (apply max (concat [0] (map count accounts)))
        ;; We wanna align on the last integer of the longest amount, so calculate length of that
        max-integer-amount-length (apply max (concat [0] (map amount-integer-part amounts)))]
    {:max-account-length max-account-length
     :max-integer-amount-length max-integer-amount-length}))

(defn- pad-account
  "Right pads account up to max-length"
  [max-length account]
  (let [pad-length (- max-length (count account))
        padding (apply str (repeat pad-length " "))]
    (format "%s%s" account padding)))

(defn- pad-amount
  "Left pads amount up to max-length"
  [max-length amount]
  (let [pad-length (- max-length (amount-integer-part amount))]
    (format " %s%.2f" (apply str (repeat pad-length " ")) amount)))


(defn format-posting
  "Formats a transaction's posting in the hledger syntax"
  ([posting]
   (format-posting 0 0 posting))
  ([max-account-length max-integer-amount-length posting]
   {:pre [(s/valid? ::spec/posting posting)]}
   (let [ffs [[:status, identity]
              [:account, #(pad-account max-account-length %)]
              [:amount, #(pad-amount max-integer-amount-length %)]
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
  (let [ctag (if (or (:comment transaction) (:tag transaction)) true false)
        t (assoc transaction :ctag ctag)
        ffs [[:date, #(jt/format date-format %)]
             [:status, identity]
             [:code, #(if % (format "(%s)" %) %)]
             [:payee, identity]
             [:note, #(if % (format "| %s" %) %)]
             [:ctag, #(if % " ;" "")]
             [:comment, identity] ; TODO: solve this for proper alignment
             [:tag, identity]] ; TODO: solve this for proper alignment
        r (mapv #((second %) (get t (first %))) ffs)]
    (reduce #(if (blank? %2) %1 (str %1 " " %2)) r)))

(defn format-transaction
  "Formats a transaction in the hledger format"
  ([transaction]
   (let [{max-account-length :max-account-length
          max-integer-amount-length :max-integer-amount-length} (transaction-max-lengths transaction)]
     (format-transaction transaction max-account-length max-integer-amount-length)))
  ([transaction max-account-length max-integer-amount-length]
   {:pre [(s/valid? ::spec/transaction transaction)]}
   (let [header (format-transaction-header transaction)
         postings (:postings transaction)
         fpostings (mapv #(format-posting max-account-length max-integer-amount-length %) postings)
         lines (concat [header] fpostings)]
     (clojure.string/join "\n" lines))))