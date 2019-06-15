(ns news-bot.sources.cmake
  (:require [news-bot.sources.common-dp :as common-dp]))

(def cmake-news-page "https://blog.kitware.com/tag/cmake/")

(defn cmake-release-parser [page]
  (println page))

(common-dp/load-releases cmake-news-page cmake-release-parser)