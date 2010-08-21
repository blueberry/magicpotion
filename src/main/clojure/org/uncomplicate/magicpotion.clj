(ns org.uncomplicate.magicpotion
  (:use org.uncomplicate.magicpotion.utils)
  (:use [org.uncomplicate.magicpotion.validation :as validation])
  (:use org.uncomplicate.magicpotion.core)
  (:use [org.uncomplicate.magicpotion.m3 :as m3]))

(defn ref>
  ([prop]
   (ref> prop nil))
  ([prop restrictions]
  {:pre [(property? prop)]}
  (create-role-def (property-def prop)
                   :by-reference
                   restrictions
                   (::m3/hierarchy (meta prop)))))

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
                        set-restrictions
                        (::m3/hierarchy (meta prop)))))

(defn val>
  ([prop]
   (val> prop nil))
  ([prop restrictions]
  {:pre [(property? prop)]}
  (create-role-def (property-def prop)
                   :by-value
                   restrictions
                   (::m3/hierarchy (meta prop)))))

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
                        set-restrictions
                        (::m3/hierarchy (meta prop)))))

(defmacro property
  [name & params]
  {:pre [(every? (partial contains? #{:restrictions :super})
                 (filter keyword? params))]}
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
  {:pre [(every? (partial contains? #{:roles :restrictions :super})
                 (filter keyword? params))]}
  (let [pos (take-while (comp not keyword?) params)
        kw-map (apply hash-map (drop-while (comp not keyword?) params))
        roles (if-let [r (first pos)] r (:roles kw-map))
        super (if-let [s (second pos)] s (:super kw-map))
        restrictions (set (if-let [re (second (rest pos))] re (:restrictions kw-map)))]
   `(let [name-keyword# (to-keyword ~name)
          concept-def# (inherit-roles
                         (create-concept-def
                           name-keyword#
                           (sanitize-roles ~roles)
                           ~restrictions
                           (map concept-def ~super)))]
      (do
        (def ~(suf-symbol name "?") (is-instance name-keyword#))
        (def ~name (create-concept concept-def#)))))))

(defn individual
  ([v id]
  {:pre [(instance? java.util.UUID id)]}
    (ref v :meta {:identifier id}))
  ([v] (individual v (java.util.UUID/randomUUID))))