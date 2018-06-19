(ns stavka.sources.url
  (:require [stavka.protocols :as sp]
            [clj-http.client :as httpc]))

(def default-http-options
  {:as :stream
   :trace-redirects true
   :socket-timeout 5000
   :conn-timeout 5000})

(defrecord UrlLoader [url http-options]
  sp/Source
  (reload [this]
    (-> url
        (httpc/get (merge default-http-options http-options))
        :body)))

(defn url
  "returns a url loader"
  ([the-url] (url the-url {}))
  ([the-url http-opts] (UrlLoader. the-url http-opts)))
