(ns org.aloole.magicpotion.m3
  (:use org.aloole.magicpotion.utils)
  (:use org.aloole.magicpotion.predicates)
  (:use [org.aloole.magicpotion.validation :as validation])
  (:use org.aloole.magicpotion.core))

;;-------------------- Bootstrap -------------------------------------
(def concept-struct (create-struct :name :roles :super))

(def property-struct (create-struct :name :restrictions :super))

(def role-struct (create-struct :property :kind :restrictions))
(def many-role-struct (create-struct :property :kind :restrictions :set-restrictions))

(defn create-concept-def 
  [pname properties super]
  (with-meta
    (struct concept-struct pname properties super)
    {:type ::m3-concept}))
  
(defn concept-def 
  [v] 
  (::def (meta v)))

(defn create-property-def 
  [pname validators super]
  (with-meta
    (struct property-struct pname validators super)
    {:type ::m3-property}))

(defn m3-property? [x]
  (isa? (type x) ::m3-property))

(defn property? [x]
  (isa? (type x) ::property))

;;change when property becomes usable
(defn property-def
  [prop]
  {:post [(m3-property? %)]}
  (if (property? prop)
    (::def (meta prop))
    prop))

(defn create-role-def 
  [property kind restrictions]
  {:pre [(m3-property? property) (contains? [:by-reference :by-value] kind)]}
  (with-meta
    (struct role-struct property kind restrictions)
    {:type ::m3-role}))

(defn create-many-role-def
  [property kind restrictions set-restrictions]
  {:pre [(m3-property? property) (contains? [:by-reference :by-value] kind)]}
  (with-meta
    (struct many-role-struct property kind restrictions set-restrictions)
    {:type ::m3-many-role}))

;;---------------------------------------------------------------------
(defmulti create-validator (fn [x] 
                             (let [metadata (meta x)] 
                               [(:link-type metadata) (:cardinality metadata)])))

(defmethod create-validator [nil nil]
  [property-def]
    (create-val-validator (reverse (deep :restrictions property-def))))

(defmethod create-validator [:by-value :1]
  [property-def]
    (create-val-validator (reverse (deep :restrictions property-def))))

(defmethod create-validator [:by-value :*]
  [property-def]
  (let [element-validator (create-multi-val-validator 
                                (concat (reverse (deep :restrictions property-def))
                                        (seq (:restrictions (meta property-def)))))]
    (if-let [set-restrictions (seq (:set-restrictions (meta property-def)))]
      (let  [set-validator (create-val-validator set-restrictions)]
        (fn [& args]
          (seq (concat (apply set-validator args) (apply element-validator args)))))
      element-validator)))

(defmethod create-validator [:by-reference :1]
  [property-def]
    (create-ref-validator (seq (concat (reverse (deep :restrictions property-def)) 
                                  (seq (:restrictions (meta property-def)))))))

(defmethod create-validator [:by-reference :*]
  [property-def]
  (let [element-validator (create-multi-ref-validator 
                                (concat (reverse (deep :restrictions property-def))
                                        (seq (:restrictions (meta property-def)))))]
    (if-let [set-validators (seq (:set-restrictions (meta property-def)))]
      (let  [set-validator (create-val-validator set-validators)]
        (fn [& args]
          (seq (concat (apply set-validator args) (apply set-validator args)))))
      set-validator)))

;;---------------------------------------------------------------------
(defmacro restrictions
  [v]
  `(:restrictions (property-def (var ~v))))

(defn create-property 
  [property-def]
  {:pre [(m3-property? property-def)]}
  (let [property-name (:name property-def)]
    (ref (fn [individual]
           (property-name individual))
         :meta {:type ::property
                ::def property-def
                ::hierarchy (infer-hierarchy (make-hierarchy) property-def)
                ::validation/validator (create-validator property-def)})))

;;--------------------------------------------------------------------------------------

(defn create-validators
  [property-defs]
  (let [validators (zipmap (map :name property-defs) 
                           (map create-validator property-defs))] 
        (reduce #(if (val %2) (assoc %1 (key %2) (val%2)) %1) {} restrictions)))

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

