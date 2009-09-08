(ns org.aloole.magicpotion.predicates)

(defn in-past?
  [date]
  (. (java.util.Date.) after date))
  
(defn in-future?
  [date]
  (. (java.util.Date.) before date))
