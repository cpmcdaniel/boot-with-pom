(ns cpmcdaniel.boot-with-pom
  {:boot/export-tasks true}
  (:require
   [boot.pod :as pod]
   [boot.util :as util]
   [boot.core :as core :refer :all]
   [clojure.java.io :as io])
  (:import (org.apache.maven.model.io.xpp3 MavenXpp3Reader)))

(deftask with-pom
  "Uses an existing pom.xml to initialize the environment with dependencies,
  repositories, artifactId, groupId, and version."

  [p pom VAL str "The location of the pom.xml file"]

  (let [model       (.read (MavenXpp3Reader.) (io/reader (io/file "pom.xml")))
        repos       (map #(vector (.getName %) (.getUrl %)) (.getRepositories model))
        dep-sym     (fn [dep] (if (= (.getArtifactId dep) (.getGroupId dep))
                               (symbol (.getArtifactId dep))
                               (symbol (.getGroupId dep) (.getArtifactId dep))))
        convert-dep (fn [dep] [(dep-sym dep) (.getVersion dep) :scope (or (.getScope dep) "compile")])
        deps        (map convert-dep (.getDependencies model))
        project-sym (symbol (.getGroupId model) (.getArtifactId model))
        jar-file    (format "%s-%s.jar" (name project-sym) (.getVersion model))
        tmp         (temp-dir!)]

    ;; Couldn't get this to work when placed inside the (with-pre-wrap)
    (set-env! :dependencies #(apply conj % deps)
              :repositories #(apply conj % repos))

    (with-pre-wrap fileset
      (let [[gid aid]          (util/extract-ids project-sym)
            pomdir             (io/file tmp "META-INF" "maven" gid aid)
            boot-generated-pom (io/file pomdir "pom.xml.generated")
            pom-xml            (io/file pomdir "pom.xml")
            propfile           (io/file pomdir "pom.properties")]
        ;; Copied from the boot (pom) task, but with an altered pom path so it doesn't
        ;; "step on" the user-defined one.
        (util/info "Writing %s and %s...\n" (.getName boot-generated-pom) (.getName propfile))
        (pod/with-call-worker
          (boot.pom/spit-pom! ~(.getPath boot-generated-pom) ~(.getPath propfile)
                              ~{:project project-sym :version (.getVersion model)}))
        (util/info "Writing %s...\n" (.getName pom-xml))
        (spit pom-xml (slurp "pom.xml"))
        (-> fileset (add-resource tmp) (commit!))))))
