(ns org.uncomplicate.magicpotion.domain
  (:use [clojure.test])
  (:use [org.uncomplicate.magicpotion.predicates])
  (:use [org.uncomplicate.magicpotion.core :as core])
  (:use [org.uncomplicate.magicpotion.m3 :as m3])
  (:use [org.uncomplicate.magicpotion :as mp]))

; Test Domain Model

(property pname
          [string?])

(property first-name
          [(min-length 3)]
          [pname])

(property last-name
          [(min-length 4)]
          [pname])

(property start-date
          [in-past?])

(concept person
         [first-name
          last-name])
	
(concept professor
         [start-date]
         [person])

(property transcedental-property
          []
          [last-name])

(concept transcedental-being
         [transcedental-property]
         [professor])

(property knows
          [person?])

(property loves
          [person?])

(concept social-person
         [(ref> knows)
          (ref*> loves)]
         [person])

(concept social-person-by-val
         [(val> knows)
          (val*> loves)]
         [person])

(concept party
         [pname])

(property cname [string?])

(property company-name
				 [(min-length 2)]
				 [pname cname])

(concept company
         [(val> pname [(min-length 3)])
					(val> cname [#(= (first %) \A)])
					(val> company-name [(max-length 6)])]
         [party]
				 [::company-name])

;; Integration Tests

(deftest test-concept-inheritance
         (is (= {::first-name nil, ::last-name nil} (person)))
         (is (= {::first-name nil, ::last-name nil ::start-date nil} (professor))))

(deftest test-concept-validator-inheritance
         (is (thrown? IllegalArgumentException  (professor ::first-name "jo")))
         (is (= "Joe" (::first-name (professor ::first-name "Joe"))))
         (is (thrown? IllegalArgumentException  (professor ::first-name 15))))

(deftest test-concept-validator
         (is (= {::first-name "Pera", ::last-name nil ::start-date nil} (professor ::first-name "Pera")))
         (is (= {::first-name nil, ::last-name "Peric" ::start-date nil} (professor ::last-name "Peric")))
         (is (= {::first-name nil, ::last-name nil ::start-date  
                 (. (java.util.GregorianCalendar. 2000 01 04) getTime)} 
                (professor ::start-date (. (java.util.GregorianCalendar. 2000 01 04) getTime))))
         (is (= {::first-name "Pera", ::last-name "Peric" 
                 ::start-date (. (java.util.GregorianCalendar. 2000 01 04) getTime)} 
                (professor ::first-name "Pera" ::last-name "Peric" 
                           ::start-date (. (java.util.GregorianCalendar. 2000 01 04) getTime) )))
         (is (thrown? IllegalArgumentException  (professor ::first-name 15 
                     ::start-date (. (java.util.GregorianCalendar. 2000 01 04) getTime))))
         (is (thrown? IllegalArgumentException  (professor ::first-name "pera" 
                     ::start-date (. (java.util.GregorianCalendar. 3000 01 04) getTime)))))

(deftest test-hierarchy
         (is (= {:parents {}, :descendants {}, :ancestors {}} 
                (::m3/hierarchy (meta pname))))
         (is (= {:parents {::first-name #{::pname}}, 
                 :descendants {::pname #{::first-name}}, 
                 :ancestors {::first-name #{::pname}}} 
                (::m3/hierarchy (meta first-name))))
         (is (= {:parents {::last-name #{::pname}}, 
                 :descendants {::pname #{::last-name}}, 
                 :ancestors {::last-name #{::pname}}} 
                (::m3/hierarchy (meta last-name))))
         (is (= {:parents {}, :descendants {}, :ancestors {}} 
                (::m3/hierarchy (meta person))))
         (is (= {:parents {::professor #{::person}}, 
                 :descendants {::person #{::professor}}, 
                 :ancestors {::professor #{::person}}} 
                (::m3/hierarchy (meta professor))))
         (is (= {:parents {::last-name #{::pname}, ::transcedental-property #{::last-name}}, 
                 :descendants {::pname #{::last-name, ::transcedental-property}, 
                               ::last-name #{::transcedental-property}}, 
                 :ancestors {::last-name #{::pname} ::transcedental-property #{::pname, ::last-name}}} 
                (::m3/hierarchy (meta transcedental-property))))
         (is (= {:parents {::professor #{::person}, ::transcedental-being #{::professor}}, 
                 :ancestors {::professor #{::person}, ::transcedental-being #{::professor ::person}}, 
                 :descendants {::person #{::professor ::transcedental-being}, ::professor #{::transcedental-being}}}
                (::m3/hierarchy (meta transcedental-being)))))

(deftest test-concept-predicate
         (is (person? (person)))
         (is (not (social-person? (person))))
         (is (person? (social-person))))

(deftest test-ref>
         (is (social-person ::knows (atom (person))))
         (is (thrown? IllegalArgumentException (social-person ::knows (person)))))

(deftest test-ref*>
         (is (social-person ::loves #{(atom (person))}))
         (is (thrown? Exception (social-person ::loves (atom (person)))))
         (is (thrown? Exception (social-person ::loves #{(person)}))))

(deftest test-val>
         (is (social-person-by-val ::knows (person)))
         (is (thrown? IllegalArgumentException (social-person-by-val ::knows (atom person))))
         (is (company? (company ::pname "A name" ::company-name "A123"))))

(deftest test-val*>
         (is (social-person-by-val ::loves #{(person)}))
         (is (thrown? Exception (social-person-by-val ::loves (person))))
         (is (thrown? Exception (social-person-by-val ::loves #{(atom (person))}))))

(deftest test-property
				 (is (= ::knows (knows)))
				 (is (= "some random data" (knows {::knows "some random data"}))))

(deftest test-role-inheritance
         (is (company? (company ::pname "A name" ::company-name "A123")))
         (is (company? (company ::company-name "A name")))
         (is (thrown? IllegalArgumentException (company ::pname 1 ::company-name "A123")))
         (is (thrown? IllegalArgumentException (company ::pname "A" ::company-name "A123")))
         (is (thrown? IllegalArgumentException (company ::company-name "A1")))
         (is (thrown? IllegalArgumentException (company ::company-name "A123456")))
				 (is (thrown? IllegalArgumentException (company ::company-name "B123"))))

(deftest test-concept-restrictions
         (is (thrown? IllegalArgumentException (company))))
