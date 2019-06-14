(ns news-bot.persistence-test
  (:require [clojure.test :refer :all]
            [news-bot.persistence :refer :all]
            [news-bot.test-utils :refer :all]))

(defn setup-wrapper [f]
  (setup-localstack bucket-name)
  (f)
  (teardown-localstack bucket-name))

(use-fixtures :once setup-wrapper)

(deftest load-posted-test
  )

(deftest store-load-posted-test
  (testing "error cases"
    (let [err-info (exception-info (store-data "wrong-bucket-name" :so #{"1" "2" "3"}))]
      (is (= (err-info :category) :news-bot.persistence/persistence))
      (is (= (err-info :type) :news-bot.persistence/s3-error)))
    )
  (testing "store/load data"
    (let [data #{"1" "3" "5" "7" "9"}]
      (store-data bucket-name :so data)
      (is (= (load-data bucket-name :so) data))))
  )

;(run-tests 'news-bot.persistence-test)
