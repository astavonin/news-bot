(ns news-bot.publisher
  (:require [news-bot.sources.so :as so]
            [news-bot.twitter :as twitter]
            [clj-time.core :as time]
            [news-bot.sources.overload :as o]
            [news-bot.persistence :as p]))

(defn- publish-so-update [data-source]
  (twitter/post-updates data-source))

(defn- publish-overload-update [bucket]
  (let [already-posted (p/load-data bucket :source :overload)]
    (if-let [just-posted (twitter/post-updates (o/get-data-provider already-posted))]
      (p/store-data bucket :overload (concat already-posted just-posted)))))

(defn publish-updates [bucket on-date]
  (let [so-data-source (if (= (time/day-of-week on-date) 7)
                         (so/get-data-provider 1 :week)
                         (so/get-data-provider 1 :day))]
    (publish-so-update so-data-source))
  (if (= (time/last-day-of-the-month on-date) (time/day on-date))
    (publish-so-update (so/get-data-provider 3 :month)))
  (if (and (= (time/month on-date) 12)
           (= (time/day on-date) 31))
    (publish-so-update (so/get-data-provider 3 :year)))
  (publish-overload-update bucket))

;(try
;  (t/post-updates so-news-reader)
;  (catch Exception e
;    (println e)))
