(ns news-bot.overload-test
  (:require [clojure.test :refer :all]
            [news-bot.sources.overload :refer :all]
            [news-bot.test-utils :refer :all]
            [hickory.core :as hc]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(def overload-page "test/data/overload/overload.html")

(deftest parse-journals-test
  (testing "journal information extraction"
    (let [page  (-> (slurp overload-page)
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
    (with-redefs [load-overload-main-page (fn [] (slurp overload-page))]
      (let [journals (load-journal-list)]
        (is (= 149 (count journals)))
        (has-keys (first journals) #{:id :link :title :tags}))
        )))

;(run-tests 'news-bot.overload-test)
