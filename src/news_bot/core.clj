(ns news-bot.core
  (:gen-class)
  (:require [news-bot.publisher :as pub]
            [clj-time.core :as time]
            [news-bot.config :as conf]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (pub/publish-updates (conf/config :storage-bucket) (time/now)))

