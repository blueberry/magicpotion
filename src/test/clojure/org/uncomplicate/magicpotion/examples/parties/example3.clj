; ===== Iteration 3: Relationships - connections between things ============
(ns org.uncomplicate.magicpotion.examples.parties.example3
  (:use clojure.test))

(use 'org.uncomplicate.magicpotion)
(use 'org.uncomplicate.magicpotion.predicates)

(property aname
         [string?])

(property street-name
         [(length-between 1 128)]
         [aname])

(property house-number
         [integer?
          (between 1 100000)])

(property city-name
         [(length-between 1 32)]
         [aname])

(concept location
         [street-name
          house-number
          city-name])

(property address
          [location?])

(concept party
         [aname
          (ref> address)])

(deftest test-reference
  (let [valid-name "Valid-aname"
        address-value (location ::street-name "5th Street"
                               ::house-number 50
                               ::city-name "South Park")
        address-individual (ref address-value)]
    (is (thrown? IllegalArgumentException
          (party ::address address-value)))
    (is (= {::aname valid-name ::address address-individual}
           (party ::aname valid-name ::address address-individual)))
    (is (thrown? IllegalArgumentException
          (party ::address (ref (address ::street-name "")))))))

(property first-name
          [(length-between 2 32)]
          [aname])

(property last-name
          [(min-length 3) (max-length 32)]
          [aname])

(concept person
         [first-name
          last-name]
         [party])

(concept company
         [(val> aname [(length-between 2 64)])
          (ref*> address)]
         [party])

(deftest test-reference
  (let [valid-name "Valid-name"
        address-value-1 (location ::street-name "5th Street"
                                   ::house-number 50
                                   ::city-name "South Park")
        address-value-2 (location ::street-name "7th Street"
                                   ::house-number 22
                                   ::city-name "Springfield")
        address-individual-1 (ref address-value-1)
        address-individual-2 (ref address-value-2)
        invalid-addresses #{address-value-1 address-value-2}
        valid-addresses #{address-individual-1 address-individual-2}]
    (is (thrown? RuntimeException
          (company ::address address-value-1)))
    (is (thrown? RuntimeException
           (company ::address address-individual-1)))
    (is (thrown? RuntimeException
           (company ::address invalid-addresses)))
    (is (= {::aname valid-name ::address valid-addresses}
           (company ::aname valid-name ::address valid-addresses)))))