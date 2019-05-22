(ns news-bot.core
  (:gen-class)
  (:require [news-bot.publisher :as pub]
            [clj-time.core :as time]
            [news-bot.config :as config]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (pub/publish-updates (config/config :storage-bucket) (time/now)))

