(ns news-bot.test-utils
  (:require [clojure.test :refer :all]
            [clojure.data :as d]
            [news-bot.stack-helper :as helper]
            [news-bot.persistence :as p]
            [hickory.core :as hc]))


(defn get-keys [rec]
  (set (keys rec)))

(defn has-keys [rec keys]
  (is (map? rec))
  (let [[l r _] (d/diff (get-keys rec) keys)]
    (is (nil? l))
    (is (nil? r))
    )
  )

(defmacro exception-info [& function]
  `(try
     ~function
     (catch Exception err#
       (ex-data err#)))
  )

(defn setup-localstack [bucket-name]
  ; configuration should include :secretsmanager :localstack but it's
  ; impossible due to LocalStack SecureManager implementation error:
  ; https://github.com/localstack/localstack/issues/1002
  (p/set-aws-endpoint :env-type {:s3 :localstack})
  (let [id (helper/start-stack)]
    (when-not (helper/wait-for-stack? id 60)
      (throw "LocalStack is not ready in 60s."))
    (p/create-bucket bucket-name))
  )

(defn teardown-localstack [bucket-name]
  (p/delete-all-objects bucket-name)
  (p/delete-bucket bucket-name))


(defn load-test-page [path]
  (-> (slurp path)
      hc/parse
      hc/as-hickory))

(def overload-test-page "test/data/overload/overload.html")
(def boost-test-page "test/data/boost/Boost News.htm")
(def bucket-name "localstack-test-bucket")
