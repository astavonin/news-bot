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
(defn gen-endpoint-info [service env-type]
  (let [port (case service
               :s3 4572
               :secretsmanager 4584)]
    (case env-type
      :localstack {:api               service
                    :endpoint-override {:protocol :http
                                        :hostname "localhost"
                                        :port     port}}
      :aws {:api service})))

(defn set-aws-endpoint [& {:keys [env-type]
                           :or   {env-type {:s3             :aws
                                            :secretsmanager :aws}}}]
  (run! #(let [[service dest] %]
           (case service
             :s3 (reset! s3 (aws/client (gen-endpoint-info service dest)))
             :secretsmanager (reset! sm (aws/client (gen-endpoint-info service dest)))))
        env-type))

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

(defn store-data [bucket source data & {:keys [destination]
                                 :or   {destination :twitter}}]
  {:pre [(s/valid? ::news-list data)
         (s/valid? string? bucket)
         (s/valid? keyword? source) (s/valid? keyword? destination)]}

  (let [key  (str/join "/" [(name destination) (name source)])
        body (io/input-stream (nippy/freeze (set data)))]
    (checked-s3-invoke {:op      :PutObject
                        :request {:Bucket bucket :Key key :Body body}})))

(defn- stream->bytes [is]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (io/copy is baos)
    (.toByteArray baos)))

(defn load-data [bucket source & {:keys [ destination]
                           :or   {destination :twitter}}]
  {:pre [(s/valid? string? bucket)
         (s/valid? keyword? source)]
   :post [(s/valid? ::news-list %)]}

  (let [key      (str/join "/" [(name destination) (name source)])
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

(defn load-twitter-cred [cred-id]
  {:pre [(s/valid? not-empty cred-id)]
   :post [(s/valid? ::twitter-cred %)]}

  (-> (checked-sm-invoke {:op      :GetSecretValue
                          :request {:SecretId cred-id}})
      (:SecretString)
      (json/read-str :key-fn keyword)))

;;;;; LocalStack has issues with SecretManager

;(set-aws-endpoint :env-type {:secretsmanager :localstack})

;(set-aws-endpoint)
;(aws/ops @sm)
;(load-twitter-cred)

;(aws/doc @sm :PutSecretValue)
;(aws/ops @sm)

;(aws/invoke @sm {:op :ListSecrets})
;(aws/invoke @sm {:op      :GetSecretValue :request {:SecretId "twitter/cpp_news_bot"}})

;(aws/invoke @sm {:op      :PutSecretValue
;                 :request {:SecretId     "twitter/cpp_news_bot"
;                           :SecretString (json/write-str {:AppKey          "AAA",
;                                                          :AppSecret       "BBB",
;                                                          :UserToken       "CCC",
;                                                          :UserTokenSecret "DDD"})}})
