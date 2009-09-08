(use 'clojure.test)

(set! *warn-on-reflection* true)

(def spaces
  ['org.aloole.magicpotion.test-utils
   'org.aloole.magicpotion.test-validation
   'org.aloole.magicpotion.test-core
   'org.aloole.magicpotion.test-m3
   'org.aloole.magicpotion.domain])

(println "Loading code ...")
(time (apply use spaces))
  
(println "Executing tests ...")

(doseq [ns (map find-ns spaces)]
  (time (run-tests ns)))
