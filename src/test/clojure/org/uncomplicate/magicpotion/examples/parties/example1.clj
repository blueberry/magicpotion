; ===== Iteration 1: Concepts, Properties, Individuals =====
(ns org.uncomplicate.magicpotion.examples.parties.example1
  (:use clojure.test))

; ----- Including Magic Potion -----
(use 'org.uncomplicate.magicpotion)
(use 'org.uncomplicate.magicpotion.predicates)

; ----- Concepts and properties: describing general things -----
(property first-name
  [string?
   (length-between 2 32)])

(concept person
  [first-name])

; ----- Individuals: stating the facts about concrete things -----
; ----- Value Statements about individuals -----
(deftest test-person
  (is (= {::first-name nil} (person))) ; open world - we don't know the name
  (is (= {::first-name "Jenna"} (person ::first-name "Jenna")))
  (is (thrown? IllegalArgumentException (person :A-keyword-is-not-a-string))))

; ----- Identity of individuals -----
(deftest test-bindings
  (let [jenna (person ::first-name "Jenna")]
    (is (= {::first-name "Jenna"} jenna)))
  (let [ref-jenna (ref (person ::first-name "Jenna"))]
    (is (= {::first-name "Jenna"} @ref-jenna))))
