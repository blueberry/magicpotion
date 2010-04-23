; ===== Iteration 2: Inheritance - reusing through specialization ============
(ns org.uncomplicate.magicpotion.examples.parties.e2
  (:use clojure.test))

(use 'org.uncomplicate.magicpotion)
(use 'org.uncomplicate.magicpotion.predicates)

; ----- General property that can be reused -----
(property aname
          [string?])

; ----- General concept with the general property
(concept party
         [aname])

(deftest test-general-concept
  (let [valid-name "Valid-aname"
        invalid-name :kw]
    (is (= {::aname nil} (party)))
    (is (= {::aname valid-name}
           (party ::aname valid-name)))
    (is (thrown? IllegalArgumentException
           (party ::aname invalid-name)))))

; ----- Specialized properties -----
(property first-name
          [(length-between 2 32)]
          [aname])

(property last-name
          [(min-length 3) (max-length 32)]
          [aname])

; ----- Specialized concepts -----
(concept person
         [first-name
          last-name]
         [party])

(concept company
         [(val> aname [(length-between 2 64)])]
         [party])

(deftest test-specialized-concepts
  (let [valid-aname "Some-random-name"
        valid-first-name "Jo"
        valid-last-name "Lee"]
    ; Test Person
    (is (= {::aname nil ::first-name nil ::last-name nil} (person)))
    (is (= {::aname valid-aname
            ::first-name nil ::last-name nil}
           (person ::aname valid-aname)))
    (is (= {::aname valid-aname
              ::first-name valid-first-name
              ::last-name valid-last-name}
           (person ::aname valid-aname
                   ::first-name valid-first-name
                   ::last-name valid-last-name)))
    (is (thrown? IllegalArgumentException
           (person ::aname :A-keyword-is-not-a-string)))
    (is (thrown? IllegalArgumentException
           (person ::first-name "A")))
    ; Test Company
    (is (= {::aname nil} (company)))
    (is (= {::aname valid-aname}
           (company ::aname valid-aname)))
    (is (thrown? IllegalArgumentException
           (company ::aname "C")))
    (is (thrown? IllegalArgumentException
           (company ::aname :A-keyword-is-not-a-string)))))
