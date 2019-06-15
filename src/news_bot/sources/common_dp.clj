(ns news-bot.sources.common-dp
  (:require [news-bot.sources.interface :as i]
            [version-clj.core :as ver]
            [clojure.spec.alpha :as s]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-http.client :as http]
            [taoensso.timbre :as log]
            [hickory.core :as hc]
            [clojure.string :as string]))

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

(defn trim-content [content]
  (-> content
      :content first string/trim))

(defn- load-releases [news-page parser]
  {:post [(s/valid? ::i/posts-coll %)]}
  (let [page (load-page news-page)]
    (parser page)))

(defn load-news [already-posted news-page parser]
  (let [latest     (first (sort-by :id #(ver/version-compare %2 %1) (load-releases news-page parser)))
        max-posted (last (ver/version-sort already-posted))]
    (if (not-empty latest)
      (if (or (empty? max-posted)
              (= -1 (ver/version-compare max-posted (:id latest))))
        [latest]
        ())
      ())))
