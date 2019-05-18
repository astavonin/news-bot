(ns news-bot.core
  (:gen-class)
  (:require [news-bot.sources.interface :as i]
            [news-bot.sources.so :as so]
            [news-bot.twitter :as t]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;(def so-news-reader (so/get-data-provider "SO question of the day: \"%s\"" 3 :month))
;
;
;(try
;  (t/post-updates so-news-reader)
;  (catch Exception e
;    (println e)))
;
