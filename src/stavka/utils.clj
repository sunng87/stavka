(ns stavka.utils)

(defmacro import-var [name v]
  `(do
     (def ~name ~v)
     (alter-meta! (var ~name)  merge (dissoc (meta ~v) :name))))

(defn deref-safe [s]
  (when s @s))

(defmacro if-provided [req positive-branch negative-branch]
  `(try
     (require ~req)

     ~positive-branch
     (catch java.io.FileNotFoundException e#
       ~negative-branch)))
