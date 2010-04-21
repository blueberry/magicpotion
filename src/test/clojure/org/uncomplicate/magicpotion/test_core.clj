(ns org.uncomplicate.magicpotion.test-core
  (:use org.uncomplicate.magicpotion.core)
  (:use clojure.test))

(deftest test-create-struct-deep
         (let [concept-def {:name ::aconcept
                            :roles [{:property {:name :aproperty :restrictions[string?]} :kind :by-value}]
                            :super []}]
           (is (= :value (:aproperty (struct (create-struct-deep concept-def) :value))))))
