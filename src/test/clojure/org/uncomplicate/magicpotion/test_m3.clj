(ns org.uncomplicate.magicpotion.test-m3
  (:use org.uncomplicate.magicpotion.m3)
  (:use clojure.test))

(deftest test-inherit-restrictions
         (let [role-defs [{:property {:name :aproperty :restrictions []} :restrictions [string?] :kind :by-ref}
													{:property {:name :aproperty :restrictions []} :restrictions [number?] :kind :by-value}]
							 processed-role-defs [{:property {:name :aproperty :restrictions []} :restrictions [string? number?] :kind :by-value}]]
           (is (= processed-role-defs (inherit-restrictions role-defs)))))