(ns org.uncomplicate.magicpotion.test-utils
  (:use org.uncomplicate.magicpotion.utils)
  (:use clojure.test))

(comment
(defn aname [])

(deftest test-to-keyword
         (is (= ::aname (to-keyword aname))))
)