(ns news-bot.sources.boost
  (:require [news-bot.sources.utils :as utils]
            [hickory.select :as hs]
            [clojure.string :as string]
            [clojure.string :as str]))

(def boost-root "https://www.boost.org")
(def news-page (str/join "/" [boost-root "/users/news/"]))

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
           {:title (format "BOOST %s is available. %s" ver changes) :link url :id ver})
         )
       (parse-page page)))

(defn load-boost-releases []
  (let [page (utils/load-page news-page)]
    (parse-versions page)))
