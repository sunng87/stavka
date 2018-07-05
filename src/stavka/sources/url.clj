(ns stavka.sources.url
  (:require [stavka.protocols :as sp]
            [clj-http.client :as httpc]))

(def default-http-options
  {:as :stream
   :trace-redirects true
   :socket-timeout 5000
   :conn-timeout 5000})

(defrecord UrlLoader [url options]
  sp/Source
  (reload [this]
    (let [quiet? (:quiet? options)]
      (try
        (-> url
            (httpc/get (merge default-http-options options))
            :body)
        (catch Throwable e
          (when-not quiet? (throw e)))))))

(defn url
  "returns a url loader"
  [the-url & {:as options}]
  (UrlLoader. the-url options))
