(ns news-bot.compiler-features
  (:require [clojure.test :refer :all]
            [news-bot.test-utils :refer :all]
            [news-bot.sources.clang-features :refer :all]
            [news-bot.sources.interface :as i]
            [clojure.set :as set]))

(def content0 ["\n"
               {:type :element, :attrs nil, :tag :td, :content ["Default member initializers for bit-fields"]}
               "\n"
               {:type    :element,
                :attrs   nil,
                :tag     :td,
                :content [{:type :element, :attrs {:href "http://wg21.link/p0683r1"}, :tag :a, :content ["P0683R1"]}]}
               "\n"
               {:type :element, :attrs {:class "full", :align "center"}, :tag :td, :content ["Clang 6"]}
               "\n"])

(def content1 ["\n"
               {:type    :element,
                :attrs   nil,
                :tag     :td,
                :content [{:type :element, :attrs nil, :tag :tt, :content ["const&"]} "-qualified pointers to members"]}
               "\n"
               {:type    :element,
                :attrs   nil,
                :tag     :td,
                :content [{:type :element, :attrs {:href "http://wg21.link/p0704r1"}, :tag :a, :content ["P0704R1"]}]}
               "\n"
               {:type :element, :attrs {:class "full", :align "center"}, :tag :td, :content ["Clang 6"]}
               "\n"])

(def content2 ["\n"
               {:type    :element,
                :attrs   {:rowspan "2"},
                :tag     :td,
                :content [{:type :element, :attrs nil, :tag :tt, :content ["__VA_OPT__"]}
                          " for preprocessor comma elision"]}
               "\n"
               {:type    :element,
                :attrs   nil,
                :tag     :td,
                :content [{:type :element, :attrs {:href "http://wg21.link/p0306r4"}, :tag :a, :content ["P0306R4"]}]}
               "\n"
               {:type :element, :attrs {:class "full", :align "center"}, :tag :td, :content ["Clang 6"]}
               "\n"])

(deftest content-processors-test
  (is (= (extract-content content0)
         ["Default member initializers for bit-fields" "P0683R1" "Clang 6"]))
  (is (= (extract-content content1)
         ["const&-qualified pointers to members" "P0704R1" "Clang 6"]))
  (is (= (extract-content content2)
         ["__VA_OPT__ for preprocessor comma elision" "P0306R4" "Clang 6"]))
  (is (= 1 (get-rows-count content0)))
  (is (= 1 (get-rows-count content1)))
  (is (= 2 (get-rows-count content2)))
  (is (= (extract-url content0) "http://wg21.link/p0683r1"))
  (is (= (extract-url content1) "http://wg21.link/p0704r1"))
  (is (= (extract-url content2) "http://wg21.link/p0306r4")))

(deftest table-parsing-test
  (let [tables (load-tables (load-test-page clang-status-test-page))
        cxx20  (process-table (nth tables cxx-20-table-nr))]
    (is (= (count cxx20) 12))
    (has-keys (first cxx20) #{:id :link :title :tags})
    (is (contains? (into #{} cxx20)
                   {:id "P0482R6", :title "P0482R6: char8_t is available since Clang 7 (11)", :link "http://wg21.link/p0482r6", :tags ["c++" "c++20" "clang" "P0482R6"]}))))

(deftest test-data-provider
  (let [already-posted #{"P0482R6" "P0722R3" "P0683R1"}
        dp (get-data-provider already-posted)
        features (map #(:id %) (i/load-news dp))]
    (is (empty? (set/intersection already-posted (into #{} (map #(:id %) features)))))))

;(run-tests 'news-bot.compiler-features)
