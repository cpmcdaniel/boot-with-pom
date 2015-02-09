# boot-with-pom

A simple `with-pom` task that allows boot builds to get their project information
(groupId, artifactId, version), dependencies, and repositories from an existing
Maven POM.

This artifact is not yet deployed to any public JAR repositories. To use it, you
must build and install it from source. Once installed, you can use it in your own
boot scripts with the following dependency declaration:

[](dependency)
```clojure
[cpmcdaniel/boot-with-pom "0.0.1-SNAPSHOT"] ;; latest release
```
[](/dependency)

## Usage

The following examples assume you have [boot installed][installboot] and up to
date.

Note: the `with-pom` task is not compatible with the built-in `pom` task. The
two will step on each other's changes.

### Within a project

If you already have a `build.boot`, add the dependency above to `:dependencies`
and `(require '[cpmcdaniel/boot-with-pom :refer :all])`. The build.boot file
may look like the following:

```clojure
(set-env!
 :source-paths   #{"src/main/java" "src/main/clojure"}
 :resource-paths #{"src/main/resources"}
 :dependencies   '[[cpmcdaniel/boot-with-pom "0.0.1-SNAPSHOT" :scope "provided"]])

(task-options!
 aot  {:namespace     #{'net.canarymod.plugin.lang.clojure.clj-plugin}}
 uber {:exclude-scope #{"provided"}})

(require '[cpmcdaniel.boot-with-pom :refer :all])

(deftask build
   "Build my project"
   []
   (comp (with-pom) (aot) (javac) (uber) (jar) (install)))
```

The build can then be executed with `boot build` or:

```bash
boot with-pom aot javac uber jar install
```

### Specifying location of the POM

In most cases, the pom.xml will exist in the project root directory. If, for some
reason, it is in a different location or has a different name, you may pass the
`-p` or `--pom` argument like so:

```bash
boot with-pom --pom foo/bar-pom.xml jar install
```

## Acknowledgements

Thanks to the boot developers and the folks in #hoplon on FreeNode IRC for walking me through my first boot task!

## License

Copyright © 2015 Craig McDaniel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[installboot]: https://github.com/boot-clj/boot#install
