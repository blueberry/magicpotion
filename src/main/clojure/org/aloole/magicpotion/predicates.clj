(ns org.aloole.magicpotion.predicates)

(defn in-past?
  [date]
  (. (java.util.Date.) after date))
  
(defn in-future?
  [date]
  (. (java.util.Date.) before date))

(defn min-length?
  [low]
  (fn [#^String s]
    (<= low (.length s))))

(defn max-length?
  [high]
  (fn [#^String s]
    (>= high (.length s))))

(defn length-between?
  [low high]
  (fn [#^String s]
    (let [length (.length s)]
    (and  (<= low length) (>= high length)))))
