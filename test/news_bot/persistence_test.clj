(ns news-bot.persistence-test
  (:require [clojure.test :refer :all]
            [news-bot.persistence :refer :all]
            [news-bot.stack-helper :as helper]
            [news-bot.test-utils :refer :all]))

(def bucket-name "localstack-test-bucket")

(defn setup-tests []
  ; configuration should include :secretsmanager :localstack but it's
  ; impossible due to LocalStack SecureManager implementation error:
  ; https://github.com/localstack/localstack/issues/1002
  (set-aws-endpoint :env-type {:s3 :localstack})
  (let [id (helper/start-stack)]
    (when-not (helper/wait-for-stack? id 60)
      (throw "LocalStack is not ready in 60s."))
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

(deftest store-load-posted-test
  (testing "error cases"
    (let [err-info (exception-info (store-data "wrong-bucket-name" :so #{1 2 3}))]
      (is (= (err-info :category) :news-bot.persistence/persistence))
      (is (= (err-info :type) :news-bot.persistence/s3-error)))
    )
  (testing "store/load data"
    (let [data #{1 3 5 7 9}]
      (store-data bucket-name :so data)
      (is (= (load-data bucket-name :so) data))))
  )

;(run-tests 'news-bot.persistence-test)
