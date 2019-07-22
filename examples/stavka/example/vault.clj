(ns stavka.example.vault
  (:require [vault.core :as vault]
            [stavka.core :as s]
            [stavka.protocols :as sp]
            [stavka.resolvers.flatdict]
            [stavka.formats.none]))

(defrecord VaultLoader [vault-client vault-info options]
  sp/Source
  (reload [this]
    (try
      (vault/read-secret vault-client (:path vault-info))
      (catch Throwable e
        (when-not (:quiet? options) (throw e))))))

(defn vault-source [vault-client vault-info & {:as options}]
  (VaultLoader. vault-client vault-info options))

(defn vault [vault-client vault-info]
  (s/holder-from-source (vault-source vault-client vault-info)
                        (stavka.formats.none/the-format)
                        (stavka.resolvers.flatdict/resolver)))

(defn -main [& args]
  (let [client (vault/new-client "http://127.0.0.1:8200")
        _ (vault/authenticate! client :app-id {:app "my_app", :user "0000-userid-000"})]
    (s/global! (vault client {:path "secret/config"}))
    (println! "Getting config from vault :test.key1" (s/$ :test.key1))))
