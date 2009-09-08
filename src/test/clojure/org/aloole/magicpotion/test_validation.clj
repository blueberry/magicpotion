(ns org.aloole.magicpotion.test-validation
  (:use [org.aloole.magicpotion.validation :as mp])
  (:use clojure.test))

(deftest test-create-validator
         (is (fn? (create-validator string?)))
         (is (thrown? Exception (create-validator nil)))
         (is (thrown? Exception (create-validator "a")))
         (is (thrown? Exception (create-validator :a)))
         (is (thrown? Exception (create-validator [])))
         (is (fn? (create-validator [#(and (not (string? %)) (not (keyword? %)))])))
         (is (fn? (create-validator [string?])))
         (is (fn? (create-validator [string? fn?])))
         (is (thrown? Exception (create-validator [string? :a]))))

(deftest test-validator
         (let [validator (create-validator string?)]
           (is (thrown? IllegalArgumentException (validator)))
           (is (false? (validator "some string")))
           (is (= (list string?) (validator :a)))
           (is (= (list string?) (validator nil))))
         (let [validator (create-validator #(string? %))]
           (is (false? (validator "some string"))))
         (let [not-string? #(not (string? %))
               validator (create-validator not-string? keyword?)]
           (is (false? (validator :a)))
           (is (= (list not-string? keyword?) (validator "some string")))))
        
(deftest test-violations
         (is (thrown? IllegalArgumentException (violations nil nil)))
         (is (thrown? IllegalArgumentException (violations nil {:p1 "a"})))
         (is (= {:p1 (list string?)} 
                (violations {:p1 (create-validator string?)} {:p1 nil})))
         (is (false? (violations (create-validator string?) "a")))
         (is (false? (violations (create-validator string? #(not (nil? %))) "a")))
         (is (= {"a" (list keyword?)} 
                (violations (create-validator keyword?) "a")))
         (is (= {nil (list keyword? string?)} 
                (violations (create-validator keyword? string?) nil)))
         (let [validators {:p1 (create-validator string?) :p2 (create-validator keyword?)}]
           (is (= {:p1 (list string?) :p2 (list keyword?)} 
                  (violations validators {:p1 nil :p2 nil})))
           (is (= {:p1 (list string?) :p2 (list keyword?)} 
                  (violations validators {:p1 :a :p2 "a"})))
           (is (= {:p1 (list string?)} 
                  (violations validators {:p1 :a :p2 :a})))
           (is (= {:p2 (list keyword?)} 
                  (violations validators {:p1 "a" :p2 "a"})))
           (is (false? (violations validators {:p1 "a" :p2 :a})))))

(deftest test-valid?
         (is (valid? nil))
         (is (valid? nil nil))
         (is (valid? {}))
         (is (valid? nil {}))
         (is (valid? {:a :b :c :d}))
         (is (valid? nil {:a :b :c :d}))
         (let [validators {:p1 (create-validator string?)}
               validators-meta {::mp/validators validators}]
           (is (valid? validators {:p1 "a"}))
           (is (valid? (with-meta {:p1 "a"} validators-meta)))
           (is (valid? validators [:p1 "a"]))
           (is (false? (valid? validators {:p1 :a})))
           (is (false? (valid? validators [:p1 :a])))
           (is (false? (valid? (with-meta {:p1 :a} validators-meta))))))

(deftest test-validate
         (is (nil? (validate nil)))
         (is (nil? (validate nil nil)))
         (is (= {} (validate {})))
         (is (= {} (validate nil {})))
         (let [some-map {:a :b :c :d}]
           (is (= some-map (validate some-map)))
           (is (= some-map (validate nil some-map))))
         (let [validators {:p1 (create-validator string?)}
               validators-meta {::mp/validators validators}]
           (is (= {:p1 "a"} 
                  (validate validators {:p1 "a"})))
           (is (= [:p1 "a"] 
                  (validate validators [:p1 "a"])))
           (is (= {:p1 "a"} 
                  (validate (with-meta {:p1 "a"} validators-meta))))
           (is (thrown? IllegalArgumentException 
                        (validate validators {:p1 :a})))
           (is (thrown? IllegalArgumentException 
                        (validate validators [:p1 :a])))
           (is (thrown? IllegalArgumentException 
                        (validate (with-meta {:p1 :a} validators-meta))))))
