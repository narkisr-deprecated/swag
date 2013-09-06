(ns swag.core
 "Swagger integration for Compojure, see https://github.com/wordnik/swagger-core/wiki/Resource-Listing for api definitions."
 (:refer-clojure :exclude [replace])
 (:use 
   clojure.pprint
   [swag.model :only (models)]
   [swag.common :only (defstruct-)]
   [flatland.useful.seq :only (find-first)]
   [clojure.string :only (replace capitalize)]
   [clojure.core.strint :only (<<)]
   [compojure.core :only (routes defroutes GET POST PUT DELETE context make-route)])
   (:require 
     [compojure.route :as route])) 

(def ^:dynamic base "http://localhost:8080/")

(defstruct- base-swag :apiVersion :swaggerVersion :basePath)

(defstruct- resource-listing :apiVersion :swaggerVersion :basePath :apis)

(defstruct- api-decleration  :apiVersion :swaggerVersion :basePath :resourcePath :apis :models)

(defstruct- bare-api :path :description)

(defstruct- api :path :description :operations)

(defstruct- operation :httpMethod :nickname :responseClass :parameters :summary :notes :errorResponses)

(defstruct- parameter :paramType :name :description :dataType :required :allowableValues :allowMultiple)

(defstruct- property :name :type :description)

(defstruct- error :code :reason)

(def ^{:doc "see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html"}
  http-codes {:conflict 409 :success 200 :bad-req 400})

(defn errors
 "A map of errors to swagger errors"
 [es]
  (mapv (fn [[k v]] (error- (http-codes k) v)) es))

(def ^{:doc "see https://github.com/wordnik/swagger-core/wiki/Datatypes"}
  primitives #{:file :byte :boolean :int :long :float :double :string :Date})

(def ^{:doc "see https://github.com/wordnik/swagger-core/wiki/Datatypes"}
  containers  #{:List :Set :Array})

(def apis (atom {}))

(defn type-match 
  "Matches a given type agains primitives containers and custom defined types"
  [m]
    (or 
      (some-> (find-first (into #{} (keys @models)) (keys m)) name capitalize)
      (some-> (find-first primitives (keys m)) name)
      (some-> (m :dataType) name)
      (throw (Exception. (str "Failed to find type " m))) 
      ))

(defn guess-type 
  "Detects param type (both http and object type)."
  [path arg]
  (let [m (meta arg)]
    {:paramType (or (m :paramType) (if (.contains path (<< ":~(str arg)")) "path" "body"))
     :dataType (type-match m)}))

(defn create-params 
  "Creates the parameter list for a given path out of provided args"
  [path args] 
  (let [defaults (parameter- nil nil nil "String" true nil false) ]
    (mapv 
      #(-> % meta 
           (merge defaults {:name (str %)} (guess-type path %))) (remove #(= % '&) args))))

(defn create-op 
  "Creates an operation" 
  [desc verb params]
  (merge (operation-) desc {:parameters params :httpMethod verb}))

(defn swag-path [path]
  "Converts compojure params to swagger params notation"
  (replace path #"\:(\w*)" (fn [v] (<< "{~{(second v)}}"))))

(defmacro swag-verb 
  "Creates a swagger enabled http route with a given verb"
  [verb path args desc & body]
  {:pre [(map? desc)]}
  `(with-meta (~verb ~path ~args ~@body) 
              (api- ~(swag-path path) "THis should be taken from defroute!"
                    [(create-op ~desc (-> ~verb var meta :name) ~(create-params path args))])))

(defmacro GET- 
  "A swagger enabled GET route."
  [path args desc & body]
  `(swag-verb GET ~path ~args ~desc ~@body))

(defmacro POST- 
  "A swagger enabled POST route."
  [path args desc & body]
  `(swag-verb POST ~path ~args ~desc ~@body))

(defmacro PUT- 
  "A swagger enabled PUT route."
  [path args desc & body]
  `(swag-verb PUT ~path ~args ~desc ~@body))

(defmacro DELETE-
  "A swagger enabled DELETE route."
  [path args desc & body]
  `(swag-verb DELETE ~path ~args ~desc ~@body))

(defn combine-apis [_apis]
  "Merges api routes with same paths different verbs"
  (mapv #(apply merge-with (fn [f s] (if (vector? f) (into [] (concat f s)) f)) %)
        (vals (group-by :path _apis))))

(defn create-api 
  "Creates the api route"
  [_name & routes]
  (let [_apis (filterv identity (map meta (rest routes)))]
    (swap! apis assoc (keyword _name) 
           (api-decleration- "0.1" "1.1" (<< "~{base}api") (<< "/~{_name}") (combine-apis _apis)
                             (into {} (map (fn [[k v]] [(-> k name capitalize) v]) @models))))))

(defmacro defroutes-
  "A swagger enabled defroute"
  [name & routes]
  `(do 
     (create-api ~(str name) ~@routes)
     (defroutes ~name ~@routes)))

#_(defroutes- machines {:path "/machine" :description "Operations on machines"}
    (GET- "/machine/:host" [^:string host] 
          {:nickname "getMachine" :summary "gets a machine"}  ())
    (POST- "/machine/:host" [^:string host] 
           {:nickname "addMachine" :summary "adds a machine"} ())
    (GET- "/machine/:cpu" [^:int cpus] 
          {:nickname "getCpus" :summary "get machines cpus list"} ())  
    (POST- "/type" [^:string type & ^:int props] {:nickname "addType" :summary "Adds a type"}
           (identity 1))
    )

(defn listing
  "The swagger api listing (api-docs.json)." 
  [version]
  (resource-listing- version "1.1" base (mapv (fn [[k v]] (bare-api- (str "/api/" (name k)) "")) @apis)))

(defn api-declerations
  "generates api decleration routes"
  []
  (apply routes 
         (map (fn [[k v]] (make-route :get (str "/api/" (name k)) (fn [_] {:body (@apis k)}))) @apis)))

(defn swagger-routes 
  "The swagger-routes ready to be include into a compojure app"
  [version]
  (routes
    (api-declerations)
    (GET "/api-docs.json" [h] {:body (listing version)}) 
    (route/files "/swagger/" {:root (str (System/getProperty "user.dir") "/public/swagger-ui-1.1.7/")})))


