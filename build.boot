(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.6.0" :scope "provided"]
                 [boot/core "2.0.0-rc8" :scope "provided"]
                 [adzerk/bootlaces "0.1.9" :scope "test"]
                 [adzerk/boot-test "1.0.3" :scope "test"]
                 [org.apache.maven/maven-model "3.2.5" :scope "provided"]])

(require
 '[adzerk.bootlaces :refer :all]
 '[adzerk.boot-test :refer :all]
 '[cpmcdaniel.boot-with-pom :refer :all])

(def +version+ "0.0.1")

(bootlaces! +version+)

(task-options!
 pom {:project 'cpmcdaniel/boot-with-pom
      :version +version+
      :description "Boot task to use an existing pom.xml"
      :url "https://github.com/cpmcdaniel/boot-with-pom"
      :scm {:url "https://github.com/cpmcdaniel/boot-with-pom"}
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
