(ns akosiaris.myexpenses2hledger.spec
  (:require [clojure.spec.alpha :as s]
             [java-time.api :as jt]))

;; Some useful defs
;; An account name can support hierarchies via the : sign.
(def account-re #"^[\w -]+(:[\w -]+)*$")
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

;; Building blocks
(s/def ::amount decimal?)
(s/def ::account (s/and string? account?))
(s/def ::commodity (s/and string? commodity?))
(s/def ::tag (s/and string? tag?))

;; posting stuff
(s/def ::cost decimal?)
(s/def ::posting (s/keys :req-un [::amount
                                  ::account
                                  ::commodity
                                  ]
                         :opt-un [::cost
                                  ::tag]))

;; transaction stuff
(s/def ::note string?)
(s/def ::payee string?)
(s/def ::status #{"*" "!" ""}) ; Cleared, pending, unmarked
(s/def ::code string?)
(s/def ::date string?) ;; TODO: Fix this
(s/def ::postings (s/coll-of ::posting :into []))
(s/def ::transaction (s/keys :req-un [::date
                                      ::payee]
                             :opt-un [::status
                                      ::code
                                      ::note
                                      ::tag
                                      ::postings]))
