(ns org.aloole.magicpotion
  (:use org.aloole.magicpotion.utils)
  (:use [org.aloole.magicpotion.validation :as validation])
  (:use org.aloole.magicpotion.core)
  (:use [org.aloole.magicpotion.m3 :as m3]))
  
(defn by-ref [prop cardinality]
  (with-meta (property-def prop) {:link-type :by-reference,
                   :cardinality cardinality}))

(defn by-val [prop cardinality]
  (with-meta (property-def prop) {:link-type :by-value,
                   :cardinality cardinality}))

(defmacro property
  [name validators super]
  `(let [property-def# (create-property-def 
                         :name (to-keyword ~name)
                         :validators ~validators
                         :super (map property-def ~super))]
     (def ~name (create-property property-def#))))

(defmacro concept
  ([name]
   (concept name nil))
  ([name properties]
   (concept name properties nil))
  ([name properties super]
   `(let [name-keyword# (to-keyword ~name)
          concept-def# (create-concept-def
                         :name (to-keyword ~name)
                         :properties (map property-def ~properties) 
                         :super (map concept-def ~super))]
          ;;validators# (create-validator (deep :validators property-def))
          ;;validators# (create-validators (deep :properties concept-def#))]
      (do
        (def ~(suf-symbol name "?") (is-instance name-keyword#))
        (def ~name (create-concept concept-def#))))))
        
