(ns cpmcdaniel.boot-with-pom
  {:boot/export-tasks true}
  (:require
   [boot.pod :as pod]
   [boot.util :as util]
   [boot.core :as core :refer :all]
   [clojure.java.io :as io])
  (:import [java.util Properties]))

(deftask with-pom
  "Uses an existing pom.xml to initialize the environment with dependencies,
  repositories, artifactId, groupId, and version."

  [p pom VAL str "The location of the pom.xml file"]

  (let [original-pom (or pom "pom.xml")
        worker       (pod/make-pod (update-in (get-env) [:dependencies]
                                             into '[[org.apache.maven/maven-model "3.2.5" :scope "provided"]]))
        {:keys [deps repos project-symbol version]}
        (pod/with-eval-in worker
          (require '[cpmcdaniel.boot-with-pom.impl :as impl])
          (impl/extract-from-pom ~original-pom))
        tmp         (temp-dir!)]

    (set-env! :dependencies #(apply conj % deps)
              :repositories #(apply conj % repos))

    (with-pre-wrap fileset
      (let [[gid aid]          (util/extract-ids project-symbol)
            props              (doto (Properties.)
                                 (.setProperty "groupId" gid)
                                 (.setProperty "artifactId" aid)
                                 (.setProperty "version" version))
            pomdir             (io/file tmp "META-INF" "maven" gid aid)
            pom-xml            (doto (io/file pomdir "pom.xml") io/make-parents)
            propfile           (doto (io/file pomdir "pom.properties") io/make-parents)]
        (util/info "Writing %s and %s...\n" (.getName pom-xml) (.getName propfile))
        (with-open [ostream (io/output-stream propfile)]
          (.store props ostream (str gid "/" aid " " version " property file")))
        (spit pom-xml (slurp original-pom))
        (-> fileset (add-resource tmp) (commit!))))))
