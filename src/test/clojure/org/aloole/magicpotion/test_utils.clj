(ns org.aloole.magicpotion.test-utils
  (:use org.aloole.magicpotion.utils)
  (:use clojure.test))

(defn aname [])

(deftest test-to-keyword
         (is (= ::aname (to-keyword aname))))