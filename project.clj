(defproject news-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-time "0.15.0"]
                 [clj-http "3.9.1"]
                 [slingshot "0.12.2"]
                 [org.clojure/tools.logging "0.4.1"]
                 [amazonica "0.3.142"]
                 ]
  :main ^:skip-aot news-bot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
