(ns org.uncomplicate.magicpotion.lazy
  (:import java.lang.ref.SoftReference))

(defn provide [source soft-atom id]
  (let [#^java.lang.ref.Reference reference @soft-atom]
    (or (.get reference)
        (if-let [obj (source id)]
          (if (compare-and-set! soft-atom reference (SoftReference. obj))
              obj)))))

(deftype LazyReference [source soft-atom id]
  clojure.lang.IDeref
  (deref [this] (deref (provide source soft-atom id))))

(deftype LazyValue [source soft-atom id]
  clojure.lang.IDeref
  (deref [this] (provide source soft-atom id)))

;;-------------------- public ----------------------------
;;just for experiment. naive.replace once it works
(def storage (atom {}))
(defn src [id] (@storage id))

(defn identifier [x]
  (:identifier (meta x)))

(defn create-soft-atom [x] (atom (SoftReference. x)))

(defprotocol Lazy
  (lazy [x]))

(extend-type clojure.lang.Ref Lazy
  (lazy [x]
    (LazyReference. src
                    (create-soft-atom x)
                    (or (identifier x)
                        (IllegalArgumentException.)))))