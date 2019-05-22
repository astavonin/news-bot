(ns news-bot.twitter-test
  (:require [clojure.test :refer :all]
            [twitter.api.restful :as t]
            [news-bot.sources.interface :as i]
            [news-bot.twitter :refer :all]
            [clojure.string :as str]
            [news-bot.test-utils :refer :all]))

(defrecord TestDataProvider [count] i/DataProvider
  (load-news [_] (map
                   (fn [id]
                     {:id id :title "some title" :link "http://foo.boo" :tags ["c++" "c++11" "just-tag"]})
                   (range count)))
  (id [_] :tests))

(deftest post-updates-test
  (testing "successful Twitter posting"
    (with-redefs [t/statuses-update (fn [& {:keys [params]}]
                                      (let [text (:status params)]
                                        (is (str/includes? text "#cpp #cpp11 #just-tag"))
                                        (is (str/includes? text "some title"))
                                        (is (str/includes? text "http://foo.boo"))))]
      (let [dp (TestDataProvider. 3)]
        (is (= #{0 1 2} (set (post-updates dp)))))
      (let [dp (TestDataProvider. 1)]
        (is (= #{0} (set (post-updates dp)))))
      (let [dp (TestDataProvider. 0)]
        (is (empty? (post-updates dp))))
      ))
  (testing "failed Twitter posting"
    (set-cred {:AppKey "wrong-data" :AppSecret "wrong-data" :UserToken "wrong-data" :UserTokenSecret "wrong-data"})
    (let [dp (TestDataProvider. 1)]
      (let [err-info (exception-info (post-updates dp))]
        (is (= (err-info :category) :news-bot.twitter/posting))
        (is (= (err-info :type) :news-bot.twitter/twitter))))))

;(run-tests 'news-bot.twitter-test)

