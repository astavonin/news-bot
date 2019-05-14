(ns news-bot.twitter
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as t]))


(defprotocol EventsUploader
  (post-update [self update]))

(defrecord TwitterPoster [cred]
  EventsUploader
  (post-update [_ update]
    (t/statuses-update :oauth-creds cred
                       :params {:status update})
    )
  )

(def my-creds (oauth/make-oauth-creds ???))
