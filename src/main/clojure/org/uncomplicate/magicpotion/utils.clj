(ns org.uncomplicate.magicpotion.utils
  (:use clojure.contrib.macros)) ;;this should cause errors once let-kw is there.

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

;;by Constantine Vetoshev
;;waits to be added to clojure.contrib.macros. Should be removed once it is there
(defmacro let-kw 
  "Adds flexible keyword handling to any form which has a parameter 
   list: fn, defn, defmethod, letfn, and others. Keywords may be 
   passed to the surrounding form as & rest arguments, lists, or 
   maps. Lists or maps must be used for functions with multiple 
   arities if more than one arity has keyword parameters. Keywords are 
   bound inside let-kw as symbols, with default values either 
   specified in the keyword spec or nil. Keyword specs may consist of 
   just the bare keyword symbol, which defaults to nil, or may have 
   the general form [keyword-name keyword-default-value* 
   keyword-supplied?*].  keyword-supplied?  is an optional symbol 
   bound to true if the keyword was supplied, and to false otherwise." 
  [kw-spec-raw kw-args & body] 
  (let [kw-spec  (map #(if (sequential? %) % [%]) kw-spec-raw) 
        symbols  (map first kw-spec) 
        keywords (map (comp keyword name) symbols) 
        defaults (map second kw-spec) 
        destrmap {:keys (vec symbols) :or (zipmap symbols defaults)} 
        supplied (reduce 
                  (fn [m [k v]] (assoc m k v)) (sorted-map) 
                  (remove (fn [[_ val]] (nil? val)) 
                          (partition 2 (interleave 
                                        keywords 
                                        (map (comp second rest) 
                                             kw-spec))))) 
        kw-args-map (gensym)] 
    `(let [kw-args# ~kw-args 
           ~kw-args-map (if (map? kw-args#) 
                            kw-args# 
                            (apply hash-map kw-args#)) 
           ~destrmap ~kw-args-map] 
       ~@(if (empty? supplied) 
             body 
             `((apply (fn [~@(vals supplied)] 
                        ~@body) 
                      (map (fn [x#] (contains? ~kw-args-map x#)) 
                           [~@(keys supplied)]))))))) 

  
  
