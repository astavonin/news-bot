(ns news-bot.sources.so
  (:require [clojure.data.json :as json]
            [clj-time.coerce :as tc]
            [clj-time.core :as time]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [clj-http.client :as http]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as log]
            [clojure.spec.alpha :as s]
            [news-bot.sources.interface :as i]))


(def so-api-endpoint "https://api.stackexchange.com")
(def so-api-version "2.2")
(def so-questions (str/join "/" [so-api-endpoint so-api-version "questions"]))
(def so-query-params {:pagesize 10
                      :sort     "votes"
                      :site     "stackoverflow"})
(def so-cpp-tags ["c++" "c++11" "c++17" "c++20"])

(defn load-thread [tag on-date period]
  (let [from-date (time/minus on-date (time/hours period))
        to-date   on-date]
    (log/debug "loading questions for" tag "on" on-date "with period" period "hours")
    (try+
      (let [resp (http/get so-questions {:query-params (assoc so-query-params
                                                         :tagged tag
                                                         :fromdate (tc/to-epoch from-date)
                                                         :todate (tc/to-epoch to-date))})]
        (resp :body))
      (catch [:status 400] {:keys [body]}
        (log/error "HTTP 400 for tag" tag body)
        "{}")
      (catch Object _
        (log/error "unable to load SO thread for tag" tag (:message &throw-context))
        "{}"))))

(defn clean-html [text]
  (-> text
      (str/replace #"&quot;" "\"")
      (str/replace #"&amp;" "&")
      (str/replace #"&lt;" "<")
      (str/replace #"&gt;" ">")
      (str/replace #"&#39;" "'")
      )
  )

(defn- so-value-reader [key value]
  (cond
    (str/ends-with? (name key) "_date") (tc/from-epoch value)
    (= key :title) (clean-html value)
    :else value
    )
  )

(defn read-feed-on [tags on-date period]
  (flatten (keep #((json/read-str
                     (load-thread % on-date period)
                     :value-fn so-value-reader :key-fn keyword)
                    :items)
                 tags))
  )

(defn cpp-feed [on-date period]
  (distinct
    (read-feed-on so-cpp-tags on-date period)
    )
  )

(defn get-so-news
  [& {:keys [count on-date period] :or {count   3
                                        on-date (time/now)
                                        period  24}}]
  {:post [(s/valid? ::i/posts-coll %)]}
  (map #(-> %
            (select-keys [:tags :question_id :title :score :link])
            (clojure.set/rename-keys {:question_id :id}))
       (take count (sort-by :score #(compare %2 %1) (cpp-feed (time/now) period)))))

(defn build-header [period date]
  (case period
    :month (tf/unparse (tf/formatter "MMMM") date)
    :year (str "the " (time/year date))
    (str "the " (name period))))

(defrecord SODataProvider [load-count period] i/DataProvider
  (load-news [_]
    (let [header     (str "%sSO post of " (build-header period (time/now)) ": \"%s\"")
          news       (get-so-news :count load-count :period (case period
                                                              :week (* 24 7)
                                                              :month (* 24 30)
                                                              :year (* 24 365)
                                                              24))
          news-count (count news)]
      (map-indexed (fn [pos val] (let [prefix (if (< 1 news-count)
                                                (format "%d/%d: " (inc pos) news-count)
                                                "")]
                                   (update val :title #(format header prefix %)))) news)))
  (id [_] :so))

(defn get-data-provider [count period]
  {:pre  [(s/valid? pos-int? count)
          (s/valid? keyword? period)]
   :post [(s/valid? ::i/data-provider %)]}
  (SODataProvider. count period)
  )

;(def dp (SODataProvider. 3 :day))
;
;(:period dp)
;(try
;  (i/load-news dp)
;  (catch Exception e
;    (println e)))
