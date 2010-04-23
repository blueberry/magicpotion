; ===== Iteration 2: Inheritance - reusing through specialization ============
(ns org.uncomplicate.magicpotion.examples.parties.e2
  (:use clojure.test))

; ----- Including Magic Potion -----
(use 'org.uncomplicate.magicpotion)
(use 'org.uncomplicate.magicpotion.predicates)

; ----- General property that can be reused -----
(property generic-name
          [string?])

; ----- General concept with the general property
(concept party
         [generic-name])

(deftest test-general-concept
  (let [valid-name "Valid-generic-name"
        invalid-name :kw]
    (is (= {::generic-name nil} (party)))
    (is (= {::generic-name valid-name}
           (party ::generic-name valid-name)))
    (is (thrown? IllegalArgumentException
           (party ::generic-name invalid-name)))))

; ----- Specialized properties -----
(property first-name
          [(length-between 2 32)]
          [generic-name])

(property last-name
          [(min-length 3) (max-length 32)]
          [generic-name])

; ----- Specialized concepts -----
(concept person
         [first-name
          last-name]
         [party])

(concept company
         [(val> generic-name [(length-between 2 64)])]
         [party])

(deftest test-specialized-concepts
  (let [valid-generic-name "Some-random-name"
        valid-first-name "Jo"
        valid-last-name "Lee"]
    ; Test Person
    (is (= {::generic-name nil ::first-name nil ::last-name nil} (person)))
    (is (= {::generic-name valid-generic-name
            ::first-name nil ::last-name nil}
           (person ::generic-name valid-generic-name)))
    (is (= {::generic-name valid-generic-name
              ::first-name valid-first-name
              ::last-name valid-last-name}
           (person ::generic-name valid-generic-name
                   ::first-name valid-first-name
                   ::last-name valid-last-name)))
    (is (thrown? IllegalArgumentException
           (person ::generic-name :A-keyword-is-not-a-string)))
    (is (thrown? IllegalArgumentException
           (person ::first-name "A")))
    ; Test Company
    (is (= {::generic-name nil} (company)))
    (is (= {::generic-name valid-generic-name}
           (company ::generic-name valid-generic-name)))
    (is (thrown? IllegalArgumentException
           (company ::generic-name "C")))
    (is (thrown? IllegalArgumentException
           (company ::generic-name :A-keyword-is-not-a-string)))))
