(ns news-bot.sources.boost
  (:require [hickory.select :as hs]
            [clojure.string :as string]
            [clojure.string :as str]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]
            [news-bot.sources.common-dp :as common-dp]))

(def boost-root "https://www.boost.org")
(def boost-news-page (str/join "/" [boost-root "/users/news/"]))

(defn create-changes-report [changes]
  (let [libs-pattern #"^New Libraries:(.*?\.)?.*Updated Libraries:(.*?\.)?(.*)$"
        parsed       (re-find libs-pattern (-> changes
                                               (#(str/split % #"\n"))
                                               str/join))
        libs-new     (nth parsed 1)
        libs-updated (nth parsed 2)
        split-libs   (fn [libs] (-> libs
                                    str/trim
                                    (#(str/split % #"[^\w]+"))))]
    (format "%s%s"
            (if (not-empty libs-new)
              (format "New libraries: %s." (str/join ", " (split-libs libs-new)))
              "")
            (if (not-empty libs-updated)
              (format "\n%d libraries updates." (count (split-libs libs-updated)))
              ""))))

(defn- parse-page [page]
  (partition-all 2 (hs/select
                     (hs/child (hs/or
                                 (hs/and
                                   (hs/tag :h2)
                                   (hs/class "news-title"))
                                 (hs/and
                                   (hs/tag :div)
                                   (hs/class "news-description"))))
                     page)))

(defn- trim-content [content]
  (-> content
      first :content first string/trim))

(defn parse-versions [page]
  (map (fn [[header descr]]
         (let [ver-elem (hs/select (hs/child (hs/find-in-text #"Version")) header)
               ver      (-> (trim-content ver-elem)
                            (#(re-find #"^Version (\S+)" %))
                            second)
               url      (str boost-root (-> ver-elem first :attrs :href))
               changes  (create-changes-report (trim-content (hs/select (hs/child (hs/class "purpose")) descr)))]
           {:title (format "BOOST %s is available. %s" ver changes) :link url :id ver :tags ["cpp" "lib-boost"]})
         )
       (parse-page page)))


(defrecord BoostDataProvider [already-posted] i/DataProvider
  (load-news [_] (common-dp/load-news already-posted boost-news-page parse-versions))
  (id [_] :boost))

(defn get-data-provider
  ([]
   {:post [(s/valid? ::i/data-provider %)]}
   (BoostDataProvider. #{}))
  ([already-posted]
   {:post [(s/valid? ::i/data-provider %)]}
   (BoostDataProvider. (if (not-empty already-posted)
                         already-posted
                         #{}))))

;(def dp (get-data-provider #{"1.69.0"}))
;
;(try
;  (i/load-news dp)
;  (catch Exception e
;    (println e)))
