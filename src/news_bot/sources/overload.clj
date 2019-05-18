(ns news-bot.sources.overload
  (:require [clj-http.client :as http]
            [slingshot.slingshot :refer [throw+ try+]]
            [hickory.core :as hc]
            [taoensso.timbre :as log]
            [hickory.select :as hs]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(def main-page "https://accu.org/index.php/journals/c78/")

(defn load-overload-main-page []
  (try+
    (let [resp (http/get main-page)]
      (resp :body))
    (catch [:status 400] {:keys [body]}
      (log/error "HTTP 400" body)
      "")
    (catch Object _
      (log/error "unable to load Overload" (:message &throw-context))
      "")))

(defn parse-journals [journals]
  (map #(let [title (-> % :content first)]
          {:id    (Integer/parseInt (re-find #"\d+" title))
           :link  (get-in % [:attrs :href])
           :title title})
       journals))

(defn extract-journals-list [page]
  (hs/select (hs/child (hs/find-in-text #"Overload Journal #")) page))

(defn load-journal-list []
  {:post [(s/valid? ::i/posts-coll %)]}
  (let [page (-> (load-overload-main-page)
                 hc/parse
                 hc/as-hickory)]
    (if-let [journals (extract-journals-list page)]
      (map #(assoc % :tags ["overload"]) (parse-journals journals))
      [])))

(defrecord OverloadDataProvider [count] i/DataProvider
  (load-news [_]
    (let [header "New %s is available!"
          journals (take count (sort-by :id #(compare %2 %1) (load-journal-list)))]
      (map (fn [n] (update n :title #(format header %))) journals))))

;(def dp (OverloadDataProvider. 1))
;(i/load-news dp)