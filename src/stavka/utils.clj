(ns stavka.utils)

(defmacro import-var [name v]
  `(do
     (def ~name ~v)
     (alter-meta! (var ~name)  merge (dissoc (meta ~v) :name))))

(defn deref-safe [s]
  (when s @s))

(defmacro when-required [req & body]
  `(try
     (require ~req)

     ~@body
     (catch java.io.FileNotFoundException e#
         nil)))

(defmacro when-imported [imp & body]
  `(try
     (import ~imp)

     ~@body
     (catch Exception e#
         nil)))
