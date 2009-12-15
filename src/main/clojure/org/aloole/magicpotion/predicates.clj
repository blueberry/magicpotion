(ns org.aloole.magicpotion.predicates)

(defn atom?
  [x]
  (isa? (type x) clojure.lang.Atom))

(defn ref?
  [x]
  (isa? (type x) clojure.lang.Ref))

(defn reference?
  [x]
  (isa? (type x) clojure.lang.IRef))

(defn in-past?
  [date]
  (. (java.util.Date.) after date))
  
(defn in-future?
  [date]
  (. (java.util.Date.) before date))

(defn min-length
  [low]
  (fn [#^String s]
    (<= low (.length s))))

(defn max-length
  [high]
  (fn [#^String s]
    (>= high (.length s))))

(defn length-between
  [low high]
  (fn [#^String s]
    (let [length (.length s)]
    (and  (<= low length) (>= high length)))))
