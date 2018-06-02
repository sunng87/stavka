(ns stavka.sources.url
  (:require [stavka.protocols :as sp]
            [clj-http.client :as httpc]))

(defrecord UrlLoader [url http-options]
  sp/Source
  (reload [this]
    (httpc/get url (assoc http-options :as :stream))))

(defn url
  "returns a url loader"
  ([the-url] (url the-url {}))
  ([the-url http-opts] (UrlLoader. the-url http-opts)))
