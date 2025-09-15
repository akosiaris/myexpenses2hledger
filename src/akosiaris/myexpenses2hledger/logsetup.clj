(ns akosiaris.myexpenses2hledger.logsetup
  (:require [taoensso.timbre :as t]))

(defn setup-logging
  "Sets up a logging setup per configuration. Returns a no argument function that, if called, will start logging"
  [{:keys [verbose ecs-logging version]}]
    ; Figure out the default log level
  (let [defaultloglevel
        (cond
            ; No verbosity => WARN
          (= verbose 0)
          :warn
            ; -v once => INFO
          (= verbose 1)
          :info
            ; -v more than once => DEBUG
          (> verbose 1)
          :debug)]
    (t/set-min-level! defaultloglevel)
    (t/info ::log-pipeline-setup :version version :loglevel defaultloglevel))

  ; ECS logging not implemented yet
  ;(if ecs-logging
  ;  (m/start-publisher! {:type :console-json})
  ;  (m/start-publisher! {:type :console}))
  )