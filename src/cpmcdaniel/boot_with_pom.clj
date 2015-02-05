(ns cpmcdaniel.boot-with-pom
  {:boot/export-tasks true}
  (:require
   [boot.pod :as pod]
   [boot.util :as util]
   [boot.core :as core :refer :all]
   [clojure.java.io :as io]))

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
        jar-file    (format "%s-%s.jar" (name project-symbol) version)
        tmp         (temp-dir!)]

    (set-env! :dependencies #(apply conj % deps)
              :repositories #(apply conj % repos))

    (with-pre-wrap fileset
      (let [[gid aid]          (util/extract-ids project-symbol)
            pomdir             (io/file tmp "META-INF" "maven" gid aid)
            boot-generated-pom (io/file pomdir "pom.xml.generated")
            pom-xml            (io/file pomdir "pom.xml")
            propfile           (io/file pomdir "pom.properties")]
        ;; Copied from the boot (pom) task, but with an altered pom path so it doesn't
        ;; "step on" the user-defined one.
        (util/info "Writing %s and %s...\n" (.getName boot-generated-pom) (.getName propfile))
        (pod/with-call-worker
          (boot.pom/spit-pom! ~(.getPath boot-generated-pom) ~(.getPath propfile)
                              ~{:project project-symbol :version version}))
        (util/info "Writing %s...\n" (.getName pom-xml))
        (spit pom-xml (slurp original-pom))
        (-> fileset (add-resource tmp) (commit!))))))
