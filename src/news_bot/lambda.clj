(ns news-bot.lambda
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [news-bot.publisher :as pub]
            [clj-time.core :as time]
            [news-bot.config :as conf]
            [taoensso.timbre :as log]))


(defn handle-event
  [event]
  (try
    (pub/publish-updates (conf/config :storage-bucket) (time/now))
    (catch Exception e
      (log/error "Unable to publish updates with error:" e)))

  {:status "ok"})

(deflambdafn news-bot.lambda.LambdaFn
             [in out ctx]
             (let [event (json/read (io/reader in))
                   res   (handle-event event)]
               (with-open [w (io/writer out)]
                 (json/write res w))))
