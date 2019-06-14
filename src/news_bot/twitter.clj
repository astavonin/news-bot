(ns news-bot.twitter
  (:require [twitter.oauth :as oauth]
            [twitter.api.restful :as t]
            [news-bot.persistence :as p]
            [news-bot.sources.interface :as sources]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [news-bot.config :as conf]
            [clojure.edn :as edn]))

(def ^:private cred (atom (let [{app-key           :AppKey
                                 app-secret        :AppSecret
                                 user-token        :UserToken
                                 user-token-secret :UserTokenSecret}
                                (try
                                  (p/load-twitter-cred (conf/config :twitter-secret-id))
                                  (catch Exception e
                                    (log/warn "unable to load twitter credentials from persistence" e)
                                    ; for testing proposes only, because of LocalStack issue
                                    (let [f (io/file "_twitter.edn")]
                                      (if (.exists f)
                                        (edn/read-string (slurp f))
                                        {:AppKey "" :AppSecret "" :UserToken "" :UserTokenSecret ""}))))]
                            (oauth/make-oauth-creds app-key app-secret user-token user-token-secret))))

(defn set-cred [new-cred]
  {:pre [(s/valid? ::p/twitter-cred new-cred)]}

  (let [{app-key           :AppKey
         app-secret        :AppSecret
         user-token        :UserToken
         user-token-secret :UserTokenSecret}
        new-cred]
    (reset! cred (oauth/make-oauth-creds app-key app-secret user-token user-token-secret))))

(defn- preprocess-tag [tag]
  (-> tag
      (str/replace #"c\+\+" "cpp")
      (str/replace #"-" "_")))

(defn- post-update [rec]
  {:pre [(s/valid? ::sources/post-rec rec)]}

  (let [tags (str/join " " (map #(str "#" (preprocess-tag %)) (rec :tags)))
        msg  (str (rec :title) "\n\n" tags "\n\n" (rec :link))]
    (try
      (t/statuses-update :oauth-creds @cred :params {:status msg})
      (catch Exception e
        (log/error "Twitter posting error:" (ex-message e))
        (throw (ex-info "Twitter error" {:category ::posting
                                         :type     ::twitter
                                         :data     (ex-message e)}))))
    (rec :id)))

(defn post-updates [update]
  {:pre  [(s/valid? ::sources/data-provider update)]
   :post [(s/valid? ::p/news-list %)]}

  (into #{} (keep post-update (sources/load-news update))))

