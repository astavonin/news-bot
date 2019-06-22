(ns news-bot.core
  (:gen-class)
  (:require [news-bot.publisher :as pub]
            [clj-time.core :as time]
            [news-bot.config :as conf]
            [taoensso.timbre :as log]))

(defn -main
  [& args]
  (try
    (pub/publish-updates (conf/config :storage-bucket) (time/now))
    (catch Exception e
      (log/error "Unable to publish updates with error:" e))))

