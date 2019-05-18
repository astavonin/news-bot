(ns news-bot.twitter
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as t]
            [news-bot.persistence :as p]
            [news-bot.sources.interface :as sources]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(def ^:private cred (atom (let [{app-key           :AppKey
                                 app-secret        :AppSecret
                                 user-token        :UserToken
                                 user-token-secret :UserTokenSecret}
                                (p/load-twitter-cred)]
                            (oauth/make-oauth-creds app-key app-secret user-token user-token-secret))))

(defn set-cred [new-cred]
  {:pre [(s/valid? ::p/twitter-cred new-cred)]}

  (let [{app-key           :AppKey
         app-secret        :AppSecret
         user-token        :UserToken
         user-token-secret :UserTokenSecret}
        new-cred]
    (reset! cred (oauth/make-oauth-creds app-key app-secret user-token user-token-secret))))

;(def rec {:tags  ["c++" "debugging" "runtime" "c++17"],
;          :title "SO question of the day: \"How to track all places where a class variable is modified by external code?\"",
;          :score 5,
;          :link  "https://stackoverflow.com/questions/56153917/how-to-track-all-places-where-a-class-variable-is-modified-by-external-code",
;          :id    56153917})

;(str (rec :title) "\n\n"
;     (str/join " " (map #(str "#" %) (rec :tags))) "\n\n"
;     (rec :link))


;(map #(str "#" (str/replace % #"c\+\+" "cpp")) (rec :tags))

(defn preprocess-tag [tag]
  (str/replace tag #"c\+\+" "cpp"))

(defn post-update [rec]
  (let [tags (str/join " " (map #(str "#" (preprocess-tag %)) (rec :tags)))
        msg (str (rec :title) "\n\n" tags "\n\n" (rec :link))]
    (t/statuses-update :oauth-creds @cred
                       :params {:status msg})
    (rec :id)))

;(post-update rec)

(defn post-updates [update]
  {:pre [(s/valid? ::sources/data-provider update)]
   :post [(s/valid? ::p/news-list %)]}

  (map post-update (sources/load-news update)))
