(ns org.aloole.magicpotion.m3
  (:use org.aloole.magicpotion.utils)
  (:use [org.aloole.magicpotion.validation :as validation])
  (:use org.aloole.magicpotion.core))

(def concept-struct (create-struct :name :properties :super))

(def property-struct (create-struct :name :validators :super))

;;---------------------------------------------------------------------

(defn create-concept-def 
  [& params]
  (with-meta
    (apply struct-map concept-struct params)
    {::type ::concept}))
  
(defn concept-def 
  [v] 
  (::def (meta v)))

;;---------------------------------------------------------------------

(defn create-property-def 
  [& params]
  (with-meta
    (apply struct-map property-struct params)
    {::type ::property}))

(defn property-def 
  [v] 
  (::def (meta v)))

;;---------------------------------------------------------------------

(defmacro validators
  [v]
  `(:validators (property-def (var ~v))))

(defn create-property [property-def]
  (let [property-name (:name property-def)]
    (ref (fn [conc]
           (property-name conc))
         :meta {:type property-name
                ::def property-def
                ::hierarchy (infer-hierarchy (make-hierarchy) property-def)
                ::validation/validator (create-validator (reverse (deep :validators property-def)))})))

(defmacro property
  [name validators super]
  `(let [property-def# (create-property-def 
                         :name (to-keyword ~name)
                         :validators ~validators
                         :super (map property-def ~super))]
     (def ~name (create-property property-def#))))
;;--------------------------------------------------------------------------------------

(defn create-validators
  [property-defs]
  (let [validators (zipmap (map :name property-defs) 
                           (map #(create-validator (deep :validators %)) property-defs))]
        (reduce #(if (val %2) (assoc %1 (key %2) (val%2)) %1) {} validators)))

;;---------------------------------------------------------------------
(defn create-concept [concept-def]
  (let [concept-name (:name concept-def)
        concept-struct (create-struct-deep concept-def)
        validators (create-validators (deep :properties concept-def))
        hierarchy (infer-hierarchy (make-hierarchy) concept-def)]
  (ref (fn [& property-entries]
         (with-meta
           (apply struct-map concept-struct (validate validators property-entries))
           {:type concept-name
            ::def concept-def
            ::struct concept-struct
            ::validation/validators validators}))
       :meta {:type ::concept
              ::def concept-def
              ::hierarchy hierarchy
              ::name concept-name
              ::validation/validators validators})))

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
      (def ~name (create-concept concept-def#)))))
