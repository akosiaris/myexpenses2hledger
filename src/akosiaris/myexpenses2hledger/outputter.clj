(ns akosiaris.myexpenses2hledger.outputter
  (:require [clojure.string]
            [akosiaris.myexpenses2hledger.formatter :refer [format-transaction
                                                            transaction-max-lengths]]))

(defn write-hledger-journal
  "Will output an hledger compatible journal from a list of transactions"
  [transactions outfile]
  ;; We want the max of maxes for both
  (let [data (map transaction-max-lengths transactions)
        max-account-length (apply max (map :max-account-length data))
        max-integer-amount-length (apply max (map :max-integer-amount-length data))]
    (spit outfile (clojure.string/join "\n" (map #(format "%s\n" (format-transaction % max-account-length max-integer-amount-length)) transactions)))))