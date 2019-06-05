(ns news-bot.publisher
  (:require [news-bot.sources.so :as so]
            [news-bot.twitter :as twitter]
            [clj-time.core :as time]
            [clj-time.format :as tf]
            [news-bot.sources.overload :as o]
            [news-bot.persistence :as p]
            [clj-time.coerce :as tc]
            [taoensso.timbre :as log]))

(defn publish-so-update [bucket data-source date]
  (let [posted-dates (try
                       (p/load-data bucket :so)
                       (catch Exception _ #{}))
        day          (tc/to-epoch (time/with-time-at-start-of-day date))]
    (if-not (contains? posted-dates day)
      (let [posted (twitter/post-updates data-source)]
        (if-not (empty? posted)
          (do
            (p/store-data bucket :so (conj posted-dates day))
            (log/info "SO update" posted "was successfully posted"))))
      (log/debug "we'd already made SO update today" date))))

(defn publish-overload-update [bucket]
  (let [already-posted (try
                         (p/load-data bucket :overload)
                         (catch Exception _ #{}))
        just-posted    (twitter/post-updates (o/get-data-provider already-posted))]
    (when-not (empty? just-posted)
      (do
        (p/store-data bucket :overload (into already-posted just-posted))
        (log/info "Overload update(s)" just-posted "was successfully posted")))))

(defn publish-updates [bucket on-date]
  ; only one SO update per day:
  ; - each last day of the week – best of the week
  ; - each last day os the month - best of the month
  ; - for 31 of Dec – best of the year
  (let [dp (if (= (time/last-day-of-the-month on-date) on-date)
             (if (= (time/month on-date) 12)                ; last day of the month
               (so/get-data-provider 3 :year)               ;   and last day of the year
               (so/get-data-provider 3 :month))             ;   just last day of the month
             (if (= (time/day-of-week on-date) 7)           ; not last day for the month
               (so/get-data-provider 1 :week)               ;   but last day of the week
               (so/get-data-provider 1 :day)))]             ;   just regular day
    (publish-so-update bucket dp on-date))
  (publish-overload-update bucket))

;(try
;  (t/post-updates so-news-reader)
;  (catch Exception e
;    (println e)))
