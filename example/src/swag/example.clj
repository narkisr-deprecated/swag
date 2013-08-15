(ns swag.example
  (:require 
    [swag.core :refer (swagger-routes GET- POST- PUT- DELETE- defroutes- errors)]
    [swag.model :refer (defmodel wrap-swag defv defc)]
    [ring.middleware [multipart-params :as mp] ]
    [compojure.handler :as handler :refer (site)]
    [ring.middleware.format :refer (wrap-restful-format)]
    [compojure.core :refer (defroutes routes)] 
    [ring.adapter.jetty :refer (run-jetty)] 
    [compojure.route :as route]))

; defining custom data types for more info please visit https://github.com/wordnik/swagger-core/wiki/Datatypes
(defmodel ccontainer :capistrano {:type "Capistrano"})

; nested data type
(defmodel actions :action-a {:type "Ccontainer"})

(defmodel action :operates-on :string :src :string :actions {:type "Actions"})

; /actions in the swagger ui 
(defroutes- actions {:path "/actions" :description "Adhoc actions managment"}

  ; here we use the custom action model (the model schema will reflect that).
  (POST- "/action" [& ^:action action] {:nickname "addActions" :summary "Adds an actions set"}
         {:status 200 :body (str "got action " action)})

  (PUT- "/action/:id" [^:int id & ^:action action] {:nickname "updateActions" :summary "Update an actions set"}
        {:status 200 :body (str "got action " action " with id " id)})

  (GET- "/action/by-target/:type" [^:string type] {:nickname "getActionsByTargetType" :summary "Gets actions that operate on a target type"}
        {:status 200 :body (str "got type " type)})

  ; note the use of :errorResponses
  (DELETE- "/action/:id" [^:int id] {:nickname "deleteActions" :summary "Deletes an action set" :errorResponses (errors {:bad-req "Missing action"})}
           {:status 200 :body (str "got id " id)}))

(defn app []
  "The api routes, secured? will enabled authentication"
  (-> 
    ; note the swagger routes version nubmer
    (routes (swagger-routes "0.0.1") actions (route/not-found "Not Found"))
    ; enables the api-docs.json route, see public/swagger-ui-1.1.7/index.html
    (wrap-swag) 
    (handler/api)
    (wrap-restful-format :formats [:json-kw :edn :yaml-kw :yaml-in-html])
    (mp/wrap-multipart-params)))

(defn -main [& args]
  (run-jetty (app) {:port 8080 :join? false }))
