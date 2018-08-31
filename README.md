# compojure.claims

_compojure.claims_ is a small library that allows you to do claims/roles authorisation per Compojure route.

## Installation

_compojure.claims_ assumes that you have Compojure added as a project dependency.

```
[za.co.simply/compojure.claims "1.0.0" :exclusions [compojure]]
```

## Usage

_compojure.claims_ provides a simple way to add authorisation requirements per compojure route, without specifying how you should implement your authentication and authorisation.

**Routes**

A corresponding claim/role method is provided for each Compojure method. Example `GETx`, `POSTx`, etc. They work exactly like the Compojure route methods, except that you should provide a required claim expression after the path definition.

```
(GETx "/a-simply-path" :a-keyword-claim request "Some Response")
```

Claims can be of any type and compojure destructuring works as normal

```
(POSTx "/users" [:create-user :update-user] {params :params} (upsert-user params))

(POSTx "/map-claims" {:foo "bar"} request "this is valid")

(POSTx "/int-claims" 1 request "this is also valid")
```

**Authentication**

compojure.claims expects that your users are already authenticated and that their claims/roles are available on the request map.

**Authorisation**

How you check your claims is entirely up to you. You define how by extending the `has-claims?` multimethod. The body of your request will only be executed if `has-claims?` returns a truthy value

```
(defmethod has-claims? :default [_ required-claims request]
  (= required-claims (:claims request)))
```

**Handling Invalid Claims**

What happens when a user does not have the required claims or roles is defined by extending the `handle-no-claims` multimethod. `handle-no-claims` will get executed if `has-claims?` returns a falsy value.

```
(defmethod handle-no-claims :default [_ required-claims request]
  {:status 403
   :body "<h1>You shall not pass</h1>"})
```

**Claims per Method type**

Both `has-claims?` and `handle-no-claims` can be implemented for any of the compojure route methods (`:get`, `:post`, `:options` etc) as well as for the `ANY` route by using `:any**


**Note**

Behaviour might be unexpected when used with `wrap-routes`, because both need to match the route before executing the body.

## Example

```
(ns compojure.claims
  (:require [compojure.core :refer :all]
            [compojure.claims :refer :all]))


(defmethod has-claims? :default [_ required-claim request]
  (let [user-claims (get-in request [:user :claims])]
    (contains? user-claims required-claim)))


(defmethod handle-no-claims :default [_ required-claim _]
  (throw (NotAuthedException. (str "requires " required-claim))))


;;; define your routes

(defn app []
  (routes
   (GET "/" _ "Welcome Everyone")
   (GETx "/user/:id" :view-users [id] (get-user id))
   (POSTx "/user" :upsert-user {params :params} (upsert-user params))))
```
