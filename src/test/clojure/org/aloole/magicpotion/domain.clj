(ns org.aloole.magicpotion.domain
  (:use [clojure.test])
  (:use [org.aloole.magicpotion.predicates])
  (:use [org.aloole.magicpotion.core :as core])
  (:use [org.aloole.magicpotion.m3 :as m3]))

(property pname
          [string?] 
          [])

(property first-name
          [string? #(< 2 (.length %))]
          [pname])

(property start-date
          [in-past?] 
          [])

(property teach
          [] 
          [])

(concept person
         [first-name]
         [])
	
(concept professor
         [start-date]
         [person])

(deftest test-property-inheritance
         (is (thrown? IllegalArgumentException  (professor ::first-name "jo")))
         (is (= "Joe" (::first-name (professor ::first-name "Joe"))))
         (is (thrown? IllegalArgumentException  (professor ::first-name 15)))
         )

(deftest test-concept
         (is (thrown? IllegalArgumentException  (professor ::first-name 15 
                     ::start-date (. (java.util.GregorianCalendar. 2000 01 04) getTime))))
         (is (thrown? IllegalArgumentException  (professor ::first-name "pera" 
                     ::start-date (. (java.util.GregorianCalendar. 3000 01 04) getTime)))) 
         (is (map? (professor ::first-name "pera"))))
