(ns news-bot.core
  (:gen-class)
  (:require [news-bot.publisher :as pub]
            [clj-time.core :as time]
            [news-bot.config :as conf]))

(defn -main
  [& args]
  (pub/publish-updates (conf/config :storage-bucket) (time/now)))

