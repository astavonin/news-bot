(ns news-bot.overload-test
  (:require [clojure.test :refer :all]
            [news-bot.sources.overload :refer :all]
            [news-bot.test-utils :refer :all]
            [hickory.core :as hc]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(deftest parse-journals-test
  (testing "journal information extraction"
    (let [page (-> (slurp overload-test-page)
                   hc/parse
                   hc/as-hickory)]
      (if-let [journals (extract-journals-list page)]
        (do
          (is (not-empty journals))
          (if-let [parsed (parse-journals journals)]
            (do
              (is (= 149 (count parsed)))
              (has-keys (first parsed) #{:id :link :title}))
            (throw "parse-journals failed")))
        (throw "extract-journals-list failed")))))

(deftest load-journal-list-test
  (testing "load and parse journals"
    (with-redefs [load-overload-main-page (fn [] (slurp overload-test-page))]
      (let [journals (load-journal-list)]
        (is (= 149 (count journals)))
        (has-keys (first journals) #{:id :link :title :tags}))
      )))

(deftest data-provider-test
  (with-redefs [load-overload-main-page (fn [] (slurp overload-test-page))]
    (let [dp (get-data-provider [])
          news (i/load-news dp)]
      (is (not-empty news))
      (is (s/valid? ::i/posts-coll news))
      (is (= 150 (:id (first news)))))
    (let [dp (get-data-provider [150])
          news (i/load-news dp)]
      (is (empty? news))
      (is (s/valid? ::i/posts-coll news))))
  (let [dp (get-data-provider [])
        id (i/id dp)]
    (is (= id :overload))))

(deftest http-errors-test
  (with-redefs [main-page "https://accu_error.org/index.php/journals/c78/"]
    (let [dp (get-data-provider [])
          news (i/load-news dp)]
      (is (empty? news))
      (is (s/valid? ::i/posts-coll news)))))

;(run-tests 'news-bot.overload-test)
