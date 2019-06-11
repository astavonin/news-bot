(ns news-bot.boost-test
  (:require [clojure.test :refer :all]
            [news-bot.test-utils :refer :all]
            [news-bot.sources.boost :refer :all]
            [news-bot.sources.utils :as utils]))

(deftest create-changes-report-test
  (let [libs1 "New Libraries: Safe Numerics. Updated Libraries: Any, Asio, Assign, Beast,\n       CircularBuffer, Concept Check, Core, DLL, Dynamic Bitset, Fiber, Filesystem,\n       Flyweight, Geometry, Integer, Iostreams, Iterator, LexicalCast, Log, Logic,\n       Math, Mp11, MultiArray, Multi-index Containers, Multiprecision, PolyCollection,\n       Pool, Preprocessor, Rational, Spirit, Stacktrace, System, Test, TypeIndex,\n       Utility, Variant, YAP. Discontinued Libraries: Signals."
        libs2 "New Libraries: Outcome, Histogram. Updated Libraries:\n "
        libs3 "New Libraries: YAP. Updated Libraries: Beast, Context, Coroutine2, Fiber, Fusion,\n       Geometry, GIL, Graph, Lexical Cast, Log, Math, Multiprecision, Optional, Predef,\n       Program Options, Python, Rational, System, Signals, Spirit, Stacktrace, Test,\n       TypeIndex, Uuid.\n "]
    (is (= (create-changes-report libs1)
           "New libraries: Safe, Numerics.\n40 libraries updates."))
    (is (= (create-changes-report libs2)
           "New libraries: Outcome, Histogram."))
    (is (= (create-changes-report libs3)
           "New libraries: YAP.\n26 libraries updates."))))

(deftest get-boost-news-test
  (let [page (load-test-page boost-test-page)]
    (let [versions (parse-versions page)]
      (do
        (is (not-empty versions))
        (is (= (count versions) 5))
        (has-keys (first versions) #{:id :link :title}))
      )))

;(run-tests 'news-bot.boost-test)

