(ns news-bot.so-test
  (:require [clojure.test :refer :all]
            [news-bot.sources.so :refer :all]
            [clj-time.core :as time]
            [news-bot.test-utils :refer :all]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))

(defn load-by-tag [tag]
  (slurp (str "test/data/questions/" (case tag
                                       "c++" "cpp.json"
                                       "c++11" "cpp11.json"
                                       "c++17" "cpp17.json"
                                       "c++20" "cpp20.json"))))

(deftest so-data-parsing
  (with-redefs [load-thread (fn [tag _ _] (load-by-tag tag))]

    (testing "StackOverflow feed should be parsed"
      (is (seq? (read-feed-on ["c++"] (time/now) 12)))

      (is (= (count (read-feed-on ["c++"] (time/now) 12)) 30))
      (is (= (count (read-feed-on ["c++11"] (time/now) 12)) 15))
      (is (= (count (read-feed-on ["c++20"] (time/now) 12)) 0))
      (is (= (count (read-feed-on ["c++" "c++11" "c++20"] (time/now) 12)) 45))
      )
    (testing "StackOverflow data record structure"
      (let [rec (first (read-feed-on ["c++"] (time/now) 12))]
        (has-keys rec #{:view_count
                        :tags
                        :last_edit_date
                        :answer_count
                        :last_activity_date
                        :title
                        :score
                        :creation_date
                        :link
                        :accepted_answer_id
                        :is_answered
                        :question_id
                        :owner})
        )
      )
    (testing "top news representation"
      (is (count (get-so-news)) 3)
      (is (count (get-so-news :count 5)) 5)

      (let [top-news (get-so-news)
            top-one  (first top-news)]
        (is (seq? top-news))

        (has-keys top-one #{:tags
                            :id
                            :score
                            :title
                            :link})))))

(deftest build-header-test
  (is (= (build-header :day (time/now)) "the day"))
  (is (= (build-header :week (time/now)) "the week"))
  (is (= (build-header :month (time/date-time 2019 04 30)) "April"))
  (is (= (build-header :year (time/date-time 2018 12 31)) "the 2018"))
  )

(deftest http-errors-test
  (with-redefs [so-questions "https://api.stackexchange_error.com"]
    (let [dp   (get-data-provider 3 :day)
          news (i/load-news dp)]
      (is (empty? news))
      (is (s/valid? ::i/posts-coll news)))))

;(run-tests 'news-bot.so-test)
