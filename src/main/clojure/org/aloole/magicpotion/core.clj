(ns org.aloole.magicpotion.core)

(defn deep
  "Applies f to all the ancestors of the concept conc. 
  The hierarchy is traversed through the keyword :super.
  typical usage: (deep :properties x)" 
  [f conc]
  (mapcat f (tree-seq :super :super conc)))

(defn create-struct-deep
  [conc-def]
  (apply create-struct (map :name (deep :properties conc-def))))
