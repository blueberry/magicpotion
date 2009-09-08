(ns org.aloole.magicpotion.utils)

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

(defn meta-with [m] 
  (fn [sym] 
    (with-meta sym m)))

(defmacro defmeta [sym m exp] 
  `(def ~(with-meta sym m) ~exp))
