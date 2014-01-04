(ns swag.test.core
  (:use 
    clojure.test 
    [compojure.core :only (POST)] 
    [swag.core :only (defroutes- GET- POST- apis type-match set-base)]
    [swag.model :only (defmodel models)] 
    ))

(set-base "http://localhost:8080")
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
    (is (= (param :dataType)  "Type")) 
    (is (= (param :paramType) "body"))))

(deftest missing-type
   (is (thrown? Exception (type-match {:foo true})))
  )
