(ns news-bot.stack-helper
  (:require [clojure.test :refer :all]
            [clj-docker-client.core :as docker]
            [taoensso.timbre :as log]
            [clojure.string :as s]))


(defonce ^:private conn (docker/connect))

(defn run-stack [& {:keys [services]
                    :or   {services {:s3 4572}}}]
  (docker/run conn "localstack/localstack"
              "docker-entrypoint.sh"
              {:SERVICES (s/join "," (map #(name %) (keys services)))}
              (into {} (mapv #(vec [% %]) (vals services)))
              true))

(defn start-stack [& {:keys [name]
                      :or   {name "news-bot-localstack"}}]
  (try
    (docker/start conn name)
    (catch Exception _))
  name)

(defn remove-stack [id]
  (docker/stop conn id)
  (docker/rm conn id)
  )

(defn is-stack-ready? [stack-id]
  (some #(= "Ready." %) (docker/logs conn stack-id))
  )

(defn wait-for-stack? [stack-id attempts]
  (loop [att   attempts
         ready (is-stack-ready? stack-id)]
    (if (and (not ready) (not (zero? att)))
      (do
        (log/debug "pinging LocalStack, attempt" (- attempts att) "of" attempts)
        (Thread/sleep 1000)
        (recur (- att 1) (is-stack-ready? stack-id))
        )
      ready
      )
    ))