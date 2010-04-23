(use 'clojure.test)

(set! *warn-on-reflection* true)

(def spaces
  ['org.uncomplicate.magicpotion.test-utils
   'org.uncomplicate.magicpotion.test-validation
   'org.uncomplicate.magicpotion.test-core
   'org.uncomplicate.magicpotion.test-predicates
   'org.uncomplicate.magicpotion.test-m3
   'org.uncomplicate.magicpotion.examples.domain])

(println "Loading code ...")
(time (apply use spaces))

(println "Executing tests ...")

(doseq [ns (map find-ns spaces)]
  (time (run-tests ns)))
