(ns news-bot.sources.interface
  (:require [clojure.spec.alpha :as s]))


(s/def ::id string?)
(s/def ::title string?)
(s/def ::tags (s/coll-of string?))
(s/def ::link string?)
(s/def ::post-rec
  (s/keys :req-un [::id ::title ::link]
          :opt-un [::tags]))
(s/def ::posts-coll (s/coll-of ::post-rec))


(defprotocol DataProvider
  (load-news [this])
  (id [this])
  )

(s/def ::data-provider #(satisfies? DataProvider %))
