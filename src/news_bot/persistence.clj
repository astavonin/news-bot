(ns news-bot.persistence
  (:require [cognitect.aws.client.api :as aws]
            [clojure.data.json :as json]
            [taoensso.nippy :as nippy]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def ^:private s3 (atom (aws/client {:api :s3})))

; 10d1ef92-5be3-492f-9065-7364b8417736

(defn set-aws-endpoint [& endpoint-override]
  (let [aws-service {:api :s3}
        aws-config  (if (some? endpoint-override)
                      (assoc aws-service :endpoint-override (first endpoint-override)))]
    (reset! s3 (aws/client aws-config))
    )
  )

(defn create-bucket [name & {:keys [location]
                             :or   {location "us-west-1"}}]

  (aws/invoke @s3 {:op :CreateBucket :request {:Bucket name
                                               :CreateBucketConfiguration
                                                       {:LocationConstraint location}}})
  )

(defn store-posted [bucket data & {:keys [source destination]
                                   :or   {source      :so
                                          destination :twitter}}]
  (if (not (set? data))
    (throw (ex-info "invalid input data format"
                    {:category ::persistence :type ::invalid-data
                     :data     {:bucket      bucket
                                :data        data
                                :data-type   (type data)
                                :source      source
                                :destination destination}})))
  (let [key      (s/join "/" [(name source) (name destination)])
        body     (io/input-stream (nippy/freeze data))
        response (aws/invoke @s3 {:op      :PutObject
                                  :request {:Bucket bucket :Key key :Body body}})]
    (if (or (response :cognitect.anomalies/category))
      (throw (ex-info "S3 error" {:category ::persistence :type ::s3-error
                                  :data     (or (response :Error) response)}))
      response)))

;(set-aws-endpoint {:protocol :http
;                   :hostname "localhost"
;                   :port     1111})
;                   :port     4572})

;(defn load-posted [bucket & {:keys [source destination]
;                             :or   {source      :so
;                                    destination :twitter}}]
;  (aws/invoke @s3 {:op :PutObject :request {:Bucket}})
;  )



;(aws/ops @s3)
;(aws/invoke @s3 {:op :ListBuckets})
;
;(aws/doc @s3 :PutObject)
;(aws/doc @s3 :GetObject)
;
;(def data {2 22 3 33})
;
;(nippy/thaw (nippy/freeze data))
;
;
;(aws/invoke @s3 {:op :PutObject :request {:Bucket "test-bucket" :Key "some-key"
;                                          :Body   (io/input-stream (nippy/freeze data))}})
;
;
;
;(def ss (aws/client {:api :secretsmanager}))
;
;(aws/ops ss)
;
;
;(def secrets [:AppKey :AppSecret :UserToken :UserTokenSecret])
;
;(def secret (aws/invoke ss {:op      :GetSecretValue
;                            :request {:SecretId "twitter/cpp_news_bot"}}))
;(aws/doc ss :GetSecretValue)
;
;(json/read-json (secret :SecretString))
