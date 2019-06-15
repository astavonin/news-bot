(ns news-bot.cmake-test
  (:require [clojure.test :refer :all]
            [news-bot.test-utils :refer :all]
            [news-bot.sources.cmake :refer :all]
            [news-bot.sources.interface :as i]
            [news-bot.sources.common-dp :as common-dp]))



(deftest cmake-release-parser-test
  (let [page (load-test-page cmake-test-page)]
    (let [versions (parse-releases page)]
      (do
        (is (not-empty versions))
        (is (= (count versions) 7))
        (has-keys (first versions) #{:id :link :title :tags}))
      )))

(deftest test-data-provider
  (with-redefs [common-dp/load-page (fn [_] (load-test-page cmake-test-page))]
    (do
      (let [dp     (get-data-provider #{"3.14.4"})
            news   (i/load-news dp)
            latest (first news)]
        (is (= 1 (count news)))
        (has-keys latest #{:id :link :title :tags})
        (is (= "3.14.5" (latest :id))))
      (let [dp   (get-data-provider #{"3.14.5"})
            news (i/load-news dp)]
        (is (empty? news)))
      (let [dp     (get-data-provider #{})
            news   (i/load-news dp)
            latest (first news)]
        (is (= 1 (count news)))
        (has-keys latest #{:id :link :title :tags})
        (is (= "3.14.5" (latest :id)))))))

;(run-tests 'news-bot.cmake-test)
