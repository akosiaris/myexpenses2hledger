(ns akosiaris.myexpenses2hledger
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [taoensso.timbre :as t]
            [akosiaris.myexpenses2hledger.logsetup :refer [setup-logging]]
            [akosiaris.myexpenses2hledger.importer :refer [load-my-expenses-json]]
            [akosiaris.myexpenses2hledger.outputter :refer [write-hledger-journal]])
  (:gen-class))

(def ^:private VERSION "0.3.0")

(def ^:private cli-options
  [;; output file
   ["-o" "--output FILE" "Path to output" :missing "Output file required"]
   ;; input. Can be a file or a directory
   ["-i" "--input FILE" "Path to input. Can be file or directory" :missing "Input file/directory required"]
   ;; Equity
   [nil "--equity-account EQUITY" "Equity account name. Defaults to 'equity:opening balances'"
    :default "equity:opening balances"]
   ;; Logging scheme
   [nil "--ecs-logging" "Use Elastic Common Schema logging. Useful if structured logging is required"]
   ["-v" "--verbose" "Verbosity level. May be specific multiple times to increase level"
    :default 0
    :update-fn inc]
   ["-h" "--help" "This help"]
   [nil "--version" "Display version and exit"]])

(defn- usage
  "Returns the summary as a usage"
  [options-summary]
  (let [text ["Parses:"
              "* A single account MyExpenses JSON export"
              "* A merged accounts MyExpenses JSON export"
              "* A directory with multiple single account MyExpenses JSON exports"
              "and produces an hledger compatible journal."
              options-summary]]
    (string/join \newline text)))

(defn- error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn validate-args
  "Validate the command line arguments. Return a map. In case of the program needing to exit, provide an exit message. Otherwise, the options provided"
  [args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      ;; First handle the help option
      (:help options) ; help => exit OK with usage
      {:exit-message (usage summary) :ok? true}
      ;; version output
      (:version options) ; version => exit OK with version string
      {:exit-message VERSION :ok? true}
      ;; argument/option parsing errors
      errors ; exit with errors
      {:exit-message (error-msg errors)}
      ;; Input/Output type validation
      (nil? (:input options))
      {:exit-message "Missing input path"}
      (nil? (:output options))
      {:exit-message "Missing output path"}
      ;; Everything checks out scenario
      :else
      {:options options})))

(defn hledgerize
  "Callable entry point to the application."
  [{:keys [input output equity-account]}]
  (t/info ::parsing-input
         :level :INFO
         :file input)
  ;; Coerce to string as we might get a keyword when using -X:run-x
  (let [inp (str input)
        outp (str output)
        equity (str equity-account)]
    (if (.isDirectory (io/file inp))
      ;; We are working with a directory, we need to walk it. We don't support recursive though, on purpose
      (let [jfs (-> inp io/file .listFiles)
            transactions (mapcat #(-> % slurp (load-my-expenses-json equity)) jfs)]
        (write-hledger-journal transactions outp))
      ;; Otherwise it's either a single account or a merged account file, treatment is
      ;; abstracted by called function
      (let [transactions (-> inp slurp (load-my-expenses-json equity))]
        (write-hledger-journal transactions outp)))))

(defn -main
  "Main function"
  [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (when exit-message
      (exit (if ok? 0 1) exit-message))
    ;; Setup logging
    (setup-logging (assoc options :version VERSION))
    (hledgerize options)))