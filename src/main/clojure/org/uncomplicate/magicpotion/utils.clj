(ns org.uncomplicate.magicpotion.utils)

(defmacro suf-symbol
  [sym s]
  `(symbol (str (name ~sym) ~s)))

(defmacro pref-symbol
  [s sym]
  `(symbol (str ~s (name ~sym))))

(defmacro to-keyword
  [sym]
  `(keyword (second (re-find #"#'(.*?)/" (str (resolve '~sym)))) (name '~sym)))

(defmacro var-ize
  [var-vals]
  (loop [ret# [] vvs# (seq var-vals)]
    (if vvs#
      (recur (conj ret# `(var ~(first vvs#))) (next vvs#))
      ret#)))

(defn safe-apply [f x]
  (assert (fn? f))
  (if (sequential? x)
    (apply f x)
    (f x)))

;;do we still need this?
(defn meta-with [m]
  (fn [sym]
    (with-meta sym m)))
;; do we still need this?
(defmacro defmeta [sym m exp]
  `(def ~(with-meta sym m) ~exp))

(defn assoc-cat [k m1 m2]
  {:pre [(keyword? k)]}
  (assoc m2 k (concat (k m1) (k m2))))
