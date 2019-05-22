(ns news-bot.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn load-config [config-name]
  (edn/read-string (slurp (io/resource config-name))))

(def config (load-config "config.edn"))
