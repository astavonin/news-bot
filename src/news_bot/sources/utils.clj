(ns news-bot.sources.utils
  (:require [clj-http.client :as http]
            [slingshot.slingshot :refer [throw+ try+]]
            [taoensso.timbre :as log]
            [hickory.core :as hc]))


(defn load-page [page]
  (try+
    (let [resp (http/get page)]
      (-> (resp :body)
          hc/parse
          hc/as-hickory))
    (catch [:status 400] {:keys [body]}
      (log/error "HTTP 400" body)
      "")
    (catch Object _
      (log/error "unable to load Overload" (:message &throw-context))
      "")))
