(ns akosiaris.myexpenses2hledger.logsetup
  (:require [com.brunobonacci.mulog :as m]))

(defn setup-logging
  "Sets up a logging setup per configuration. Returns a no argument function that, if called, will start logging"
  [{:keys [verbose ecs-logging version]}]
    ; Figure out the default log level
  (let [defaultloglevel
        (cond
            ; No verbosity => WARN
          (= verbose 0)
          :WARN
            ; -v once => INFO
          (= verbose 1)
          :INFO
            ; -v more than once => DEBUG
          (> verbose 1)
          :DEBUG)]
    (m/set-global-context! {:app-name "myexpenses2hledger",
                            :version version,
                            :level defaultloglevel,
                            :env "local"})
    (m/log ::log-pipeline-setup "event" "Logging set up" :version version :loglevel defaultloglevel))

  (if ecs-logging
    (m/start-publisher! {:type :console-json})
    (m/start-publisher! {:type :console})))