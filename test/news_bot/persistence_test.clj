(ns news-bot.persistence-test
  (:require [clojure.test :refer :all]
            [news-bot.persistence :refer :all]
            [news-bot.stack-helper :as helper]))

(def bucket-name "localstack-test-bucket")

(defn setup-tests []
  (set-aws-endpoint {:protocol :http
                     :hostname "localhost"
                     :port     4572})
  (let [id (helper/start-stack)]
    (when-not (helper/wait-for-stack? id 30)
      (throw "LocalStack is not ready in 30s."))
    (create-bucket bucket-name))
  )

(defn teardown-test []
  (delete-all-objects bucket-name)
  (delete-bucket bucket-name))

(defn setup-wrapper [f]
  (setup-tests)
  (f)
  (teardown-test))

(use-fixtures :once setup-wrapper)

(deftest load-posted-test
  )

(defmacro exception-info [& function]
  `(try
     ~function
     (catch Exception err#
       (ex-data err#)))
  )

(deftest store-load-posted-test
  (testing "error cases"
    (let [err-info (exception-info (store-data "wrong-bucket-name" #{1 2 3}))]
      (is (= (err-info :category) :news-bot.persistence/persistence))
      (is (= (err-info :type) :news-bot.persistence/s3-error)))
    (do
      (set-aws-endpoint {:protocol :http :hostname "localhost" :port 1111})
      (let [err-info (exception-info (store-data bucket-name #{1 2 3}))]
        (is (= (err-info :category) :news-bot.persistence/persistence))
        (is (= (err-info :type) :news-bot.persistence/s3-error)))
      (set-aws-endpoint {:protocol :http :hostname "localhost" :port 4572})))
  (testing "store/load data"
    (let [data #{1 3 5 7 9}]
      (store-data bucket-name data)
      (is (= (load-data bucket-name) data))))
  )
