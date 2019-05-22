(ns news-bot.test-utils
  (:require [clojure.test :refer :all]
            [clojure.data :as d]))


(defn get-keys [rec]
  (set (keys rec)))

(defn has-keys [rec keys]
  (is (map? rec))
  (let [[l r _] (d/diff (get-keys rec) keys)]
    (is (nil? l))
    (is (nil? r))
    )
  )

(defmacro exception-info [& function]
  `(try
     ~function
     (catch Exception err#
       (ex-data err#)))
  )
