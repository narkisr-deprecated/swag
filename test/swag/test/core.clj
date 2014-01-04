(ns swag.test.core
  (:refer-clojure :exclude  [name type])
  (:require
    [compojure.core :refer (POST)] 
    [swag.core :refer (defroutes- GET- POST- apis type-match set-base)]
    [swag.model :refer (defmodel models conversions defc convert)] )
  (:use midje.sweet))

(defn swag-meta [r & ks] (-> r meta (get-in ks)))

(fact "half way doc"
  (defroutes- machines {:path "/machines" :description "Operations on machines"}
    (GET- "/machine/" [^:string host] {:nickname "getMachine" :summary "gets a machine"}  
          (println host))
    (POST "/machine/" [^:string host] (println host)))
  (count (get-in @apis [:machines :apis])) => 1)

(fact "manual params"
  (defroutes- machines {}
    (GET- "/machine/" [^{:paramType "query" :dataType "String"} host] {:nickname "getMachine" :summary "gets a machine"}  
          ()))
  (let [param (get-in @apis [:machines :apis 0  :operations 0 :parameters 0])]
    (param :dataType) => "String" 
    (param :paramType) => "query") 
  )

(fact "auto param type guessing"
  (let [param (swag-meta (GET- "/machine/:host" [^:string host] {} ()) :operations 0 :parameters 0)]
    (param :dataType) =>  "string" 
    (param :paramType) => "path") 
  )

(defmodel type :id :string)

(fact "using model"
  (let [param (swag-meta (GET- "/machine/" [^:string host ^:type type] {} ()) :operations 0 :parameters 1)]
    (param :dataType) =>  "Type" 
    (param :paramType) => "body"))

(fact "missing type"
   (type-match {:foo true}) => (throws Exception))

(fact "conversion mapping"
   (defc "/machine" [:type] (keyword v))   
   (@conversions "/machine")  => (just {[:type] fn?}) 
   (convert "/machine" {:type "foo"}) => {:type :foo}
      )

