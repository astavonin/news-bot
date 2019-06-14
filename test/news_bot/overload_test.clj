(ns news-bot.overload-test
  (:require [clojure.test :refer :all]
            [news-bot.sources.overload :refer :all]
            [news-bot.test-utils :refer :all]
            [hickory.core :as hc]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(deftest load-journal-list-test
  (testing "load and parse journals"
    (let [page (load-test-page overload-main-page)]
      (let [journals (load-journal-list page)]
        (is (= 150 (count journals)))
        (has-keys (first journals) #{:id :link :title :tags}))
      )))

(deftest http-errors-test
  (with-redefs [overload-main-page "https://accu_error.org/index.php/journals/c78/"]
    (let [dp (get-data-provider [])
          news (i/load-news dp)]
      (is (empty? news))
      (is (s/valid? ::i/posts-coll news)))))

;(run-tests 'news-bot.overload-test)
