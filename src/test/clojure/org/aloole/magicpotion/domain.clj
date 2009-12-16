(ns org.aloole.magicpotion.domain
  (:use [clojure.test])
  (:use [org.aloole.magicpotion.predicates])
  (:use [org.aloole.magicpotion.core :as core])
  (:use [org.aloole.magicpotion.m3 :as m3]))

(property pname
          [string?] 
          [])

(property first-name
          [(min-length 3)]
          [pname])

(property last-name
          [(min-length 4)]
          [pname])


(property start-date
          [in-past?] 
          [])

(concept person
         [first-name
          last-name]
         [])
	
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
          [person?]
          [])

(concept social-person
         [knows]
         [person])

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
