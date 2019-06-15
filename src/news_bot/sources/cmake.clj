(ns news-bot.sources.cmake
  (:require [news-bot.sources.common-dp :as common-dp]
            [hickory.select :as hs]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(def cmake-news-page "https://blog.kitware.com/tag/cmake/")

(defn parse-releases [page]
  (map (fn [ver-elem]
         (let [title (common-dp/trim-content ver-elem)
               ver   (-> title
                         (#(re-find #"CMake (\S+) available for download" %))
                         second)
               url   (-> ver-elem :attrs :href)]
           {:title title :id ver :link url :tags ["cmake"]}))
       (hs/select (hs/child (hs/find-in-text #"available for download$")) page)))

(common-dp/load-news #{} cmake-news-page parse-releases)

(defrecord CMakeDataProvider [already-posted] i/DataProvider
  (load-news [_] (common-dp/load-news already-posted cmake-news-page parse-releases))
  (id [_] :cmake))

(defn get-data-provider
  ([]
   {:post [(s/valid? ::i/data-provider %)]}
   (CMakeDataProvider. #{}))
  ([already-posted]
   {:post [(s/valid? ::i/data-provider %)]}
   (CMakeDataProvider. (if (not-empty already-posted)
                         already-posted
                         #{}))))
