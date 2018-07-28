(ns compojure.claims
  (:require [compojure.core :refer :all]))


(defmulti has-claims?
  "Returns a boolean indicating weather the claims are valid"
  (fn [method required-claims request] method))


(defmulti handle-no-claims
  "Handle Invalid Claims. Return value should be a response object"
  (fn [method required-claims request] method))


(defn- compile-claims-route [method method-key path required-claims args body]
  `(fn [request#]
     (let [handler#
           (~method ~path ~args
            (if (has-claims? ~method-key ~required-claims request#)
              (do ~@body)
              (handle-no-claims ~method-key ~required-claims request#)))]
       (handler# request#))))


(defmacro GETx [path required-claims args & body]
  (compile-claims-route `GET :get path required-claims args body))


(defmacro POSTx [path required-claims args & body]
  (compile-claims-route `POST :post path required-claims args body))


(defmacro PUTx [path required-claims args & body]
  (compile-claims-route `PUT :put path required-claims args body))


(defmacro DELETEx [path required-claims args & body]
  (compile-claims-route `DELETE :delete path required-claims args body))


(defmacro HEADx [path required-claims args & body]
  (compile-claims-route `HEAD :head path required-claims args body))


(defmacro OPTIONSx [path required-claims args & body]
  (compile-claims-route `OPTIONS :options path required-claims args body))


(defmacro PATCHx [path required-claims args & body]
  (compile-claims-route `PATCH :patch path required-claims args body))


(defmacro ANYx [path required-claims args & body]
  (compile-claims-route `ANT :any path required-claims args body))