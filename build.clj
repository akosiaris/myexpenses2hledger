(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as d]))

(def lib 'net.clojars.akosiaris/myexpenses2hledger)
(def version "0.3.0")
(def main 'akosiaris.myexpenses2hledger)
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" lib version))

(defn test
  "Run all the tests."
  [opts]
  (let [basis    (b/create-basis {:aliases [:test]})
        cmds     (b/java-command
                  {:basis     basis
                   :main      'clojure.main
                   :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn- create-opts [opts]
  (assoc opts
         :lib lib
         :version version
         :main main
         :uber-file uber-file
         :basis (b/create-basis {})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))

(defn- uberjar
  "Build the uber jar"
  [opts]
  (b/delete {:path "target"})
  (println "\nCopying source...")
  (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
  (println (str "\nCompiling " main "..."))
  (b/compile-clj opts)
  (println "\nBuilding uber JAR..." (:uber-file opts))
  (b/uber opts))

(defn ci
  "Run the CI pipeline of tests, build the uberjar and upload it"
  [opts]
  (let [opts (create-opts opts)]
    (test opts)
    (uberjar opts)
    ;; Re-create the pom.xml to appease deps-deploy who seeks it
    ;; a pom.xml in the current directory
    (b/write-pom (assoc (dissoc opts :class-dir) :target "."))
    ;; Upload uberjar
    (when (:upload opts)
      (d/deploy {:installer :remote
                 :sign-releases? false
                 :artifact uber-file})))
  opts)