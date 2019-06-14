(ns news-bot.sources.overload
  (:require [slingshot.slingshot :refer [throw+ try+]]
            [hickory.select :as hs]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]
            [news-bot.sources.common-dp :as common-dp]))

(def overload-main-page "https://accu.org/index.php/journals/c78/")

(defn parse-journals [journals]
  (map #(let [title (-> % :content first)]
          {:id    (re-find #"\d+" title)
           :link  (get-in % [:attrs :href])
           :title title})
       journals))

(defn extract-journals-list [page]
  (hs/select (hs/child (hs/find-in-text #"Overload Journal #")) page))

(defn load-journal-list [page]
  {:post [(s/valid? ::i/posts-coll %)]}
  (let [journals (extract-journals-list page)]
    (map #(assoc % :tags ["overload" "cpp" "accu"]) (parse-journals journals))))

(defrecord OverloadDataProvider [already-posted] i/DataProvider
  (load-news [_] (common-dp/load-news already-posted overload-main-page load-journal-list))
  (id [_] :overload))

(defn get-data-provider
  ([]
   {:post [(s/valid? ::i/data-provider %)]}
   (OverloadDataProvider. #{}))
  ([already-posted]
   {:post [(s/valid? ::i/data-provider %)]}
   (OverloadDataProvider. (if (not-empty already-posted)
                            already-posted
                            #{""}))))

;(def dp (get-data-provider []))
;
;(try
;  (i/load-news dp)
;  (catch Exception e
;    (println e)))
