(ns stavka.example.kubernetes
  (:require [stavka.protocols :as sp])
  (:import [io.fabric8.kubernetes.client KubernetesClient]
           [io.fabric8.kubernetes.api.model ConfigMap]
           [java.io ByteArrayInputStream]))

(defrecord ConfigMapLoader [^KubernetesClient client
                            k8s-ns k8s-name k8s-item options]
  sp/Source
  (reload [this]
    (try
      (when-let [config-map (.. client
                           (configMaps)
                           (inNamespace k8s-ns)
                           (withName k8s-item)
                           (get))]
        (let [config-item (.. client
                              (getData)
                              (get k8s-item))]
          (ByteArrayInputStream. (.getBytes config-item "UTF-8"))))
      (catch Throwable e
        (when-not (:quiet? options) (throw e))))))

(defn kubernetes-configmap
  "the kubernetes configmap source.

  * client: the fabric8 kubernetes client
  * k8s-ns: the namespace of configmap
  * k8s-name: the name of configmap
  * k8s-item: the item name of configmap
  "
  [^KubernetesClient client k8s-ns k8s-name k8s-item options]
  (ConfigMapLoader. client k8s-ns k8s-name k8s-item options))
