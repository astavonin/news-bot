(ns news-bot.persistence
  (:require [cognitect.aws.client.api :as aws]
            [clojure.data.json :as json]
            [taoensso.nippy :as nippy]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            ))

; -----------------------
; in/out data data validators
; -----------------------
(s/def ::news-id int?)
(s/def ::news-list (s/coll-of ::news-id))
(s/def ::twitter-cred (s/map-of keyword? string?))

; -----------------------
; S3 and SecretManager state
; -----------------------
(def ^:private s3 (atom (aws/client {:api :s3})))
(def ^:private sm (atom (aws/client {:api :secretsmanager})))


; -----------------------
; S3 implementation
; -----------------------
(defn set-aws-endpoint [& endpoint-override]
  (let [override+ #(if (some? endpoint-override)
                     (assoc % :endpoint-override (first endpoint-override))
                     %)
        s3-config (override+ {:api :s3})
        sm-config (override+ {:api :secretsmanager})]
    (reset! s3 (aws/client s3-config))
    (reset! sm (aws/client sm-config))
    )
  )

(defmacro checked-s3-invoke [keys]
  `(let [response# (aws/invoke @s3 ~keys)]
     (if (response# :cognitect.anomalies/category)
       (throw (ex-info "S3 error" {:category ::persistence :type ::s3-error
                                   :data     (or (response# :Error) response#)}))
       response#)))

(defn create-bucket [name & {:keys [location]
                             :or   {location "us-west-1"}}]
  (checked-s3-invoke {:op      :CreateBucket
                      :request {:Bucket name
                                :CreateBucketConfiguration
                                        {:LocationConstraint location}}}))


(defn delete-all-objects [bucket]
  (let [objects (into [] (map #(let [val (% :Key)] {:Key val})
                              ((checked-s3-invoke {:op      :ListObjects
                                                   :request {:Bucket bucket}})
                                :Contents)))]
    (checked-s3-invoke {:op      :DeleteObjects
                        :request {:Delete {:Objects objects}
                                  :Bucket bucket}})))

(defn delete-bucket [name]
  (checked-s3-invoke {:op      :DeleteBucket
                      :request {:Bucket name}}))

(defn store-data [bucket data & {:keys [source destination]
                                 :or   {source      :so
                                        destination :twitter}}]
  {:pre [(s/valid? ::news-list data)
         (s/valid? string? bucket)]}

  (let [key  (str/join "/" [(name source) (name destination)])
        body (io/input-stream (nippy/freeze data))]
    (checked-s3-invoke {:op      :PutObject
                        :request {:Bucket bucket :Key key :Body body}})))

(defn- stream->bytes [is]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (io/copy is baos)
    (.toByteArray baos)))

(defn load-data [bucket & {:keys [source destination]
                           :or   {source      :so
                                  destination :twitter}}]
  {:post [(s/valid? ::news-list %)]}

  (let [key      (str/join "/" [(name source) (name destination)])
        raw-data (checked-s3-invoke {:op      :GetObject
                                     :request {:Bucket bucket :Key key}})]
    (-> (raw-data :Body)
        (stream->bytes)
        (nippy/thaw)
        )))

; -----------------------
; SecureManager implementation
; -----------------------
(defmacro checked-sm-invoke [keys]
  `(let [response# (aws/invoke @sm ~keys)]
     (if (response# :cognitect.anomalies/category)
       (throw (ex-info "SecretsManager error" {:category ::persistence :type ::secretsmanager-error
                                               :data     (or (response# :Error) response#)}))
       response#)))

(defn load-twitter-cred []
  {:post [(s/valid? ::twitter-cred %)]}

  (-> (checked-sm-invoke {:op      :GetSecretValue
                          :request {:SecretId "twitter/cpp_news_bot"}})
      (:SecretString)
      (json/read-json)))
