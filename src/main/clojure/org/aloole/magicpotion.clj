(ns org.aloole.magicpotion
  (:use clojure.contrib.generic.functor)
  (:use org.aloole.magicpotion.utils)
  (:use [org.aloole.magicpotion.validation :as validation])
  (:use org.aloole.magicpotion.core)
  (:use [org.aloole.magicpotion.m3 :as m3]))
  
(defn ref> 
  ([prop]
   (ref> prop nil))
  ([prop restrictions]
  {:pre [(property? prop)]}
  (create-role-def (property-def prop) 
                   :by-reference
                   restrictions)))

(defn ref*> 
  ([prop]
   (ref*> prop nil))
  ([prop restrictions]
   (ref*> prop restrictions nil))
  ([prop restrictions set-restrictions]
  {:pre [(property? prop)]}
  (create-many-role-def (property-def prop)
                        :by-reference
                        restrictions
                        set-restrictions)))

(defn val> 
  ([prop]
   (val> prop nil))
  ([prop restrictions]
  {:pre [(property? prop)]}
  (create-role-def (property-def prop)
                   :by-value
                   restrictions)))

(defn val*> 
  ([prop ]
   (val*> prop nil))
  ([prop restrictions]
   (val*> prop restrictions nil))
  ([prop restrictions set-restrictions]
  {:pre [(property? prop)]}
  (create-many-role-def (property-def prop)
                        :by-value
                        restrictions
                        set-restrictions)))

(defmacro property
  [name & params]
  {:pre [(every? (partial contains? #{:restrictions :super}) (filter keyword? params))]}
  (let [pos (take-while (comp not keyword?) params)
        kw-map (apply hash-map (drop-while (comp not keyword?) params))
        restrictions (if-let [r (first pos)] r (:restrictions kw-map))
        super (if-let [s (second pos)] s (:super kw-map))]
    `(let [property-def# (create-property-def 
                          (to-keyword ~name)
                          ~restrictions
                          (map property-def ~super))]
     (def ~name (create-property property-def#)))))

(defn sanitize-roles [raw-roles]
  (map #(if (property? %) (val> %) %) raw-roles))

(defmacro concept
  ([name & params]
  {:pre [(every? (partial contains? #{:roles :super}) (filter keyword? params))]}
  (let [pos (take-while (comp not keyword?) params)
        kw-map (apply hash-map (drop-while (comp not keyword?) params))
        roles (if-let [r (first pos)] r (:roles kw-map))
        super (if-let [s (second pos)] s (:super kw-map))]
   `(let [name-keyword# (to-keyword ~name)
          concept-def# (create-concept-def
                         name-keyword#
                         (sanitize-roles ~roles)
                         (map concept-def ~super))]
      (do
        (def ~(suf-symbol name "?") (is-instance name-keyword#))
        (def ~name (create-concept concept-def#)))))))
