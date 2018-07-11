(ns stavka.formats.none
  (:require [stavka.protocols :as sp]))

(defrecord NoneFormat []
  sp/Format
  (parse [this input] input))

(def instance (NoneFormat.))

(defn the-format
  "The nil format used when the source has no format at all."
  [] instance)
