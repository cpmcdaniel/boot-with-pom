(ns cpmcdaniel.boot-with-pom.impl
  (:require [clojure.java.io :as io])
  (:import (org.apache.maven.model.io.xpp3 MavenXpp3Reader)))


(defn deps
  "Extract dependencies from a Maven model"
  [model]
  (letfn [(depsym [dep]
            (if (= (.getArtifactId dep) (.getGroupId dep))
              (symbol (.getArtifactId dep))
              (symbol (.getGroupId dep) (.getArtifactId dep))))
          (convert-dep [dep]
            [(depsym dep) (.getVersion dep) :scope (or (.getScope dep) "compile")])]
    (map convert-dep (.getDependencies model))))

(defn repos
  "Extract repositories from a Maven model"
  [model]
  (map #(vector (.getName %) (.getUrl %)) (.getRepositories model)))

(defn extract-from-pom
  "Extract project groupId/artifactId/version, dependencies and repositories
  from the given pom.xml file"
  [pom]
  (let [model (.read (MavenXpp3Reader.) (io/reader (io/file (or pom "pom.xml"))))]
    {:deps (deps model)
     :repos (repos model)
     :project-symbol (symbol (.getGroupId model) (.getArtifactId model))
     :version (.getVersion model)}))
