(ns news-bot.sources.clang-features
  (:require [news-bot.sources.common-dp :as common-dp]
            [hickory.select :as hs]
            [clojure.string :as str]
            [news-bot.sources.interface :as i]
            [clojure.spec.alpha :as s]))


(def clang-cxx-status-page "https://clang.llvm.org/cxx_status.html")
(def cxx-20-table-nr 3)

;(spit "cxx_status.html" (:body (http/get clang-cxx-status-page)))

(defn- join-content [content]
  (str/join "" (keep #(if-let [rec (:content %)]
                        (first rec)
                        %) (:content content))))

(defn extract-content [content]
  (into []
        (keep #(if (:content %)
                 (join-content %))
              content)))

(defn get-rows-count [record]
  (if-let [val (first (keep #(get-in % [:attrs :rowspan])
                            record))]
    (read-string val)
    1))

(defn extract-url [record]
  (first (keep #(if-let [content (:content %)]
                  (get-in (first content) [:attrs :href]))
               record)))

(defn load-tables [page]
  (hs/select (hs/child (hs/tag :tbody)) page))

(defn process-record [record]
  (let [content (extract-content record)]
    (if-not (empty? content)
      [content (extract-url record) (get-rows-count record)])))

(defn- extract-records [table]
  (let [preparsed (keep (fn [elem]
                          (let [rec (process-record (:content elem))]
                            (if-not (empty? rec)
                              rec))) (:content table))]
    (reduce (fn [l [[_ doc impl :as descr] url count]]
              (if (= count 1)
                (let [[[header_prev _ impl_prev] _ count_prev] (last l)
                      count_prev (or count_prev 1)]
                  (if (> count_prev 1)
                    (conj l [[header_prev doc (or impl impl_prev)] url (dec count_prev)])
                    (conj l [descr url count])))
                (conj l [descr url count])))
            [] preparsed)))

(defn process-table [table]
  (let [parsed (extract-records table)]
    (map (fn [[[header proposal ver] url _]]
           {:id    proposal
            :title (format "%s: %s is available since %s" proposal header ver)
            :link  url
            :tags  ["c++" "c++20" "clang" proposal]})
         (filter (fn fn [[[_ _ ver] _ _]]
                   (re-find #"^Clang.*" (or ver ""))) parsed))))

(defn parse-versions [page]
  (process-table
    (nth (load-tables page) cxx-20-table-nr)))

(defrecord ClangCxxDataProvider [already-posted] i/DataProvider
  (load-news [_]
    (let [features (parse-versions (common-dp/load-page clang-cxx-status-page))]
      (keep #(if-not (contains? already-posted (:id %))
               %) features)))
  (id [_] :clang-cxx))

(defn get-data-provider
  ([]
   {:post [(s/valid? ::i/data-provider %)]}
   (ClangCxxDataProvider. #{}))
  ([already-posted]
   {:post [(s/valid? ::i/data-provider %)]}
   (ClangCxxDataProvider. (if (not-empty already-posted)
                            already-posted
                            #{}))))

;(def dp (get-data-provider #{"P0482R6" "P0722R3" "P0683R1"}))
;
;(try
;  (i/load-news dp)
;  (catch Exception e
;    (println e)))
