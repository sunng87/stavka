(ns stavka.utils)

(defmacro import-var [name v]
  `(do
     (def ~name ~v)
     (alter-meta! (var ~name)  merge (dissoc (meta ~v) :name))))

(defn deref-safe [s]
  (when s @s))
