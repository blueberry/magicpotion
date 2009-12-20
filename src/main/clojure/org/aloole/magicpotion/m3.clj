(ns org.aloole.magicpotion.m3
  (:use org.aloole.magicpotion.utils)
  (:use org.aloole.magicpotion.predicates)
  (:use [org.aloole.magicpotion.validation :as validation])
  (:use org.aloole.magicpotion.core))

;;-------------------- Bootstrap -------------------------------------
(def concept-struct (create-struct :name :properties :super))

(def property-struct (create-struct :name :validators :super))

(defn create-concept-def 
  [& params]
  (with-meta
    (apply struct-map concept-struct params)
    {:type ::m3-concept}))
  
(defn concept-def 
  [v] 
  (::def (meta v)))

(defn create-property-def 
  [& params]
  (with-meta
    (apply struct-map property-struct params)
    {:type ::m3-property}))

(defn m3-property? [x]
  (isa? (type x) ::m3-property))

(defn property? [x]
  (isa? (type x) ::property))

(defn property-def
  [prop]
  {:post [(m3-property? %)]}
  (if (property? prop)
    (::def (meta prop))
    prop))

;;---------------------------------------------------------------------
(defmulti create-validator (fn [x] 
                             (let [metadata (meta x)] 
                               [(:link-type metadata) (:cardinality metadata)])))

(defmethod create-validator [nil nil]
  [property-def]
    (create-val-validator (reverse (deep :validators property-def))))

(defmethod create-validator [:by-value :1]
  [property-def]
    (create-val-validator (reverse (deep :validators property-def))))

(defmethod create-validator [:by-reference :1]
  [property-def]
    (create-ref-validator (seq (concat (reverse (deep :validators property-def)) 
                                  (seq (:validators (meta property-def)))))))

(defmethod create-validator [:by-reference :*]
  [property-def]
  (let [unqualified-validator (create-multi-ref-validator 
                                (concat (reverse (deep :validators property-def))
                                        (seq (:validators (meta property-def)))))]
    (if-let [qualified-validators (seq (:set-validators (meta property-def)))]
      (let  [qualified-validator (create-val-validator qualified-validators)]
        (fn [& args]
          (seq (concat (apply qualified-validator args) (apply unqualified-validator args)))))
      unqualified-validator)))


;;---------------------------------------------------------------------
(defmacro validators
  [v]
  `(:validators (property-def (var ~v))))

(defn create-property 
  [property-def]
  {:pre [(m3-property? property-def)]}
  (let [property-name (:name property-def)]
    (ref (fn [conc]
           (property-name conc))
         :meta {:type ::property
                ::def property-def
                ::hierarchy (infer-hierarchy (make-hierarchy) property-def)
                ::validation/validator (create-validator property-def)})))

;;--------------------------------------------------------------------------------------

(defn create-validators
  [property-defs]
  (let [validators (zipmap (map :name property-defs) 
                           (map create-validator property-defs))] 
        (reduce #(if (val %2) (assoc %1 (key %2) (val%2)) %1) {} validators)))

;;---------------------------------------------------------------------
(defn create-concept [concept-def]
  (let [concept-name (:name concept-def)
        concept-struct (create-struct-deep concept-def)
        validators (create-validators (deep :properties concept-def))
        hierarchy (infer-hierarchy (make-hierarchy) concept-def)
        instance-metadata {:type concept-name
                           ::def concept-def
                           ::hierarchy hierarchy
                           ::struct concept-struct
                           ::validation/validators validators}]
    (ref (fn [& property-entries]
           (with-meta
             (apply struct-map concept-struct (validate validators property-entries))
             instance-metadata))
       :meta {:type ::concept
              ::def concept-def
              ::hierarchy hierarchy
              ::name concept-name
              ::validation/validators validators})))


(defn concept? [x]
  (isa? (type x) ::concept))
;;  ----------------- Predicates -------------------------------------
(defn is-instance
  [t]
  (let [concept-name (if (keyword? t) t (::name (meta t)))]
    (assert concept-name)
    (fn [instance]
      (if-let [hierarchy (::hierarchy (meta instance))]
              (isa? (::hierarchy (meta instance)) (type instance) concept-name)
              false))))

