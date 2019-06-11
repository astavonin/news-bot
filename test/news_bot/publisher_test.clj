(ns news-bot.publisher-test
  (:require [clojure.test :refer :all]
            [news-bot.publisher :refer :all]
            [news-bot.sources.overload :refer :all]
            [news-bot.test-utils :refer :all]
            [twitter.api.restful :as t]
            [news-bot.sources.so :as so]
            [news-bot.so-test :as sot]
            [clj-time.core :as time]))

(defn setup-wrapper [f]
  (setup-localstack bucket-name)
  (f)
  (teardown-localstack bucket-name))

(use-fixtures :once setup-wrapper)

(deftest publish-overload-update-test
  (let [post-counter (atom 0)]
    (with-redefs [load-overload-main-page (fn [] (slurp overload-test-page))
                  t/statuses-update       (fn [& {:keys [_]}]
                                            (swap! post-counter inc))]
      (publish-overload-update bucket-name)
      (is (= @post-counter 1))
      ; update should be posted just once
      (publish-overload-update bucket-name)
      (is (= @post-counter 1)))))

(deftest publish-so-update-test
  (let [post-counter (atom 0)]
    (with-redefs [so/load-thread    (fn [tag _ _] (sot/load-by-tag tag))
                  t/statuses-update (fn [& {:keys [_]}]
                                      (swap! post-counter inc))]
      (let [dp (so/get-data-provider 3 :day)]
        (publish-so-update bucket-name dp (time/now))
        (is (= @post-counter 3))
        ; update should be posted just once (3 posts)
        (publish-so-update bucket-name dp (time/now))
        (is (= @post-counter 3))))))

(deftest publish-updates-test
  (let [so-dp        (atom {})
        post-counter (atom 0)]
    (with-redefs [publish-so-update       (fn [_ data-source _]
                                            (reset! so-dp data-source)
                                            (swap! post-counter inc))
                  publish-overload-update (fn [_])]
      (publish-updates bucket-name (time/date-time 2019 05 16 10 10))
      (is (= (:period @so-dp) :day))
      (is (= @post-counter 1))
      (publish-updates bucket-name (time/date-time 2019 05 19 10 10))
      (is (= (:period @so-dp) :week))
      (is (= @post-counter 2))
      (publish-updates bucket-name (time/date-time 2019 05 31 10 10))
      (is (= (:period @so-dp) :month))
      (is (= @post-counter 3))
      (publish-updates bucket-name (time/date-time 2019 12 31 10 10))
      (is (= (:period @so-dp) :year))
      (is (= @post-counter 4)))))

;(run-tests 'news-bot.publisher-test)
