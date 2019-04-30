(ns news-bot.data-provider
  (:require [clojure.data.json :as json]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clojure.string :as str]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+ try+]]
            [clojure.string :as s]))

(def so-api-endpoint "https://api.stackexchange.com")
(def so-api-version "2.2")
(def so-questions (s/join "/" [so-api-endpoint so-api-version "questions"]))
(def so-query-params {:pagesize 10
                      :sort     "votes"
                      :site     "stackoverflow"})
(def so-cpp-tags ["c++" "c++11" "c++17" "c++20"])

(defn load-thread [tag on-date & {:keys [period] :or {period 12}}]
  (let [from-date (t/minus on-date (t/hours period))
        to-date   (t/plus on-date (t/hours period))]
    (try+
      (let [resp (http/get so-questions {:query-params (assoc so-query-params
                                                         :tagged tag
                                                         :fromdate (tc/to-epoch from-date)
                                                         :todate (tc/to-epoch to-date))})]
        (resp :body)
        )
      ;(catch [:status 400] {:keys [body]}
      ;  (log/warn "NOT Found 400" body))
      (catch Object _
        (log/error "unable to load thread for tag" tag (:message &throw-context))
        ""
        ))
    )
  )

;(load-thread "c++" (t/now))

(defn- so-value-reader [key value]
  (if (str/ends-with? (name key) "_date")
    (tc/from-epoch value)
    value))

(defn read-feed-on [tags on-date]
  (flatten (map #((json/read-str (load-thread % on-date)
                                 :value-fn so-value-reader
                                 :key-fn keyword)
                   :items)
                tags))
  )


(defn cpp-feed [on-date] (distinct
                           (read-feed-on so-cpp-tags on-date)
                           )
  )

(defn get-so-news
  [& {:keys [count on-date] :or {count   3
                                 on-date (t/now)}}]
  (map #(select-keys % [:tags :title :score :link])
       (take count
             (sort-by :score #(compare %2 %1) (cpp-feed on-date))
             ))
  )
