(ns org.aloole.magicpotion.test-predicates
  (:use clojure.test)
  (:use org.aloole.magicpotion.predicates))

(deftest test-ref?
         (is (ref? (ref :val)))
         (is (not (ref? (atom :val))))
         (is (not (reference? nil))))

(deftest test-atom?
         (is (atom? (atom :val)))
         (is (not (atom? (ref :val))))
         (is (not (reference? nil))))

(deftest test-reference?
         (is (reference? (ref :val)))
         (is (reference? (atom :val)))
         (is (not (reference? :val)))
         (is (not (reference? nil))))
