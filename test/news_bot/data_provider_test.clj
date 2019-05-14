(ns news-bot.data-provider-test
  (:require [clojure.test :refer :all]
            [news-bot.data-provider :refer :all]
            [clj-time.core :as t]
            [clojure.data :as d]
            )
  )

(defn- load-by-tag [tag]
  (slurp (str "test/data/questions/" (case tag
                                       "c++" "cpp.json"
                                       "c++11" "cpp11.json"
                                       "c++17" "cpp17.json"
                                       "c++20" "cpp20.json")))
  )

(defn- get-keys [rec]
  (set (keys rec)))

(defn- has-keys [rec keys]
  (is (map? rec))
  (let [[l r _] (d/diff (get-keys rec) keys)]
    (is (nil? l))
    (is (nil? r))
    )
  )

(deftest so-data-parsing
  (with-redefs [load-thread (fn [tag _ _] (load-by-tag tag))]

    (testing "StackOverflow feed should be parsed"
      (is (seq? (read-feed-on ["c++"] (t/now) 12)))

      (is (= (count (read-feed-on ["c++"] (t/now) 12)) 30))
      (is (= (count (read-feed-on ["c++11"] (t/now) 12)) 15))
      (is (= (count (read-feed-on ["c++20"] (t/now) 12)) 0))
      (is (= (count (read-feed-on ["c++" "c++11" "c++20"] (t/now) 12)) 45))
      )
    (testing "StackOverflow data record structure"
      (let [rec (first (read-feed-on ["c++"] (t/now) 12))]
        (has-keys rec #{:view_count
                        :tags
                        :last_edit_date
                        :answer_count
                        :last_activity_date
                        :title
                        :creation_date
                        :link
                        :score
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
                            :title
                            :score
                            :link})
        (is (= (top-one :score) 10))
        )
      )
    )
  )