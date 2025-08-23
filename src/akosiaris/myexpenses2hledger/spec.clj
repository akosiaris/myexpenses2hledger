(ns akosiaris.myexpenses2hledger.spec
  (:require [clojure.spec.alpha :as s]
             [java-time.api :as jt]))

;; Some useful defs
;; An account name can support hierarchies via the : sign.
(def account-re #"^([\p{L}-]+ ?)+(:?[\p{L}-]+ ?)*$")
(defn account?
  "Check if an account name matches the proper syntax"
  [name]
  (re-matches account-re name))
;; A commodity can be really anything, but let's stick with currency symbols and words
(def commodity-re #"^\w+|\p{Sc}$")
(defn commodity?
  "Check if a commodity name matches the proper syntax"
  [name]
  (re-matches commodity-re name))
;; A tag should have no whitespace and end with a :
(def tag-re #"^[\w-]+:$")
(defn tag?
  "Check if a tag name matches the proper syntax"
  [name]
  (re-matches tag-re name))
(defn balanced-postings?
  "Check if a collection of postings is balanced"
  [postings]
  (= 0M (reduce + (map :amount postings))))

;; Building blocks
(s/def ::amount decimal?)
(s/def ::account (s/and string? account?))
(s/def ::commodity (s/and string? commodity?))
(s/def ::tag (s/and string? tag?))
(s/def ::comment string?)

;; posting stuff
(s/def ::cost decimal?)
(s/def ::posting (s/keys :req-un [::amount
                                  ::account
                                  ::commodity
                                  ]
                         :opt-un [::cost
                                  ::status
                                  ::comment
                                  ::tag]))

;; transaction stuff
(s/def ::note string?)
(s/def ::payee string?)
(s/def ::status #{"*" "!" ""}) ; Cleared, pending, unmarked
(s/def ::code string?)
(s/def ::date jt/local-date?)
(s/def ::postings (s/and (s/coll-of ::posting
                                    :kind vector
                                    :min-count 2)
                         balanced-postings?))
(s/def ::transaction (s/keys :req-un [::date
                                      ::payee
                                      ::status]
                             :opt-un [::code
                                      ::note
                                      ::comment
                                      ::tag
                                      ::postings]))
