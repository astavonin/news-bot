(defproject news-bot "0.1.0"
  :description "CppNews Twitter Bot"
  :url "https://twitter.com/NewsCpp"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "0.2.7"]
                 [com.taoensso/timbre "4.10.0"]
                 [clj-time "0.15.2"]
                 [twitter-api "1.8.0"]
                 [com.cognitect.aws/api "0.8.408"]
                 [com.cognitect.aws/endpoints "1.1.11.686"]
                 [com.cognitect.aws/secretsmanager "770.2.568.0"]
                 [com.cognitect.aws/s3 "780.2.583.0"]
                 [lispyclouds/clj-docker-client "0.3.2"]
                 [com.taoensso/nippy "2.14.0"]
                 [slingshot "0.12.2"]
                 [hickory "0.7.1"]
                 [uswitch/lambada "0.1.2"]
                 [version-clj "0.1.2"]]
  :plugins [[lein-cloverage "1.1.1"]
            [lein-ancient "0.6.15"]]
  :main ^:skip-aot news-bot.core
  :target-path "target/%s"
  :uberjar-name "news-bot.jar"
  :profiles {:uberjar {:aot :all}})
