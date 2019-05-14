(ns news-bot.persistence-test
  (:require [clojure.test :refer :all]
            [news-bot.persistence :refer :all]
            [news-bot.stack-helper :as helper]))

(def bucket-name "local-bucket-name")

(defn setup-tests []
  (let [id (helper/start-stack)]
    (if (not (helper/wait-for-stack? id 30))
      (throw "LocalStack is not ready in 30s."))
    (create-bucket bucket-name))
  (set-aws-endpoint {:protocol :http
                     :hostname "localhost"
                     :port     4572})
  )

(defn teardown-test []
  )

(defn setup-wrapper [f]
  (setup-tests)
  (f)
  (teardown-test))

(use-fixtures :once setup-wrapper)

(deftest load-posted-test
  )

(defmacro exception-info [& function]
  `(try
     (~@function)
     (catch Exception err#
       (ex-data err#)))
  )

(deftest store-posted-test
  (testing "error cases"
    (let [err-info (exception-info (store-posted bucket-name "not-a-set"))]
      (is (= (err-info :category) :news-bot.persistence/persistence))
      (is (= (err-info :type) :news-bot.persistence/invalid-data)))
    (let [err-info (exception-info (store-posted "wrong-bucket-name" #{1 2 3}))]
      (is (= (err-info :category) :news-bot.persistence/persistence))
      (is (= (err-info :type) :news-bot.persistence/s3-error)))
    (do
      (set-aws-endpoint {:protocol :http :hostname "localhost" :port 1111})
      (let [err-info (exception-info (store-posted bucket-name #{1 2 3}))]
        (is (= (err-info :category) :news-bot.persistence/persistence))
        (is (= (err-info :type) :news-bot.persistence/s3-error)))
      (set-aws-endpoint {:protocol :http :hostname "localhost" :port 4572})))
  (testing "storing data"
    ()
    )
  )
