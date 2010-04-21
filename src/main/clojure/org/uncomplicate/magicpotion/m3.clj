(ns org.uncomplicate.magicpotion.m3
	(:use clojure.contrib.seq-utils)
  (:use org.uncomplicate.magicpotion.utils)
  (:use org.uncomplicate.magicpotion.predicates)
  (:use [org.uncomplicate.magicpotion.validation :as validation])
  (:use org.uncomplicate.magicpotion.core))

;;-------------------- Bootstrap -------------------------------------
(def concept-struct (create-struct :name :roles :super))

(def property-struct (create-struct :name :restrictions :super))

(def role-struct (create-struct :property :kind :restrictions :super))

(def many-role-struct (create-struct :property :kind :restrictions :set-restrictions :super))

(defn create-concept-def 
  [pname roles super]
  (with-meta
    (struct concept-struct pname roles super)
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

(defn property-def
  [prop]
  (::def (meta prop)))

(defn create-role-def 
  [property kind restrictions]
  {:pre [(m3-property? property) (contains? #{:by-reference :by-value} kind)]}
  (with-meta
    (struct role-struct property kind restrictions)
    {:type ::m3-role}))

(defn create-many-role-def
  [property kind restrictions set-restrictions]
  {:pre [(m3-property? property) (contains? #{:by-reference :by-value} kind)]}
  (with-meta
    (struct many-role-struct property kind restrictions set-restrictions)
    {:type ::m3-many-role}))

;;---------------------------------------------------------------------
(defmulti create-validator (fn [role]  
                               [(type role) (:kind role)]))

(defmethod create-validator [::m3-property nil]
  [property-def]
    (create-val-validator (reverse (deep :restrictions property-def))))

(defmethod create-validator [::m3-role :by-value]
  [role-def]
    (create-val-validator (seq (concat (reverse (deep :restrictions (:property role-def))) 
                                  		 (reverse (deep :restrictions role-def))))))

(defmethod create-validator [::m3-many-role :by-value]
  [role-def]
  (let [element-validator (create-multi-val-validator 
														(concat (reverse (deep :restrictions (:property role-def)))
                                    (reverse (deep :restrictions role-def))))]
    (if-let [set-restrictions (reverse (deep :set-restrictions role-def))]
      (let  [set-validator (create-val-validator set-restrictions)]
        (fn [& args]
          (seq (concat (apply set-validator args) (apply element-validator args)))))
      element-validator)))

(defmethod create-validator [::m3-role :by-reference]
  [role-def]
  (create-ref-validator (seq (concat (reverse (deep :restrictions (:property role-def))) 
                                  	 (reverse (deep :restrictions role-def))))))

(defmethod create-validator [::m3-many-role :by-reference]
  [role-def]
  (let [element-validator (create-multi-ref-validator 
                                (concat (reverse (deep :restrictions (:property role-def)))
                                        (reverse (deep :restrictions role-def))))]
    (if-let [set-restrictions (reverse (deep :set-restrictions role-def))]
      (let  [set-validator (create-val-validator set-restrictions)]
        (fn [& args]
          (seq (concat (apply set-validator args) (apply element-validator args)))))
      element-validator)))

;;---------------------------------------------------------------------
(defmacro restrictions
  [v]
  `(:restrictions (property-def (var ~v))))

(defn create-property 
  [property-def]
  {:pre [(m3-property? property-def)]}
  (let [property-name (:name property-def)
				property-function (fn ([individual] (property-name individual))
															([] property-name))]
    (ref property-function
         :meta {:type ::property
                ::def property-def
                ::hierarchy (infer-hierarchy (make-hierarchy) property-def)
                ::validation/validator (create-validator property-def)})))

;;--------------------------------------------------------------------------------------
(defn inherit-roles [concept-def]
	(let [roles (:roles concept-def)
				deep-roles (deep :roles concept-def)
				processed-roles (reduce (fn [r ro] 
																(cons (if-let [sro (find-first 
																											#(and (not (= ro %)) 
																														((partial = (:name (:property ro))) 
																												    	(:name (:property %)))) 
																											deep-roles)]
																				(assoc ro :super (cons sro (:super ro)))
																				ro)
																			r))
 											 () roles)]
		(assoc concept-def :roles processed-roles)))

(defn create-validators
  [role-defs] 
  (let [validators (zipmap (map (comp :name :property) role-defs) 
                           (map create-validator role-defs))]
        (reduce (fn [r [k v]] (if v (assoc r k v) r)) {} validators)))

;;-------------------------------------------------------------------------------------

(defn create-concept [concept-def]
  (let [concept-name (:name concept-def)
        concept-struct (create-struct-deep concept-def)
        validators (create-validators (reverse (deep :roles concept-def)))
        hierarchy (infer-hierarchy (make-hierarchy) concept-def)
        instance-metadata {:type concept-name
                           ::def concept-def
                           ::hierarchy hierarchy
                           ::struct concept-struct
                           ::validation/validators validators}]
    (ref (fn [& role-entries]
           (with-meta
             (apply struct-map concept-struct (validate validators role-entries))
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
