(ns news-bot.publisher
  (:require [news-bot.sources.so :as so]
            [news-bot.twitter :as twitter]
            [clj-time.core :as time]
            [news-bot.sources.overload :as overload]
            [news-bot.sources.boost :as boost]
            [news-bot.sources.cmake :as cmake]
            [news-bot.sources.clang-features :as clang-features]
            [news-bot.persistence :as p]
            [news-bot.sources.interface :as sources]
            [taoensso.timbre :as log]))

(defn publish-so-update [bucket data-source date]
  (let [posted-dates (try
                       (p/load-data bucket :so)
                       (catch Exception _ #{}))
        day          (str (time/with-time-at-start-of-day date))]
    (if-not (contains? posted-dates day)
      (let [posted (twitter/post-updates data-source)]
        (if-not (empty? posted)
          (do
            (p/store-data bucket :so (conj posted-dates day))
            (log/info "SO update" posted "was successfully posted"))))
      (log/debug "we'd already made SO update today" date))))

(defn publish-update [bucket data-provider-getter]
  (let [source         (sources/id (data-provider-getter))
        already-posted (try
                         (p/load-data bucket source)
                         (catch Exception _ #{}))
        just-posted    (twitter/post-updates (data-provider-getter already-posted))]
    (when-not (empty? just-posted)
      (do
        (p/store-data bucket source (into already-posted just-posted))
        (log/info (name source) "update(s)" just-posted "was successfully posted")))))

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
  (publish-update bucket overload/get-data-provider)
  (publish-update bucket cmake/get-data-provider)
  (publish-update bucket boost/get-data-provider)
  ;(publish-update bucket clang-features/get-data-provider)  ; something mysterious is going on here
  )


;(publish-update "cpp-news-bot-singapore" clang-features/get-data-provider)
;
;(def source :clang-cxx)
;(def bucket "cpp-news-bot-singapore")
;
;(def already-posted (p/load-data bucket source))
;(def ds (clang-features/get-data-provider already-posted))
;(twitter/post-updates ds)
;(sources/load-news ds)

;(try
;  (t/post-updates so-news-reader)
;  (catch Exception e
;    (println e)))
