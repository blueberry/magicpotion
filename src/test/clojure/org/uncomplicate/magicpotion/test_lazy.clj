(ns org.uncomplicate.magicpotion.test-lazy
  (:use org.uncomplicate.magicpotion.lazy)
  (:use org.uncomplicate.magicpotion)
  (:import org.uncomplicate.magicpotion.lazy.LazyValue)
  (:import org.uncomplicate.magicpotion.lazy.LazyReference)
  (:use clojure.test)
  (:use clojure.contrib.mock)
  (:import java.lang.ref.SoftReference))

(deftest test-provide
  (let [direct (Object.)
        from-source (Object.)
        soft-reference (SoftReference. direct)
        soft-atom (atom soft-reference)
        source {1 from-source}]
    (is (= direct (provide source soft-atom 1)))
    (is (= from-source (do (.clear soft-reference) (provide source soft-atom 1))))))

(let [obj (Object.)
      source (Object.)
      soft-atom (Object.)
      id (Object.)]

  (deftest test-deref-LazyValue
    (let [lazy-value (LazyValue. source soft-atom 1)]
      (expect [provide (times 1 (has-args [source soft-atom 1] (returns obj)))]
        (is (= obj @lazy-value)))))

  (deftest test-deref-LazyReference
    (let [lazy-reference (LazyReference. source soft-atom 1)]
      (expect [provide (times 1 (has-args [source soft-atom 1] (returns (ref obj))))]
        (is (= obj @lazy-reference))))))

(deftest test-lazy
  (let [ind (individual {})]
    (is (= {} (deref (lazy ind))))))
