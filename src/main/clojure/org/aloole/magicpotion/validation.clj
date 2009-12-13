(ns org.aloole.magicpotion.validation
  (:use clojure.set)
  (:use org.aloole.magicpotion.utils))

(defn satisfies?
  ([f x]
   (cond
     (fn? f) (if (f x) true false)
     (sequential? f) (and ((first f) x) (recur (next f) x))))
  ([f x & more]
   (if (satisfies? f x)
     (if more
       (recur (first more) (second more) (next (next more)))
       true)
     false)))

(defn satisfy?
  [fs xs]
  (if (satisfies? (first fs) (first xs))
    (if (and (seq fs) (seq xs))
      (recur (next fs) (next xs))
      true)
    false))

(defn check-preconditions [condition preds f]
  (assert (and (fn? condition) preds (fn? f)))
  (fn [& args]
    (if (condition preds args)
       (safe-apply f args))
    (throw (IllegalArgumentException. ))))

(def verify (partial check-preconditions satisfy?))

;;---------------------- Validation -----------------------------

(defn create-validator
  ([f]
   (assert (or (fn? f) (and (sequential? f) (every? fn? f))))
   (let [try-apply (fn [func args] 
                     (try (apply func args) 
                       (catch RuntimeException e false)))]
     (cond
       (fn? f) (fn [& args]
                 (if (try-apply f args)
                   false (list f)))
       (sequential? f) (fn [& args]
                         (if-let [errors (seq (remove #(try-apply % args) f))]
                           errors
                           false)))))
  ([f & more]
   (create-validator (cons f more))))

(defn assoc-violations [validator result value-entry]
  (if-let [errors (safe-apply validator (val value-entry))]
    (assoc result (key value-entry) errors)
    result))

(defmulti violations (fn 
                       ([x] (type x))
                       ([x y] [(type x) (type y)])))

(defmethod violations
  [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]
  [validators values]
  (let [errors (reduce #(if-let [validator (validators (key %2))] 
                             (assoc-violations validator %1 %2) 
                             %1)
                          {} values)] 
    (if (seq errors) errors false)))

(defmethod violations
  [clojure.lang.IPersistentMap clojure.lang.Sequential]
  [validators values]
  (violations validators (safe-apply hash-map values)))

(defmethod violations
  [clojure.lang.IFn clojure.lang.IPersistentMap]
  [validator values]
  (let [errors (reduce #(assoc-violations validator %1 %2) {} values)]
    (if (seq errors) errors false)))

(defmethod violations
  [clojure.lang.IFn clojure.lang.Sequential]
  [validator value]
  (if-let [errors (apply validator value)]
    {value errors}
    false))

(defmethod violations
  [clojure.lang.IFn Object]
  [validator value]
  (if-let [res-entry (validator value)] 
    {value res-entry}
    false))

(defmethod violations
  [clojure.lang.IFn nil]
  [validator value]
  (if-let [res-entry (validator value)] 
    {value res-entry}
    false))

(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]
               [clojure.lang.IFn Object])
(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]
               [clojure.lang.IPersistentMap clojure.lang.Sequential])
(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]
               [clojure.lang.IFn clojure.lang.IPersistentMap])
(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.Sequential]
               [clojure.lang.IFn clojure.lang.Sequential])
(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.Sequential]
               [clojure.lang.IFn Object])
(prefer-method violations
               [clojure.lang.IPersistentMap clojure.lang.IPersistentMap]
               [clojure.lang.IFn Object])

(defn valid? 
  ([x]
   (valid? (::validators (meta x)) x))
  ([validators x]
   (if validators
     (not (violations validators x))
     true)))

(defn validate
  ([x]
   (validate (::validators (meta x)) x))
  ([validator x]
   (if validator
     (if-let [errors (violations validator x)]
       (throw (IllegalArgumentException. (str errors)))
       x)
     x)))

(defn pre-validate 
  ([validator f]
   (assert (and f validator))
   (fn [& args] (apply f (validate validator args))))
  ([f]
   (let [validator (::validators (meta f))]
     (assert validator)
     (pre-validate validator @f))))

(defn post-validate
  ([validator f]
   (assert (and (fn? f) validator))
   (fn [& args] (validate validator (apply f args))))
  ([f]
   (assert (fn? f))
   (fn [& args] (validate (apply f args)))))
