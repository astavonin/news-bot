(defproject news-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.taoensso/timbre "4.10.0"]
                 [clj-time "0.15.0"]
                 [twitter-api "1.8.0"]
                 [com.cognitect.aws/api "0.8.301"]
                 [com.cognitect.aws/endpoints "1.1.11.537"]
                 [com.cognitect.aws/secretsmanager "707.2.405.0"]
                 [com.cognitect.aws/s3 "714.2.430.0"]
                 [lispyclouds/clj-docker-client "0.2.3"]
                 [com.taoensso/nippy "2.14.0"]
                 [slingshot "0.12.2"]
                 [hickory "0.7.1"]
                 ]
  :main ^:skip-aot news-bot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
